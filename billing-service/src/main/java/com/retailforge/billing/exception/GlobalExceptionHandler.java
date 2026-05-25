package com.retailforge.billing.exception;

import com.retailforge.api.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({OrderNotFoundException.class, InvoiceNotFoundException.class, ProductNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFoundExceptions(BillingServiceException ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmptyCartException(EmptyCartException ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductCatalogOfflineException.class)
    public ResponseEntity<ApiResponse<Void>> handleCatalogOffline(ProductCatalogOfflineException ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(PdfGenerationException.class)
    public ResponseEntity<ApiResponse<Void>> handlePdfGenerationFailure(PdfGenerationException ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BillingServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseBillingException(BillingServiceException ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
