package com.retailforge.billing.client;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.billing.config.FeignClientConfig;
import com.retailforge.billing.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service",configuration = FeignClientConfig.class)
public interface ProductClient {

    @GetMapping("/products/barcode/{barcode}")
    ApiResponse<ProductDto> getProductByBarcode(@PathVariable("barcode") String barcode);

    @GetMapping("/products/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable("id") Long id);
}
