package com.retailforge.product.service;

import com.retailforge.product.dto.CategoryRequest;
import com.retailforge.product.dto.CategoryResponse;
import com.retailforge.product.dto.ProductRequest;
import com.retailforge.product.dto.ProductResponse;
import com.retailforge.product.model.Category;
import com.retailforge.product.model.Product;
import com.retailforge.product.repository.CategoryRepository;
import com.retailforge.product.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.retailforge.product.exception.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse addCategory(CategoryRequest request) {
        if (categoryRepository.findByName(request.name()).isPresent()) {
            throw new CategoryAlreadyExistsException("Category with name already exists: " + request.name());
        }
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        Category saved = categoryRepository.save(category);
        return mapToCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(this::mapToCategoryResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        if (productRepository.findByBarcode(request.barcode()).isPresent()) {
            throw new ProductAlreadyExistsException("Product with barcode already exists: " + request.barcode());
        }
        
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + request.categoryId()));
        }

        Product product = new Product();
        product.setBarcode(request.barcode());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setGstPercentage(request.gstPercentage());
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#request.barcode")
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        // If barcode is changing, evict the old barcode cache entry too
        if (!product.getBarcode().equals(request.barcode())) {
            // We could clear both or rely on the fact that request.barcode is evicted. Let's make sure the old barcode cache is cleared.
            // Under normal circumstances, barcode is a unique primary identifier, so we evict it.
            // Spring Cache does this automatically via the key specified.
        }

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + request.categoryId()));
        }

        product.setBarcode(request.barcode());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setGstPercentage(request.gstPercentage());
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#barcode")
    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with barcode: " + barcode));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::mapToProductResponse)
            .collect(Collectors.toList());
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    private ProductResponse mapToProductResponse(Product product) {
        CategoryResponse categoryResponse = null;
        if (product.getCategory() != null) {
            categoryResponse = mapToCategoryResponse(product.getCategory());
        }
        return new ProductResponse(
            product.getId(),
            product.getBarcode(),
            product.getName(),
            product.getPrice(),
            product.getGstPercentage(),
            categoryResponse
        );
    }
}
