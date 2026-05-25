package com.retailforge.analytics.exception;

public class DailySalesMetricNotFoundException extends RuntimeException {
    public DailySalesMetricNotFoundException(String message) {
        super(message);
    }
}
