package com.pharmapack.qms.aql;

import com.pharmapack.qms.batch.Batch;
import com.pharmapack.qms.batch.BatchRepository;
import com.pharmapack.qms.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** AQL sampling decision: defects ≤ Ac → ACCEPT, defects ≥ Re → REJECT, anything between → REVIEW. */
class AqlControllerTest {

    private AqlPlanRepository plans;
    private AqlInspectionRepository inspections;
    private AqlController controller;
    private final AtomicReference<AqlInspection> saved = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        plans = mock(AqlPlanRepository.class);
        inspections = mock(AqlInspectionRepository.class);
        BatchRepository batches = mock(BatchRepository.class);
        controller = new AqlController(plans, inspections, batches);

        when(plans.findById(1L)).thenReturn(Optional.of(plan(1L, "AQL-II-1.0", 125, 3, 4)));
        // a plan with a gap between Ac and Re, so REVIEW is reachable
        when(plans.findById(2L)).thenReturn(Optional.of(plan(2L, "AQL-GAP", 80, 1, 3)));
        when(plans.findById(9L)).thenReturn(Optional.empty());

        Product product = Product.builder().id(1L).productCode("PMP-001").productName("Demo tablets").build();
        when(batches.findById(1L)).thenReturn(Optional.of(
                Batch.builder().id(1L).batchNumber("BATCH-DEMO-001").product(product).lotSize(new BigDecimal("10000")).build()));

        when(inspections.save(any())).thenAnswer(inv -> {
            AqlInspection a = inv.getArgument(0);
            a.setId(42L);
            saved.set(a);
            return a;
        });
        when(inspections.findById(42L)).thenAnswer(inv -> Optional.ofNullable(saved.get()));
    }

    private static AqlPlan plan(Long id, String code, int sample, int ac, int re) {
        AqlPlan p = new AqlPlan();
        p.setId(id);
        p.setPlanCode(code);
        p.setSampleSize(sample);
        p.setAcceptanceNumber(ac);
        p.setRejectionNumber(re);
        return p;
    }

    private AqlInspectionResponse inspect(long planId, Integer defects, Integer sample) {
        return controller.create(new AqlController.Create(1L, planId, 1L, sample, defects, "unit test"));
    }

    @ParameterizedTest(name = "AQL-II-1.0 (Ac 3 / Re 4): {0} defects → {1}")
    @CsvSource({"0, ACCEPT", "3, ACCEPT", "4, REJECT", "12, REJECT"})
    void seededPlanAcceptsUpToAcAndRejectsFromRe(int defects, String expected) {
        assertEquals(expected, inspect(1L, defects, 125).result());
    }

    @ParameterizedTest(name = "Ac 1 / Re 3: {0} defects → {1}")
    @CsvSource({"1, ACCEPT", "2, REVIEW", "3, REJECT"})
    void betweenAcAndReNeedsReview(int defects, String expected) {
        assertEquals(expected, inspect(2L, defects, 80).result());
    }

    @Test
    void missingDefectCountMeansZeroAndAccept() {
        AqlInspectionResponse r = inspect(1L, null, 125);
        assertEquals(0, r.defectsFound());
        assertEquals("ACCEPT", r.result());
    }

    @Test
    void missingSampleSizeTakesThePlanSampleSize() {
        assertEquals(125, inspect(1L, 0, null).sampleSize());
    }

    @Test
    void responseCarriesBatchAndPlan() {
        AqlInspectionResponse r = inspect(1L, 1, 125);
        assertEquals("BATCH-DEMO-001", r.batchNumber());
        assertEquals("AQL-II-1.0", r.planCode());
        assertNotNull(r.inspectionTime());
    }

    @Test
    void unknownPlanIsRefusedAndNothingIsSaved() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> inspect(9L, 0, 125));
        assertEquals("AQL plan not found", e.getMessage());
        verify(inspections, never()).save(any());
    }
}
