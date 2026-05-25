package com.retailforge.billing.client;

import com.retailforge.billing.dto.ProductDto;
import com.retailforge.billing.exception.ProductCatalogOfflineException;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductDto getProductByBarcode(String barcode) {
        throw new ProductCatalogOfflineException("Product catalog service is currently offline or unreachable. Cannot verify item: " + barcode);
    }
}
