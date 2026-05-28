package com.retailforge.product.service;

import com.retailforge.product.dto.CategoryRequest;
import com.retailforge.product.dto.CategoryResponse;
import com.retailforge.product.dto.ProductRequest;
import com.retailforge.product.dto.ProductResponse;
import com.retailforge.product.exception.CategoryAlreadyExistsException;
import com.retailforge.product.exception.CategoryNotFoundException;
import com.retailforge.product.exception.ProductAlreadyExistsException;
import com.retailforge.product.exception.ProductNotFoundException;
import com.retailforge.product.model.Category;
import com.retailforge.product.model.Product;
import com.retailforge.product.repository.CategoryRepository;
import com.retailforge.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;

    @BeforeEach
    public void setup() {
        category = new Category();
        category.setId(1L);
        category.setName("Beverages");
        category.setDescription("Cold drinks and juices");

        product = new Product();
        product.setId(10L);
        product.setBarcode("9876543210");
        product.setName("Mango Juice");
        product.setPrice(BigDecimal.valueOf(2.50));
        product.setGstPercentage(BigDecimal.valueOf(18.0));
        product.setCategory(category);
    }

    @Test
    public void testAddCategory_Success() {
        CategoryRequest request = new CategoryRequest("Beverages", "Cold drinks and juices");
        when(categoryRepository.findByName(request.name())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = productService.addCategory(request);

        assertNotNull(response);
        assertEquals(category.getId(), response.id());
        assertEquals(category.getName(), response.name());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void testAddCategory_AlreadyExists() {
        CategoryRequest request = new CategoryRequest("Beverages", "Cold drinks and juices");
        when(categoryRepository.findByName(request.name())).thenReturn(Optional.of(category));

        assertThrows(CategoryAlreadyExistsException.class, () -> productService.addCategory(request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    public void testAddProduct_Success() {
        ProductRequest request = new ProductRequest("9876543210", "Mango Juice", BigDecimal.valueOf(2.50), BigDecimal.valueOf(18.0), 1L);
        when(productRepository.findByBarcode(request.barcode())).thenReturn(Optional.empty());
        when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.addProduct(request);

        assertNotNull(response);
        assertEquals(product.getId(), response.id());
        assertEquals(product.getBarcode(), response.barcode());
        assertEquals(category.getName(), response.category().name());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testAddProduct_BarcodeConflict() {
        ProductRequest request = new ProductRequest("9876543210", "Mango Juice", BigDecimal.valueOf(2.50), BigDecimal.valueOf(18.0), 1L);
        when(productRepository.findByBarcode(request.barcode())).thenReturn(Optional.of(product));

        assertThrows(ProductAlreadyExistsException.class, () -> productService.addProduct(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testAddProduct_CategoryNotFound() {
        ProductRequest request = new ProductRequest("9876543210", "Mango Juice", BigDecimal.valueOf(2.50), BigDecimal.valueOf(18.0), 1L);
        when(productRepository.findByBarcode(request.barcode())).thenReturn(Optional.empty());
        when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> productService.addProduct(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testGetProductByBarcode_Success() {
        String barcode = "9876543210";
        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductByBarcode(barcode);

        assertNotNull(response);
        assertEquals(product.getBarcode(), response.barcode());
        assertEquals(product.getName(), response.name());
    }

    @Test
    public void testGetProductByBarcode_NotFound() {
        String barcode = "1111111111";
        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductByBarcode(barcode));
    }

    @Test
    public void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> list = productService.getAllProducts();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(product.getName(), list.get(0).name());
    }
}
