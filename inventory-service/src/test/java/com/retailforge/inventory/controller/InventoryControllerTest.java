package com.retailforge.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailforge.inventory.dto.*;
import com.retailforge.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(InventoryControllerTest.TestSecurityConfig.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @TestConfiguration
    @EnableWebSecurity
    public static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    private WarehouseResponse warehouseResponse;
    private InventoryResponse inventoryResponse;

    @BeforeEach
    public void setup() {
        warehouseResponse = new WarehouseResponse(1L, "Central Hub", "New York");
        inventoryResponse = new InventoryResponse(10L, 100L, 1L, 50, 1);
    }

    @Test
    public void testCreateWarehouse_Success() throws Exception {
        WarehouseRequest request = new WarehouseRequest("Central Hub", "New York");
        when(inventoryService.createWarehouse(any(WarehouseRequest.class))).thenReturn(warehouseResponse);

        mockMvc.perform(post("/inventory/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Warehouse created successfully."))
                .andExpect(jsonPath("$.data.name").value("Central Hub"));
    }

    @Test
    public void testCreateWarehouse_ValidationError() throws Exception {
        WarehouseRequest request = new WarehouseRequest("", ""); // blank values

        mockMvc.perform(post("/inventory/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed")));
    }

    @Test
    public void testAddStock_Success() throws Exception {
        AddStockRequest request = new AddStockRequest(100L, 1L, 20);
        when(inventoryService.addStock(any(AddStockRequest.class))).thenReturn(inventoryResponse);

        mockMvc.perform(post("/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantity").value(50));
    }

    @Test
    public void testAddStock_ValidationError() throws Exception {
        AddStockRequest request = new AddStockRequest(null, 1L, 0); // null product, invalid qty

        mockMvc.perform(post("/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed")));
    }

    @Test
    public void testTransferStock_Success() throws Exception {
        TransferStockRequest request = new TransferStockRequest(100L, 1L, 2L, 10);
        InventoryResponse sourceResponse = new InventoryResponse(10L, 100L, 1L, 40, 1);
        InventoryResponse destResponse = new InventoryResponse(11L, 100L, 2L, 15, 1);
        
        when(inventoryService.transferStock(any(TransferStockRequest.class))).thenReturn(List.of(sourceResponse, destResponse));

        mockMvc.perform(post("/inventory/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock transfer completed atomically."))
                .andExpect(jsonPath("$.data[0].quantity").value(40))
                .andExpect(jsonPath("$.data[1].quantity").value(15));
    }

    @Test
    public void testGetInventoryByProduct() throws Exception {
        when(inventoryService.getInventoryByProduct(100L)).thenReturn(List.of(inventoryResponse));

        mockMvc.perform(get("/inventory/product/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].productId").value(100L));
    }

    @Test
    public void testGetTransactionHistoryByWarehouse() throws Exception {
        StockTransactionResponse transactionResponse = new StockTransactionResponse(1L, 100L, 1L, 20, "ADDITION", LocalDateTime.now());
        when(inventoryService.getTransactionHistoryByWarehouse(1L)).thenReturn(List.of(transactionResponse));

        mockMvc.perform(get("/inventory/transactions/warehouse/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("ADDITION"));
    }

    @Test
    public void testGetProductsByWarehouse() throws Exception {
        when(inventoryService.getProductsByWarehouse(1L)).thenReturn(List.of(inventoryResponse));

        mockMvc.perform(get("/inventory/warehouse/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All products retrieved successfully."))
                .andExpect(jsonPath("$.data[0].productId").value(100L));
    }
}
