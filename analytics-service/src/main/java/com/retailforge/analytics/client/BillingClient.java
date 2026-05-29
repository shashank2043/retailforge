package com.retailforge.analytics.client;

import com.retailforge.analytics.config.FeignClientConfig;
import com.retailforge.api.response.ApiResponse;
import com.retailforge.analytics.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "billing-service", fallback = BillingClientFallback.class,configuration = FeignClientConfig.class)
public interface BillingClient {

    @GetMapping("/orders/{id}")
    ApiResponse<OrderResponse> getOrderDetails(@PathVariable("id") Long id);
}
