package com.retailforge.analytics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailforge.analytics.dto.DashboardResponse;
import com.retailforge.analytics.service.AnalyticsService;
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
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AnalyticsControllerTest.TestSecurityConfig.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

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

    private DashboardResponse dashboardResponse;

    @BeforeEach
    public void setup() {
        dashboardResponse = new DashboardResponse(LocalDate.now(), BigDecimal.valueOf(159.00), BigDecimal.valueOf(13.50), BigDecimal.valueOf(13.50), 6L);
    }

    @Test
    public void testGetDashboardMetrics_Success() throws Exception {
        when(analyticsService.getDashboardMetrics()).thenReturn(dashboardResponse);

        mockMvc.perform(get("/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Analytics dashboard metrics loaded successfully."))
                .andExpect(jsonPath("$.data.totalRevenue").value(159.00))
                .andExpect(jsonPath("$.data.transactionCount").value(6));
    }
}
