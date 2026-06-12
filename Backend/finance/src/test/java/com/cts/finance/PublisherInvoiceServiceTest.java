package com.cts.finance;

import com.cts.finance.billing.dto.PublisherInvoiceResponse;
import com.cts.finance.billing.dto.ReconciliationResultResponse;
import com.cts.finance.billing.dto.SubmitPublisherInvoiceRequest;
import com.cts.finance.billing.entity.PublisherInvoice;
import com.cts.finance.billing.enums.PublisherInvoiceStatus;
import com.cts.finance.billing.repository.PublisherInvoiceRepository;
import com.cts.finance.billing.service.PublisherInvoiceService;
import com.cts.finance.shared.AuditLogService;
import com.cts.finance.shared.BudgetCalculationService;
import com.cts.finance.shared.StatusTransitionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublisherInvoiceServiceTest {

    @Mock PublisherInvoiceRepository repository;
    @Mock StatusTransitionValidator statusValidator;
    @Mock AuditLogService auditLog;
    @Mock BudgetCalculationService budgetCalc;

    @InjectMocks
    PublisherInvoiceService service;

    private PublisherInvoice received(BigDecimal invoiceAmount) {
        return PublisherInvoice.builder()
                .id(10L).publisherId(5L).ioId(9L)
                .invoiceAmount(invoiceAmount)
                .status(PublisherInvoiceStatus.RECEIVED)
                .build();
    }

    @Test
    void submit_setsReceivedAndAudits() {
        when(repository.save(any(PublisherInvoice.class))).thenAnswer(inv -> {
            PublisherInvoice p = inv.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
        var req = new SubmitPublisherInvoiceRequest(5L, 9L, new BigDecimal("1000"), null);

        PublisherInvoiceResponse resp = service.submit(req, 5L);

        assertEquals("RECEIVED", resp.status());
        assertEquals(0, resp.deliveredValue().compareTo(BigDecimal.ZERO));
        assertEquals(0, resp.varianceAmount().compareTo(BigDecimal.ZERO));
        assertNotNull(resp.receivedDate());   // defaulted to today
        verify(auditLog).log(eq(5L), eq("PUBLISHER_INVOICE_RECEIVED"), eq("PublisherInvoice"), any());
    }

    @Test
    void reconcile_exactMatch_reconciled() {
        when(repository.findById(10L)).thenReturn(Optional.of(received(new BigDecimal("1000.00"))));
        when(repository.save(any(PublisherInvoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(budgetCalc.deliveredValueForInsertionOrder(9L)).thenReturn(new BigDecimal("1000.00"));

        ReconciliationResultResponse resp = service.reconcile(10L, 42L);

        assertEquals("RECONCILED", resp.status());
        assertEquals(0, resp.varianceAmount().compareTo(BigDecimal.ZERO));
        verify(statusValidator).validate(PublisherInvoiceStatus.RECEIVED, PublisherInvoiceStatus.RECONCILED);
        verify(auditLog).log(eq(42L), eq("PUBLISHER_INVOICE_RECONCILED"), eq("PublisherInvoice"), eq(10L));
    }

    @Test
    void reconcile_materialVariance_discrepancy() {
        when(repository.findById(10L)).thenReturn(Optional.of(received(new BigDecimal("1000.00"))));
        when(repository.save(any(PublisherInvoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(budgetCalc.deliveredValueForInsertionOrder(9L)).thenReturn(new BigDecimal("950.00"));

        ReconciliationResultResponse resp = service.reconcile(10L, 1L);

        assertEquals("DISCREPANCY", resp.status());
        assertEquals(0, resp.varianceAmount().compareTo(new BigDecimal("50.00")));
        assertEquals(0, resp.deliveredValue().compareTo(new BigDecimal("950.00")));
    }

    @Test
    void reconcile_withinTolerance_reconciled() {
        // variance of exactly 0.01 is within RECONCILE_TOLERANCE -> RECONCILED
        when(repository.findById(10L)).thenReturn(Optional.of(received(new BigDecimal("1000.00"))));
        when(repository.save(any(PublisherInvoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(budgetCalc.deliveredValueForInsertionOrder(9L)).thenReturn(new BigDecimal("999.99"));

        ReconciliationResultResponse resp = service.reconcile(10L, 1L);

        assertEquals("RECONCILED", resp.status());
        assertEquals(0, resp.varianceAmount().compareTo(new BigDecimal("0.01")));
    }
}
