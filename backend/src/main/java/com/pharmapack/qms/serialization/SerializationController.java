package com.pharmapack.qms.serialization;

import com.pharmapack.qms.auth.CurrentUser;
import com.pharmapack.qms.batch.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

/**
 * Unit-level serialization and hierarchical aggregation (UNIT under CASE under PALLET), in
 * the spirit of the DSCSA (US) / EU-FMD serialization &amp; aggregation capability that
 * global pharma packaging providers such as Sharp publicly advertise on their own website
 * (sharpservices.com names "Serialization and Aggregation Solutions" as a core capability
 * area). This module implements the widely-documented industry-standard three-tier
 * aggregation model using this project's own batch data - it does not reproduce any
 * vendor's proprietary software or internal implementation.
 */
@RestController
@RequestMapping("/api/serialization")
public class SerializationController {

    static final List<String> LEVELS = List.of("UNIT", "CASE", "PALLET");

    final SerializedUnitRepository sur;
    final BatchRepository br;
    final SerializationEventRepository er;

    public SerializationController(SerializedUnitRepository s, BatchRepository b, SerializationEventRepository er) {
        this.sur = s;
        this.br = b;
        this.er = er;
    }

    @GetMapping("/batch/{batchId}")
    public List<SerializedUnitResponse> byBatch(@PathVariable Long batchId) {
        return sur.findByBatchIdOrderByIdAsc(batchId).stream()
                .map(this::dto)
                .toList();
    }

    @GetMapping("/{serialNumber}")
    public SerializedUnitResponse get(@PathVariable String serialNumber) {
        return dto(sur.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("Serial number not found: " + serialNumber)));
    }

    @GetMapping("/{serialNumber}/children")
    public List<SerializedUnitResponse> children(@PathVariable String serialNumber) {
        SerializedUnit p = sur.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("Serial number not found: " + serialNumber));
        return sur.findByParentId(p.getId()).stream()
                .map(this::dto)
                .toList();
    }

    public record Commission(Long batchId, Integer quantity, String aggregationLevel, String gtin) {}

    @PostMapping("/commission")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SerializedUnitResponse> commission(@RequestBody Commission x) {
        // findWithProduct (not findById) so Batch.product is eager-fetched — dto() below reads
        // b.getProduct() after this method's own transaction/session has already closed
        // (spring.jpa.open-in-view=false), so a lazy proxy here would fail with "no session".
        Batch b = br.findWithProduct(x.batchId())
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));

        if (x.quantity() == null || x.quantity() < 1 || x.quantity() > 100000) {
            throw new IllegalArgumentException("Quantity must be between 1 and 100000");
        }

        String level = x.aggregationLevel() == null || x.aggregationLevel().isBlank()
                ? "UNIT"
                : x.aggregationLevel().toUpperCase();
        if (!LEVELS.contains(level)) {
            throw new IllegalArgumentException("aggregationLevel must be one of " + LEVELS);
        }

        long start = sur.countByBatchId(b.getId()) + 1;
        List<SerializedUnit> made = new ArrayList<>();

        for (long i = 0; i < x.quantity(); i++) {
            String sn = String.format("SN-%s-%s-%06d", b.getBatchNumber(), level, start + i);
            SerializedUnit u = SerializedUnit.builder()
                    .batch(b)
                    .serialNumber(sn)
                    .gtin(x.gtin())
                    .aggregationLevel(level)
                    .status("CREATED")
                    .build();
            SerializedUnit saved = sur.save(u);
            made.add(saved);
        }

        for (SerializedUnit u : made) {
            er.save(SerializationEvent.builder()
                    .serializedUnit(u)
                    .eventType(SerializationEventType.CREATED)
                    .eventResult("CREATED")
                    .operatorId(CurrentUser.id())
                    .details("Serial provisioned by line manager simulator")
                    .build());
        }

        return made.stream().map(this::dto).toList();
    }

    public record Aggregate(String parentSerialNumber, List<String> childSerialNumbers) {}

    @PostMapping("/aggregate")
    public SerializedUnitResponse aggregate(@RequestBody Aggregate x) {
        if (x.parentSerialNumber() == null || x.parentSerialNumber().isBlank()) {
            throw new IllegalArgumentException("parentSerialNumber is required");
        }
        if (x.childSerialNumbers() == null || x.childSerialNumbers().isEmpty()) {
            throw new IllegalArgumentException("At least one child serial number is required");
        }

        SerializedUnit parent = sur.findBySerialNumber(x.parentSerialNumber())
                .orElseThrow(() -> new IllegalArgumentException("Parent serial number not found: " + x.parentSerialNumber()));
        int parentRank = LEVELS.indexOf(parent.getAggregationLevel());

        for (String sn : x.childSerialNumbers()) {
            SerializedUnit child = sur.findBySerialNumber(sn)
                    .orElseThrow(() -> new IllegalArgumentException("Child serial number not found: " + sn));

            if (!child.getBatch().getId().equals(parent.getBatch().getId())) {
                throw new IllegalArgumentException("Cannot aggregate " + sn + ": different batch than parent");
            }

            int childRank = LEVELS.indexOf(child.getAggregationLevel());
            if (childRank < 0 || parentRank < 0 || childRank >= parentRank) {
                throw new IllegalArgumentException("Cannot aggregate " + sn + " (" + child.getAggregationLevel()
                        + ") under " + x.parentSerialNumber() + " (" + parent.getAggregationLevel()
                        + "); the child's level must be lower than the parent's (UNIT < CASE < PALLET)");
            }

            if (!"COMMISSIONED".equals(child.getStatus())) {
                throw new IllegalArgumentException("Cannot aggregate " + sn + ": status is "
                        + child.getStatus() + ", but only COMMISSIONED serials may be aggregated "
                        + "(complete print -> verify -> commission first)");
            }

            child.setParent(parent);
            child.setStatus("AGGREGATED");
            sur.save(child);

            er.save(SerializationEvent.builder()
                    .serializedUnit(child)
                    .eventType(SerializationEventType.AGGREGATED)
                    .eventResult("PASS")
                    .operatorId(CurrentUser.id())
                    .details("Parent=" + parent.getSerialNumber())
                    .build());
        }

        return dto(sur.findBySerialNumber(x.parentSerialNumber()).orElseThrow());
    }

    SerializedUnitResponse dto(SerializedUnit u) {
        var b = u.getBatch();
        var p = u.getParent();
        return new SerializedUnitResponse(
                u.getId(),
                b.getId(),
                b.getBatchNumber(),
                b.getProduct().getProductCode(),
                u.getSerialNumber(),
                u.getGtin(),
                u.getAggregationLevel(),
                p == null ? null : p.getId(),
                p == null ? null : p.getSerialNumber(),
                u.getStatus(),
                u.getCommissionedAt(),
                u.getCommissionedBy()
        );
    }
}
