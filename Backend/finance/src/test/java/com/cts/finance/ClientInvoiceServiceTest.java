package com.cts.finance;

import com.cts.finance.billing.dto.*;
import com.cts.finance.billing.entity.ClientInvoice;
import com.cts.finance.billing.enums.ClientInvoiceStatus;
import com.cts.finance.billing.exception.BillingRuleException;
import com.cts.finance.billing.repository.ClientInvoiceRepository;
import com.cts.finance.billing.service.ClientInvoiceService;
import com.cts.finance.shared.AuditLogService;
import com.cts.finance.shared.BudgetCalculationService;
import com.cts.finance.shared.StatusTransitionValidator;
import com.cts.finance.shared.exception.IllegalStatusTransitionException;
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
class ClientInvoiceServiceTest {

    @Mock ClientInvoiceRepository repository;
    @Mock StatusTransitionValidator statusValidator;
    @Mock AuditLogService auditLog;
    @Mock BudgetCalculationService budgetCalc;

    @InjectMocks
    ClientInvoiceService service;

    private void stubSaveReturnsWithId() {
        when(repository.save(any(ClientInvoice.class))).thenAnswer(inv -> {
            ClientInvoice c = inv.getArgument(0);
            if (c.getId() == null) c.setId(1L);
            return c;
        });
    }

    @Test
    void create_computesCommissionOnTop() {
        stubSaveReturnsWithId();
        var req = new CreateClientInvoiceRequest(7L, 3L, "2026-05",
                new BigDecimal("1000"), new BigDecimal("0.15"));

        ClientInvoiceResponse resp = service.create(req, 42L);

        assertEquals(new BigDecimal("150.00"), resp.agencyCommission());
        assertEquals(new BigDecimal("1150.00"), resp.netBillable());
        assertEquals("DRAFT", resp.status());
        verify(auditLog).log(eq(42L), eq("CLIENT_INVOICE_CREATED"), eq("ClientInvoice"), any());
    }

    @Test
    void create_usesDefaultCommissionRate_whenNull() {
        stubSaveReturnsWithId();
        var req = new CreateClientInvoiceRequest(7L, null, "2026-05",
                new BigDecimal("1000"), null);

        ClientInvoiceResponse resp = service.create(req, 1L);

        // default rate is 15%
        assertEquals(new BigDecimal("150.00"), resp.agencyCommission());
        assertEquals(new BigDecimal("1150.00"), resp.netBillable());
    }

    @Test
    void generate_computesFromDeliveredSpend() {
        stubSaveReturnsWithId();
        when(budgetCalc.deliveredSpendForCampaign(3L)).thenReturn(new BigDecimal("2000"));
        var req = new GenerateClientInvoiceRequest(7L, 3L, "2026-05", new BigDecimal("0.10"));

        ClientInvoiceResponse resp = service.generate(req, 1L);

        assertEquals(new BigDecimal("2000"), resp.invoiceAmount());
        assertEquals(new BigDecimal("200.00"), resp.agencyCommission());
        assertEquals(new BigDecimal("2200.00"), resp.netBillable());
    }

    @Test
    void generate_throwsWhenNoApprovedDelivery() {
        when(budgetCalc.deliveredSpendForCampaign(3L)).thenReturn(BigDecimal.ZERO);
        var req = new GenerateClientInvoiceRequest(7L, 3L, "2026-05", null);

        assertThrows(BillingRuleException.class, () -> service.generate(req, 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void changeStatus_draftToIssued_setsIssuedDateAndAudits() {
        ClientInvoice draft = ClientInvoice.builder()
                .id(10L).advertiserId(7L).invoiceAmount(new BigDecimal("1000"))
                .status(ClientInvoiceStatus.DRAFT).build();
        when(repository.findById(10L)).thenReturn(Optional.of(draft));
        when(repository.save(any(ClientInvoice.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientInvoiceResponse resp = service.changeStatus(10L, "ISSUED", 42L);

        assertEquals("ISSUED", resp.status());
        assertNotNull(resp.issuedDate());
        verify(statusValidator).validate(ClientInvoiceStatus.DRAFT, ClientInvoiceStatus.ISSUED);
        verify(auditLog).log(eq(42L), eq("CLIENT_INVOICE_STATUS_ISSUED"), eq("ClientInvoice"), eq(10L));
    }

    @Test
    void changeStatus_illegalTransition_propagates() {
        ClientInvoice draft = ClientInvoice.builder()
                .id(10L).status(ClientInvoiceStatus.DRAFT).build();
        when(repository.findById(10L)).thenReturn(Optional.of(draft));
        doThrow(new IllegalStatusTransitionException("ClientInvoice", "DRAFT", "PAID"))
                .when(statusValidator).validate(any(), any());

        assertThrows(IllegalStatusTransitionException.class,
                () -> service.changeStatus(10L, "PAID", 1L));
        verify(repository, never()).save(any());
    }
}
