package com.retailforge.product.controller;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.dto.CategoryDto;
import com.retailforge.dto.ProductDto;
import com.retailforge.product.dto.CategoryRequest;
import com.retailforge.product.dto.ProductRequest;
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
    public ResponseEntity<ApiResponse<ProductDto>> addProduct(@Valid @RequestBody ProductRequest request) {
        ProductDto response = productService.createProduct(request);
        ApiResponse<ProductDto> apiResponse = new ApiResponse<>(true, "Product added successfully.", response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ProductDto>> getProductByBarcode(@PathVariable String barcode) {
        ProductDto response = productService.getProductByBarcode(barcode);
        ApiResponse<ProductDto> apiResponse = new ApiResponse<>(true, "Product barcode query successful.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        List<ProductDto> response = productService.getAllProducts();
        ApiResponse<List<ProductDto>> apiResponse = new ApiResponse<>(true, "All products retrieved successfully.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<CategoryDto>> addCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryDto response = productService.createCategory(request);
        ApiResponse<CategoryDto> apiResponse = new ApiResponse<>(true, "Category created successfully.", response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> response = productService.getAllCategories();
        ApiResponse<List<CategoryDto>> apiResponse = new ApiResponse<>(true, "All categories retrieved successfully.", response);
        return ResponseEntity.ok(apiResponse);
    }
}
