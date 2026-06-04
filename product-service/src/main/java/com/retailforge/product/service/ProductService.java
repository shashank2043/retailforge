package com.retailforge.product.service;

import com.retailforge.dto.CategoryDto;
import com.retailforge.dto.ProductDto;
import com.retailforge.product.exception.CategoryNotFoundException;
import com.retailforge.product.exception.ProductAlreadyExistsException;
import com.retailforge.product.exception.ProductNotFoundException;
import com.retailforge.product.model.Category;
import com.retailforge.product.model.Product;
import com.retailforge.product.repository.CategoryRepository;
import com.retailforge.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductDto createProduct(com.retailforge.product.dto.ProductRequest request) {
        if (productRepository.findByBarcode(request.barcode()).isPresent()) {
            throw new ProductAlreadyExistsException("Product already exists with barcode: " + request.barcode());
        }

        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + request.categoryId()));

        Product product = new Product();
        product.setBarcode(request.barcode());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setGstPercentage(request.gstPercentage());
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return mapToProductDto(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#barcode")
    public ProductDto getProductByBarcode(String barcode) {
        log.info("Fetching product details from database for barcode: {}", barcode);
        Product product = productRepository.findByBarcode(barcode)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with barcode: " + barcode));
        return mapToProductDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::mapToProductDto)
            .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "products", key = "#barcode")
    public void deleteProduct(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with barcode: " + barcode));
        productRepository.delete(product);
    }

    @Transactional
    public CategoryDto createCategory(com.retailforge.product.dto.CategoryRequest request) {
        if (categoryRepository.findByName(request.name()).isPresent()) {
            throw new com.retailforge.product.exception.CategoryAlreadyExistsException("Category already exists with name: " + request.name());
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());

        Category saved = categoryRepository.save(category);
        return mapToCategoryDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(this::mapToCategoryDto)
            .collect(Collectors.toList());
    }

    private CategoryDto mapToCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName(), category.getDescription());
    }

    private ProductDto mapToProductDto(Product product) {
        CategoryDto categoryResponse = null;
        if (product.getCategory() != null) {
            categoryResponse = mapToCategoryDto(product.getCategory());
        }
        return new ProductDto(
            product.getId(),
            product.getBarcode(),
            product.getName(),
            product.getPrice(),
            product.getGstPercentage(),
            categoryResponse
        );
    }
}
