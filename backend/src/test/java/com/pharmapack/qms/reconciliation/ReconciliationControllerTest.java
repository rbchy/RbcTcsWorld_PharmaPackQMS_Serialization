package com.pharmapack.qms.reconciliation;

import com.pharmapack.qms.audit.AuditTrailService;
import com.pharmapack.qms.batch.Batch;
import com.pharmapack.qms.batch.BatchRepository;
import com.pharmapack.qms.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/** Material reconciliation: starting = good + reject + unused, otherwise the batch needs an investigation. */
class ReconciliationControllerTest {

    private ReconciliationRepository reconciliations;
    private BatchRepository batches;
    private AuditTrailService audit;
    private ReconciliationController controller;
    private final AtomicReference<Reconciliation> stored = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        reconciliations = mock(ReconciliationRepository.class);
        batches = mock(BatchRepository.class);
        audit = mock(AuditTrailService.class);
        controller = new ReconciliationController(reconciliations, batches, audit);

        Product product = Product.builder().id(1L).productCode("PMP-001").productName("Demo tablets").build();
        Batch batch = Batch.builder().id(7L).batchNumber("BATCH-DEMO-007").product(product).lotSize(new BigDecimal("1000")).build();
        when(batches.findById(7L)).thenReturn(Optional.of(batch));
        when(batches.findById(99L)).thenReturn(Optional.empty());
        // the repository behaves like a one-row table keyed by batch
        when(reconciliations.findByBatchId(anyLong())).thenAnswer(inv -> Optional.ofNullable(stored.get()));
        when(reconciliations.save(any())).thenAnswer(inv -> {
            Reconciliation r = inv.getArgument(0);
            if (r.getId() == null) r.setId(1L);
            stored.set(r);
            return r;
        });
    }

    private ReconciliationController.Calc calc(String start, String good, String reject, String unused) {
        return new ReconciliationController.Calc(7L, dec(start), dec(good), dec(reject), dec(unused), 1L, "unit test");
    }

    private static BigDecimal dec(String v) {
        return v == null ? null : new BigDecimal(v);
    }

    @Test
    void balancedQuantitiesAreReconciled() {
        ReconciliationResponse r = controller.calculate(calc("100", "80", "10", "10"));

        assertEquals("RECONCILED", r.status());
        assertEquals(0, r.variance().compareTo(BigDecimal.ZERO));
        assertEquals(0, r.reconciledQuantity().compareTo(new BigDecimal("100")));
        assertEquals("BATCH-DEMO-007", r.batchNumber());
        assertEquals("PMP-001", r.productCode());
    }

    @Test
    void aShortfallRequiresInvestigationAndKeepsTheVariance() {
        ReconciliationResponse r = controller.calculate(calc("100", "80", "10", "9"));

        assertEquals("INVESTIGATION_REQUIRED", r.status());
        assertEquals(0, r.variance().compareTo(BigDecimal.ONE), "variance = 100 - 99");
    }

    @Test
    void anOverageAlsoRequiresInvestigation() {
        ReconciliationResponse r = controller.calculate(calc("100", "85", "10", "10"));

        assertEquals("INVESTIGATION_REQUIRED", r.status());
        assertEquals(0, r.variance().compareTo(new BigDecimal("-5")), "more accounted for than issued");
    }

    @Test
    void missingQuantitiesCountAsZero() {
        ReconciliationResponse r = controller.calculate(calc("50", "50", null, null));

        assertEquals("RECONCILED", r.status());
        assertEquals(0, r.rejectQuantity().compareTo(BigDecimal.ZERO));
        assertEquals(0, r.unusedQuantity().compareTo(BigDecimal.ZERO));
    }

    @Test
    void decimalQuantitiesAreComparedExactly() {
        ReconciliationResponse r = controller.calculate(calc("10.50", "10.25", "0.25", "0"));
        assertEquals("RECONCILED", r.status());
    }

    @Test
    void negativeOrMissingStartingQuantityIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> controller.calculate(calc("-1", "0", "0", "0")));
        assertThrows(IllegalArgumentException.class, () -> controller.calculate(calc(null, "0", "0", "0")));
        verify(reconciliations, never()).save(any());
    }

    @Test
    void unknownBatchIsRefused() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> controller.calculate(
                new ReconciliationController.Calc(99L, dec("1"), dec("1"), dec("0"), dec("0"), 1L, null)));
        assertEquals("Batch not found", e.getMessage());
    }

    @Test
    void recalculatingUpdatesTheSameRecord() {
        controller.calculate(calc("100", "80", "10", "9"));
        ReconciliationResponse second = controller.calculate(calc("100", "80", "10", "10"));

        assertEquals(1L, second.id(), "one reconciliation per batch");
        assertEquals("RECONCILED", second.status());
        verify(batches, times(1)).findById(7L);
    }

    @Test
    void everyCalculationIsWrittenToTheAuditTrail() {
        controller.calculate(calc("100", "80", "10", "9"));

        verify(audit).log(eq("CALCULATE"), eq("Reconciliation"), eq(7L), eq("variance"), isNull(),
                eq(new BigDecimal("1")));
    }
}
