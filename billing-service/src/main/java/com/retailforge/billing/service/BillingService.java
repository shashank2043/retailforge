package com.retailforge.billing.service;

import com.retailforge.billing.client.ProductClient;
import com.retailforge.billing.dto.*;
import com.retailforge.billing.model.Invoice;
import com.retailforge.billing.model.Order;
import com.retailforge.billing.model.OrderItem;
import com.retailforge.billing.model.Payment;
import com.retailforge.billing.repository.InvoiceRepository;
import com.retailforge.billing.repository.OrderRepository;
import com.retailforge.billing.repository.PaymentRepository;
import com.retailforge.billing.util.InvoicePdfGenerator;
import com.retailforge.billing.exception.*;
import com.retailforge.billing.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductClient productClient;
    private final BillingEventPublisher eventPublisher;

    @Value("${retailforge.business.name:RetailForge}")
    private String businessName;

    @Value("${retailforge.business.address:1-32, Gachibowli, Hyderabad, 500032}")
    private String businessAddress;

    @Value("${retailforge.business.gstin:36AAAAA1111A1Z1}")
    private String businessGstin;

    @Value("${retailforge.business.phone:+91-40-12345678}")
    private String businessPhone;

    private static final String INVOICES_DIR = "./billing-service/invoices/";

    public BillingService(OrderRepository orderRepository,
                          PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          ProductClient productClient,
                          BillingEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.productClient = productClient;
        this.eventPublisher = eventPublisher;
        
        // Ensure invoices directory exists
        try {
            Files.createDirectories(Paths.get(INVOICES_DIR));
        } catch (IOException e) {
            log.error("Failed to create invoices directory", e);
        }
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        log.info("Processing checkout request with method: {}", request.paymentMethod());

        if (request.items() == null || request.items().isEmpty()) {
            throw new EmptyCartException("Cart cannot be empty for checkout.");
        }

        // 1. Create order skeleton in PENDING status
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemEvent> eventItems = new ArrayList<>();

        // 2. Query product catalog and compute GST rates
        for (CartItemRequest itemReq : request.items()) {
            ProductDto product = productClient.getProductByBarcode(itemReq.barcode());
            if (product == null) {
                throw new ProductNotFoundException("Product not found with barcode: " + itemReq.barcode());
            }

            BigDecimal unitPrice = product.price();
            BigDecimal gstPercentage = product.gstPercentage();

            BigDecimal quantity = BigDecimal.valueOf(itemReq.quantity());
            BigDecimal itemSubtotal = unitPrice.multiply(quantity);
            
            // Calculate GST amount: Subtotal * GST% / 100
            BigDecimal itemGstAmount = itemSubtotal.multiply(gstPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BigDecimal lineTotal = itemSubtotal.add(itemGstAmount);
            totalAmount = totalAmount.add(lineTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.id());
            orderItem.setQuantity(itemReq.quantity());
            orderItem.setPrice(unitPrice);
            orderItem.setGstAmount(itemGstAmount);

            order.getItems().add(orderItem);
            eventItems.add(new OrderItemEvent(product.id(), itemReq.quantity()));
        }

        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        Order savedOrder = orderRepository.save(order);

        // 3. Publish Asynchronous ORDER_CREATED event
        eventPublisher.publishOrderCreated(new OrderCreatedEvent(
            savedOrder.getId(),
            savedOrder.getOrderNumber(),
            request.paymentMethod().toUpperCase(),
            eventItems
        ));

        // Return instant response with status PENDING
        return new CheckoutResponse(
            savedOrder.getId(),
            savedOrder.getOrderNumber(),
            savedOrder.getTotalAmount(),
            savedOrder.getStatus(),
            "PENDING", // Payment status PENDING
            null,      // No transaction ID yet
            null       // No invoice number yet
        );
    }

    @Transactional
    public void completeOrder(Long orderId, String paymentMethod) {
        log.info("Completing order ID: {} with payment method: {}", orderId, paymentMethod);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        if (!"PENDING".equals(order.getStatus())) {
            log.warn("Order {} is not in PENDING state. Current status: {}", orderId, order.getStatus());
            return;
        }

        // 1. Simulate Payment Gateway Success
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(paymentMethod.toUpperCase());
        payment.setStatus("SUCCESS");
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        Payment savedPayment = paymentRepository.save(payment);

        order.setStatus("COMPLETED");
        Order savedOrder = orderRepository.save(order);

        // 2. Fetch product catalog details for PDF invoice table
        Map<Long, ProductDto> productMap = new HashMap<>();
        for (OrderItem item : savedOrder.getItems()) {
            try {
                ProductDto product = productClient.getProductById(item.getProductId());
                if (product != null) {
                    productMap.put(product.id(), product);
                }
            } catch (Exception e) {
                log.error("Failed to query product details for ID: {}", item.getProductId(), e);
            }
        }

        // 3. Generate PDF Invoice Receipt
        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        byte[] pdfBytes = InvoicePdfGenerator.generateInvoicePdf(savedOrder, savedPayment, invoiceNumber, productMap,
                businessName, businessAddress, businessGstin, businessPhone);

        String pdfPathStr = INVOICES_DIR + invoiceNumber + ".pdf";
        try {
            Path pdfPath = Paths.get(pdfPathStr);
            Files.write(pdfPath, pdfBytes);
            log.info("Saved invoice PDF to: {}", pdfPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write invoice PDF to disk", e);
            throw new PdfGenerationException("Failed to generate digital invoice PDF");
        }

        // 4. Save Invoice Record
        Invoice invoice = new Invoice();
        invoice.setOrder(savedOrder);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setPdfUrl(pdfPathStr);
        invoiceRepository.save(invoice);

        // 5. Emit PAYMENT_COMPLETED event
        eventPublisher.publishPaymentCompleted(new PaymentCompletedEvent(
            savedOrder.getId(),
            savedOrder.getOrderNumber(),
            savedOrder.getTotalAmount(),
            savedPayment.getTransactionId(),
            LocalDateTime.now()
        ));
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order ID: {}. Reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        if (!"PENDING".equals(order.getStatus())) {
            log.warn("Order {} is not in PENDING state. Current status: {}", orderId, order.getStatus());
            return;
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        List<OrderItemResponse> items = order.getItems().stream()
            .map(item -> new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                "Product ID: " + item.getProductId(),
                item.getQuantity(),
                item.getPrice(),
                item.getGstAmount()
            ))
            .collect(Collectors.toList());

        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getCreatedAt(),
            items
        );
    }

    @Transactional(readOnly = true)
    public byte[] getInvoicePdfBytes(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found for order ID: " + orderId));

        try {
            Path path = Paths.get(invoice.getPdfUrl());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Failed to read invoice PDF from disk path: {}", invoice.getPdfUrl(), e);
            throw new PdfGenerationException("Failed to retrieve digital invoice PDF bytes");
        }
    }
}
