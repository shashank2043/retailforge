package com.retailforge.inventory.service;

import com.retailforge.inventory.dto.*;
import com.retailforge.inventory.event.LowStockAlertEvent;
import com.retailforge.inventory.event.StockUpdatedEvent;
import com.retailforge.inventory.exception.*;
import com.retailforge.inventory.model.Inventory;
import com.retailforge.inventory.model.StockTransaction;
import com.retailforge.inventory.model.Warehouse;
import com.retailforge.inventory.repository.InventoryRepository;
import com.retailforge.inventory.repository.StockTransactionRepository;
import com.retailforge.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockTransactionRepository transactionRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventoryService inventoryService;

    private Warehouse warehouseA;
    private Warehouse warehouseB;
    private Inventory inventory;

    @BeforeEach
    public void setup() {
        warehouseA = new Warehouse();
        warehouseA.setId(1L);
        warehouseA.setName("Central Hub");
        warehouseA.setLocation("New York");

        warehouseB = new Warehouse();
        warehouseB.setId(2L);
        warehouseB.setName("Retail Store Outlet");
        warehouseB.setLocation("Boston");

        inventory = new Inventory();
        inventory.setId(10L);
        inventory.setProductId(100L);
        inventory.setWarehouseId(1L);
        inventory.setQuantity(50);
        inventory.setVersion(1);
    }

    @Test
    public void testCreateWarehouse_Success() {
        WarehouseRequest request = new WarehouseRequest("Central Hub", "New York");
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouseA);

        WarehouseResponse response = inventoryService.createWarehouse(request);

        assertNotNull(response);
        assertEquals(warehouseA.getId(), response.id());
        assertEquals(warehouseA.getName(), response.name());
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    public void testGetAllWarehouses() {
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouseA, warehouseB));

        List<WarehouseResponse> responses = inventoryService.getAllWarehouses();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Central Hub", responses.get(0).name());
        assertEquals("Retail Store Outlet", responses.get(1).name());
    }

    @Test
    public void testAddStock_Success_ExistingInventory() {
        AddStockRequest request = new AddStockRequest(100L, 1L, 20);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouseA));
        when(inventoryRepository.findByProductIdAndWarehouseId(100L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryResponse response = inventoryService.addStock(request);

        assertNotNull(response);
        assertEquals(70, inventory.getQuantity()); // 50 + 20
        verify(inventoryRepository, times(1)).save(inventory);
        verify(transactionRepository, times(1)).save(any(StockTransaction.class));
        verify(eventPublisher, times(1)).publishStockUpdated(any(StockUpdatedEvent.class));
    }

    @Test
    public void testAddStock_Success_NewInventory() {
        AddStockRequest request = new AddStockRequest(100L, 1L, 20);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouseA));
        when(inventoryRepository.findByProductIdAndWarehouseId(100L, 1L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.addStock(request);

        assertNotNull(response);
        assertEquals(100L, response.productId());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(transactionRepository, times(1)).save(any(StockTransaction.class));
    }

    @Test
    public void testAddStock_WarehouseNotFound() {
        AddStockRequest request = new AddStockRequest(100L, 99L, 20);
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> inventoryService.addStock(request));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    public void testAddStock_InvalidQuantity() {
        AddStockRequest request = new AddStockRequest(100L, 1L, 0);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouseA));

        assertThrows(InvalidQuantityException.class, () -> inventoryService.addStock(request));
    }

    @Test
    public void testReduceStock_Success() {
        when(inventoryRepository.findByProductIdAndWarehouseId(100L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryResponse response = inventoryService.reduceStock(100L, 1L, 10);

        assertNotNull(response);
        assertEquals(40, inventory.getQuantity()); // 50 - 10
        verify(inventoryRepository, times(1)).save(inventory);
        verify(transactionRepository, times(1)).save(any(StockTransaction.class));
        verify(eventPublisher, times(1)).publishStockUpdated(any(StockUpdatedEvent.class));
    }

    @Test
    public void testReduceStock_InsufficientStock() {
        when(inventoryRepository.findByProductIdAndWarehouseId(100L, 1L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.reduceStock(100L, 1L, 100));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    public void testReduceStock_LowStockAlertCheck() {
        inventory.setQuantity(12);
        when(inventoryRepository.findByProductIdAndWarehouseId(100L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // Deduct 5, new quantity will be 7, which is < LOW_STOCK_THRESHOLD (10)
        inventoryService.reduceStock(100L, 1L, 5);

        verify(eventPublisher, times(1)).publishLowStockAlert(any(LowStockAlertEvent.class));
    }

    @Test
    public void testTransferStock_Success() {
        TransferStockRequest request = new TransferStockRequest(100L, 1L, 2L, 10);
        
        Inventory destInventory = new Inventory();
        destInventory.setId(11L);
        destInventory.setProductId(100L);
        destInventory.setWarehouseId(2L);
        destInventory.setQuantity(5);
        destInventory.setVersion(1);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouseA));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouseB));
        when(inventoryRepository.findByProductIdAndWarehouseId(100L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.findByProductIdAndWarehouseId(100L, 2L)).thenReturn(Optional.of(destInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<InventoryResponse> responses = inventoryService.transferStock(request);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(40, inventory.getQuantity()); // 50 - 10
        assertEquals(15, destInventory.getQuantity()); // 5 + 10
        verify(inventoryRepository, times(2)).save(any(Inventory.class));
        verify(transactionRepository, times(2)).save(any(StockTransaction.class));
        verify(eventPublisher, times(2)).publishStockUpdated(any(StockUpdatedEvent.class));
    }

    @Test
    public void testTransferStock_SameWarehouseError() {
        TransferStockRequest request = new TransferStockRequest(100L, 1L, 1L, 10);

        assertThrows(InvalidWarehouseTransferException.class, () -> inventoryService.transferStock(request));
    }

    @Test
    public void testTransferStock_InsufficientStock() {
        TransferStockRequest request = new TransferStockRequest(100L, 1L, 2L, 100);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouseA));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouseB));
        when(inventoryRepository.findByProductIdAndWarehouseId(100L, 1L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.transferStock(request));
    }
}
