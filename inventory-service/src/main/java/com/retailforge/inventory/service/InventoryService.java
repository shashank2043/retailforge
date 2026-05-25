package com.retailforge.inventory.service;

import com.retailforge.inventory.dto.*;
import com.retailforge.inventory.event.LowStockAlertEvent;
import com.retailforge.inventory.event.StockUpdatedEvent;
import com.retailforge.inventory.event.OrderCreatedEvent;
import com.retailforge.inventory.event.OrderItemEvent;
import com.retailforge.inventory.model.Inventory;
import com.retailforge.inventory.model.StockTransaction;
import com.retailforge.inventory.model.Warehouse;
import com.retailforge.inventory.repository.InventoryRepository;
import com.retailforge.inventory.repository.StockTransactionRepository;
import com.retailforge.inventory.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.retailforge.inventory.exception.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockTransactionRepository transactionRepository;
    private final InventoryEventPublisher eventPublisher;

    private static final int LOW_STOCK_THRESHOLD = 10;

    public InventoryService(InventoryRepository inventoryRepository,
                            WarehouseRepository warehouseRepository,
                            StockTransactionRepository transactionRepository,
                            InventoryEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.name());
        warehouse.setLocation(request.location());
        Warehouse saved = warehouseRepository.save(warehouse);
        return new WarehouseResponse(saved.getId(), saved.getName(), saved.getLocation());
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
            .map(w -> new WarehouseResponse(w.getId(), w.getName(), w.getLocation()))
            .collect(Collectors.toList());
    }

    @Transactional
    public InventoryResponse addStock(AddStockRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found with ID: " + request.warehouseId()));

        if (request.quantity() <= 0) {
            throw new InvalidQuantityException("Stock quantity to add must be greater than zero.");
        }

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(request.productId(), request.warehouseId())
            .orElseGet(() -> {
                Inventory inv = new Inventory();
                inv.setProductId(request.productId());
                inv.setWarehouseId(request.warehouseId());
                inv.setQuantity(0);
                return inv;
            });

        int oldQuantity = inventory.getQuantity();
        int newQuantity = oldQuantity + request.quantity();
        inventory.setQuantity(newQuantity);
        
        Inventory saved = inventoryRepository.save(inventory);

        // Record stock transaction history log
        StockTransaction tx = new StockTransaction();
        tx.setProductId(request.productId());
        tx.setWarehouseId(request.warehouseId());
        tx.setQuantity(request.quantity());
        tx.setType("ADDITION");
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);

        // Publish STOCK_UPDATED event
        eventPublisher.publishStockUpdated(new StockUpdatedEvent(
            request.productId(),
            request.warehouseId(),
            oldQuantity,
            newQuantity,
            LocalDateTime.now()
        ));

        // Low stock alert check (though we added stock, it could still be low)
        checkLowStockAlert(request.productId(), request.warehouseId(), newQuantity);

        return mapToInventoryResponse(saved);
    }

    @Transactional
    public InventoryResponse reduceStock(Long productId, Long warehouseId, Integer quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Deduction quantity must be greater than zero.");
        }

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new InventoryNotFoundException("Inventory record not found for product: " + productId + " and warehouse: " + warehouseId));

        int oldQuantity = inventory.getQuantity();
        if (oldQuantity < quantity) {
            throw new InsufficientStockException("Insufficient inventory. Available: " + oldQuantity + ", Requested: " + quantity);
        }

        int newQuantity = oldQuantity - quantity;
        inventory.setQuantity(newQuantity);
        Inventory saved = inventoryRepository.save(inventory);

        // Record transaction
        StockTransaction tx = new StockTransaction();
        tx.setProductId(productId);
        tx.setWarehouseId(warehouseId);
        tx.setQuantity(-quantity);
        tx.setType("DEDUCTION");
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);

        // Publish STOCK_UPDATED event
        eventPublisher.publishStockUpdated(new StockUpdatedEvent(
            productId,
            warehouseId,
            oldQuantity,
            newQuantity,
            LocalDateTime.now()
        ));

        // Check and publish low stock alert if threshold crossed
        checkLowStockAlert(productId, warehouseId, newQuantity);

        return mapToInventoryResponse(saved);
    }

    @Transactional
    public List<InventoryResponse> transferStock(TransferStockRequest request) {
        if (request.quantity() <= 0) {
            throw new InvalidQuantityException("Transfer quantity must be greater than zero.");
        }
        if (request.sourceWarehouseId().equals(request.destinationWarehouseId())) {
            throw new InvalidWarehouseTransferException("Source and destination warehouses cannot be the same.");
        }

        Warehouse sourceWarehouse = warehouseRepository.findById(request.sourceWarehouseId())
            .orElseThrow(() -> new WarehouseNotFoundException("Source warehouse not found with ID: " + request.sourceWarehouseId()));
        Warehouse destWarehouse = warehouseRepository.findById(request.destinationWarehouseId())
            .orElseThrow(() -> new WarehouseNotFoundException("Destination warehouse not found with ID: " + request.destinationWarehouseId()));

        // 1. Deduct from source warehouse
        Inventory sourceInv = inventoryRepository.findByProductIdAndWarehouseId(request.productId(), request.sourceWarehouseId())
            .orElseThrow(() -> new InventoryNotFoundException("No inventory found for product at source warehouse."));

        int sourceOldQty = sourceInv.getQuantity();
        if (sourceOldQty < request.quantity()) {
            throw new InsufficientStockException("Insufficient inventory at source. Available: " + sourceOldQty + ", Requested: " + request.quantity());
        }

        int sourceNewQty = sourceOldQty - request.quantity();
        sourceInv.setQuantity(sourceNewQty);
        Inventory savedSource = inventoryRepository.save(sourceInv);

        // Log transaction (Outbound)
        StockTransaction txOut = new StockTransaction();
        txOut.setProductId(request.productId());
        txOut.setWarehouseId(request.sourceWarehouseId());
        txOut.setQuantity(-request.quantity());
        txOut.setType("TRANSFER_OUT");
        txOut.setTimestamp(LocalDateTime.now());
        transactionRepository.save(txOut);

        // 2. Add to destination warehouse
        Inventory destInv = inventoryRepository.findByProductIdAndWarehouseId(request.productId(), request.destinationWarehouseId())
            .orElseGet(() -> {
                Inventory inv = new Inventory();
                inv.setProductId(request.productId());
                inv.setWarehouseId(request.destinationWarehouseId());
                inv.setQuantity(0);
                return inv;
            });

        int destOldQty = destInv.getQuantity();
        int destNewQty = destOldQty + request.quantity();
        destInv.setQuantity(destNewQty);
        Inventory savedDest = inventoryRepository.save(destInv);

        // Log transaction (Inbound)
        StockTransaction txIn = new StockTransaction();
        txIn.setProductId(request.productId());
        txIn.setWarehouseId(request.destinationWarehouseId());
        txIn.setQuantity(request.quantity());
        txIn.setType("TRANSFER_IN");
        txIn.setTimestamp(LocalDateTime.now());
        transactionRepository.save(txIn);

        // Publish events for source warehouse
        eventPublisher.publishStockUpdated(new StockUpdatedEvent(
            request.productId(),
            request.sourceWarehouseId(),
            sourceOldQty,
            sourceNewQty,
            LocalDateTime.now()
        ));
        checkLowStockAlert(request.productId(), request.sourceWarehouseId(), sourceNewQty);

        // Publish events for destination warehouse
        eventPublisher.publishStockUpdated(new StockUpdatedEvent(
            request.productId(),
            request.destinationWarehouseId(),
            destOldQty,
            destNewQty,
            LocalDateTime.now()
        ));
        checkLowStockAlert(request.productId(), request.destinationWarehouseId(), destNewQty);

        return Arrays.asList(mapToInventoryResponse(savedSource), mapToInventoryResponse(savedDest));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByProduct(Long productId) {
        // Return inventory lists across warehouses
        return inventoryRepository.findAll().stream()
            .filter(inv -> inv.getProductId().equals(productId))
            .map(this::mapToInventoryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new InventoryNotFoundException("Inventory record not found for product: " + productId + " and warehouse: " + warehouseId));
        return mapToInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getTransactionHistoryByWarehouse(Long warehouseId) {
        return transactionRepository.findByWarehouseId(warehouseId).stream()
            .map(tx -> new StockTransactionResponse(tx.getId(), tx.getProductId(), tx.getWarehouseId(), tx.getQuantity(), tx.getType(), tx.getTimestamp()))
            .toList();
    }

    @Transactional
    public void reserveStockForOrder(OrderCreatedEvent event) {
        log.info("Attempting to reserve stock for order: {}", event.orderNumber());
        // Cashier billing is fulfilled from the retail store warehouse ID: 2
        Long defaultStoreOutletWarehouseId = 2L;

        for (OrderItemEvent item : event.items()) {
            reduceStock(item.productId(), defaultStoreOutletWarehouseId, item.quantity());
        }
    }

    private void checkLowStockAlert(Long productId, Long warehouseId, int currentQty) {
        if (currentQty < LOW_STOCK_THRESHOLD) {
            eventPublisher.publishLowStockAlert(new LowStockAlertEvent(
                productId,
                warehouseId,
                currentQty,
                LOW_STOCK_THRESHOLD,
                LocalDateTime.now()
            ));
        }
    }

    private InventoryResponse mapToInventoryResponse(Inventory inventory) {
        return new InventoryResponse(
            inventory.getId(),
            inventory.getProductId(),
            inventory.getWarehouseId(),
            inventory.getQuantity(),
            inventory.getVersion()
        );
    }
}
