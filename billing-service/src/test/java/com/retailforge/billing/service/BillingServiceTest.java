package com.retailforge.billing.service;

import com.retailforge.billing.client.ProductClient;
import com.retailforge.billing.dto.*;
import com.retailforge.billing.event.OrderCreatedEvent;
import com.retailforge.billing.event.PaymentCompletedEvent;
import com.retailforge.billing.exception.EmptyCartException;
import com.retailforge.billing.exception.OrderNotFoundException;
import com.retailforge.billing.exception.ProductNotFoundException;
import com.retailforge.billing.model.Invoice;
import com.retailforge.billing.model.Order;
import com.retailforge.billing.model.OrderItem;
import com.retailforge.billing.model.Payment;
import com.retailforge.billing.repository.InvoiceRepository;
import com.retailforge.billing.repository.OrderRepository;
import com.retailforge.billing.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BillingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private BillingEventPublisher eventPublisher;

    @InjectMocks
    private BillingService billingService;

    private ProductDto productDto;
    private Order order;
    private Payment payment;

    @BeforeEach
    public void setup() {
        // Inject string values since they are annotated with @Value in BillingService
        ReflectionTestUtils.setField(billingService, "businessName", "RetailForge");
        ReflectionTestUtils.setField(billingService, "businessAddress", "Gachibowli, Hyderabad");
        ReflectionTestUtils.setField(billingService, "businessGstin", "36AAAAA1111A1Z1");
        ReflectionTestUtils.setField(billingService, "businessPhone", "+91-40-12345678");

        productDto = new ProductDto(100L, "9876543210", "Mango Juice", BigDecimal.valueOf(2.50), BigDecimal.valueOf(18.0), new CategoryDto(1L, "Beverages", "Drinks"));

        order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-12345");
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.valueOf(2.95)); // 2.50 + 18% GST (0.45) = 2.95
        order.setItems(new ArrayList<>());

        OrderItem item = new OrderItem();
        item.setId(10L);
        item.setOrder(order);
        item.setProductId(100L);
        item.setQuantity(1);
        item.setPrice(BigDecimal.valueOf(2.50));
        item.setGstAmount(BigDecimal.valueOf(0.45));
        order.getItems().add(item);

        payment = new Payment();
        payment.setId(20L);
        payment.setOrder(order);
        payment.setAmount(BigDecimal.valueOf(2.95));
        payment.setMethod("CASH");
        payment.setStatus("SUCCESS");
        payment.setTransactionId("TXN-987654321");
    }

    @Test
    public void testCheckout_Success() {
        CheckoutRequest request = new CheckoutRequest("CASH", List.of(new CartItemRequest("9876543210", 1)));
        when(productClient.getProductByBarcode("9876543210")).thenReturn(productDto);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        CheckoutResponse response = billingService.checkout(request);

        assertNotNull(response);
        assertEquals(1L, response.orderId());
        assertEquals("PENDING", response.status());
        assertEquals(BigDecimal.valueOf(2.95), response.totalAmount());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, times(1)).publishOrderCreated(any(OrderCreatedEvent.class));
    }

    @Test
    public void testCheckout_EmptyCart() {
        CheckoutRequest request = new CheckoutRequest("CASH", Collections.emptyList());

        assertThrows(EmptyCartException.class, () -> billingService.checkout(request));
    }

    @Test
    public void testCheckout_ProductNotFound() {
        CheckoutRequest request = new CheckoutRequest("CASH", List.of(new CartItemRequest("9876543210", 1)));
        when(productClient.getProductByBarcode("9876543210")).thenReturn(null);

        assertThrows(ProductNotFoundException.class, () -> billingService.checkout(request));
    }

    @Test
    public void testCompleteOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productClient.getProductById(100L)).thenReturn(productDto);

        billingService.completeOrder(1L, "CASH");

        assertEquals("COMPLETED", order.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
        verify(eventPublisher, times(1)).publishPaymentCompleted(any(PaymentCompletedEvent.class));
    }

    @Test
    public void testCompleteOrder_OrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> billingService.completeOrder(99L, "CASH"));
    }

    @Test
    public void testCancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        billingService.cancelOrder(1L, "Customer changed mind");

        assertEquals("CANCELLED", order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void testGetOrderDetails() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = billingService.getOrderDetails(1L);

        assertNotNull(response);
        assertEquals(order.getId(), response.id());
        assertEquals("PENDING", response.status());
        assertEquals(1, response.items().size());
        assertEquals(100L, response.items().get(0).productId());
    }
}
