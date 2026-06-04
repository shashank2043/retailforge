package com.retailforge.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailforge.dto.CategoryDto;
import com.retailforge.dto.ProductDto;
import com.retailforge.product.dto.CategoryRequest;
import com.retailforge.product.dto.ProductRequest;
import com.retailforge.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProductControllerTest.TestSecurityConfig.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private org.springframework.cache.CacheManager cacheManager;

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

    private CategoryDto categoryResponse;
    private ProductDto productResponse;

    @BeforeEach
    public void setup() {
        categoryResponse = new CategoryDto(1L, "Beverages", "Cold drinks");
        productResponse = new ProductDto(10L, "9876543210", "Mango Juice", BigDecimal.valueOf(2.50), BigDecimal.valueOf(18.0), categoryResponse);
    }

    @Test
    public void testAddProduct_Success() throws Exception {
        ProductRequest request = new ProductRequest("9876543210", "Mango Juice", BigDecimal.valueOf(2.50), BigDecimal.valueOf(18.0), 1L);
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product added successfully."))
                .andExpect(jsonPath("$.data.barcode").value("9876543210"))
                .andExpect(jsonPath("$.data.name").value("Mango Juice"));
    }

    @Test
    public void testAddProduct_ValidationError() throws Exception {
        // Name is blank, price is negative - should trigger validation errors
        ProductRequest request = new ProductRequest("", "", BigDecimal.valueOf(-2.50), BigDecimal.valueOf(18.0), 1L);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed")));
    }

    @Test
    public void testGetProductByBarcode_Success() throws Exception {
        String barcode = "9876543210";
        when(productService.getProductByBarcode(barcode)).thenReturn(productResponse);

        mockMvc.perform(get("/products/barcode/" + barcode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.barcode").value(barcode));
    }

    @Test
    public void testAddCategory_Success() throws Exception {
        CategoryRequest request = new CategoryRequest("Beverages", "Cold drinks");
        when(productService.createCategory(any(CategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/products/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Beverages"));
    }
}
