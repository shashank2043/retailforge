package com.retailforge.billing.client;

import com.retailforge.billing.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/products/barcode/{barcode}")
    ProductDto getProductByBarcode(@PathVariable("barcode") String barcode);

    @GetMapping("/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);
}
