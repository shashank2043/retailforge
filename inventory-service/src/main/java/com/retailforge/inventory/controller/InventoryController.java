package com.retailforge.inventory.controller;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.inventory.dto.*;
import com.retailforge.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/warehouses")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(@RequestBody WarehouseRequest request) {
        WarehouseResponse response = inventoryService.createWarehouse(request);
        ApiResponse<WarehouseResponse> apiResponse = new ApiResponse<>(true, "Warehouse created successfully.", response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllWarehouses() {
        List<WarehouseResponse> response = inventoryService.getAllWarehouses();
        ApiResponse<List<WarehouseResponse>> apiResponse = new ApiResponse<>(true, "All warehouses retrieved successfully.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<InventoryResponse>> addStock(@RequestBody AddStockRequest request) {
        InventoryResponse response = inventoryService.addStock(request);
        ApiResponse<InventoryResponse> apiResponse = new ApiResponse<>(true, "Stock replenished successfully.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> transferStock(@RequestBody TransferStockRequest request) {
        List<InventoryResponse> response = inventoryService.transferStock(request);
        ApiResponse<List<InventoryResponse>> apiResponse = new ApiResponse<>(true, "Stock transfer completed atomically.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryByProduct(@PathVariable Long productId) {
        List<InventoryResponse> response = inventoryService.getInventoryByProduct(productId);
        ApiResponse<List<InventoryResponse>> apiResponse = new ApiResponse<>(true, "Inventory levels by product retrieved successfully.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByProductAndWarehouse(
            @PathVariable Long productId, @PathVariable Long warehouseId) {
        InventoryResponse response = inventoryService.getInventoryByProductAndWarehouse(productId, warehouseId);
        ApiResponse<InventoryResponse> apiResponse = new ApiResponse<>(true, "Warehouse inventory details loaded.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/transactions/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getTransactionHistoryByWarehouse(@PathVariable Long warehouseId) {
        List<StockTransactionResponse> response = inventoryService.getTransactionHistoryByWarehouse(warehouseId);
        ApiResponse<List<StockTransactionResponse>> apiResponse = new ApiResponse<>(true, "Warehouse transaction history ledger loaded.", response);
        return ResponseEntity.ok(apiResponse);
    }
}
