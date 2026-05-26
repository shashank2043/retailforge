package com.retailforge.product.controller;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.product.dto.CategoryRequest;
import com.retailforge.product.dto.CategoryResponse;
import com.retailforge.product.dto.ProductRequest;
import com.retailforge.product.dto.ProductResponse;
import com.retailforge.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.addProduct(request);
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(true, "Product added successfully.", response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(true, "Product updated successfully.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductByBarcode(@PathVariable String barcode) {
        ProductResponse response = productService.getProductByBarcode(barcode);
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(true, "Product barcode query successful.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(true, "Product lookup successful.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> response = productService.getAllProducts();
        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>(true, "All products retrieved successfully.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponse>> addCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = productService.addCategory(request);
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>(true, "Category created successfully.", response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> response = productService.getAllCategories();
        ApiResponse<List<CategoryResponse>> apiResponse = new ApiResponse<>(true, "All categories retrieved successfully.", response);
        return ResponseEntity.ok(apiResponse);
    }
}
