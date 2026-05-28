package com.retailforge.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailforge.billing.dto.*;
import com.retailforge.billing.service.BillingService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BillingControllerTest.TestSecurityConfig.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
public class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BillingService billingService;

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

    private CheckoutResponse checkoutResponse;
    private OrderResponse orderResponse;

    @BeforeEach
    public void setup() {
        checkoutResponse = new CheckoutResponse(1L, "ORD-12345", BigDecimal.valueOf(2.95), "PENDING", "PENDING", null, null);
        orderResponse = new OrderResponse(1L, "ORD-12345", BigDecimal.valueOf(2.95), "PENDING", LocalDateTime.now(), List.of(
            new OrderItemResponse(10L, 100L, "Mango Juice", 1, BigDecimal.valueOf(2.50), BigDecimal.valueOf(0.45))
        ));
    }

    @Test
    public void testCheckout_Success() throws Exception {
        CheckoutRequest request = new CheckoutRequest("CASH", List.of(new CartItemRequest("9876543210", 1)));
        when(billingService.checkout(any(CheckoutRequest.class))).thenReturn(checkoutResponse);

        mockMvc.perform(post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Checkout transaction completed successfully."))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-12345"));
    }

    @Test
    public void testCheckout_ValidationError() throws Exception {
        CheckoutRequest request = new CheckoutRequest("", Collections.emptyList()); // Empty fields

        mockMvc.perform(post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed")));
    }

    @Test
    public void testGetOrderDetails_Success() throws Exception {
        when(billingService.getOrderDetails(1L)).thenReturn(orderResponse);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-12345"));
    }

    @Test
    public void testGetInvoicePdf_Success() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3, 4};
        when(billingService.getInvoicePdfBytes(1L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/orders/1/invoice"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdfBytes));
    }
}
