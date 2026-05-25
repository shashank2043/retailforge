package com.retailforge.analytics.client;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.analytics.dto.OrderResponse;
import com.retailforge.analytics.exception.BillingServiceOfflineException;
import org.springframework.stereotype.Component;

@Component
public class BillingClientFallback implements BillingClient {

    @Override
    public ApiResponse<OrderResponse> getOrderDetails(Long id) {
        throw new BillingServiceOfflineException("POS Billing service is currently offline or unreachable. Cannot retrieve order metadata for aggregation.");
    }
}
