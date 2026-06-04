package com.retailforge.billing.client;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.dto.ProductDto;
import com.retailforge.billing.exception.ProductCatalogOfflineException;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ApiResponse<ProductDto> getProductByBarcode(String barcode) {
        throw new ProductCatalogOfflineException("Product catalog service is currently offline or unreachable. Cannot verify item: " + barcode);
    }

    @Override
    public ApiResponse<ProductDto> getProductById(Long id) {
        throw new ProductCatalogOfflineException("Product catalog service is currently offline or unreachable. Cannot verify item ID: " + id);
    }
}
