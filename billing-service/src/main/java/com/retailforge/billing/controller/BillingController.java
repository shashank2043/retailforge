package com.retailforge.billing.controller;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.billing.dto.CheckoutRequest;
import com.retailforge.billing.dto.CheckoutResponse;
import com.retailforge.billing.dto.OrderResponse;
import com.retailforge.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(@Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = billingService.checkout(request);
        ApiResponse<CheckoutResponse> apiResponse = new ApiResponse<>(true, "Checkout transaction completed successfully.", response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(@PathVariable Long id) {
        OrderResponse response = billingService.getOrderDetails(id);
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>(true, "Order metadata query successful.", response);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Download Invoice PDF", description = "Retrieves the generated PDF invoice for a specific order.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "A PDF file download", 
        content = @Content(
            mediaType = "application/pdf", 
            schema = @Schema(type = "string", format = "binary")
        )
    )
    @GetMapping(value = "/{id}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN')")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id) {
        byte[] pdfBytes = billingService.getInvoicePdfBytes(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice-" + id + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
