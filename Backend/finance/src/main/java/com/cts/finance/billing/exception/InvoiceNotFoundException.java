package com.cts.finance.billing.exception;

import com.cts.finance.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvoiceNotFoundException extends ApiException {
    public InvoiceNotFoundException(String type, Long id) {
        super(HttpStatus.NOT_FOUND, type + " not found: " + id);
    }
}
