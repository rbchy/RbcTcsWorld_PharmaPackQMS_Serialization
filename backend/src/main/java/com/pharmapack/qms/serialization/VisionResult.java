package com.pharmapack.qms.serialization;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vision_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VisionResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serialized_unit_id", nullable = false)
    private SerializedUnit serializedUnit;

    @Column(name = "expected_barcode", length = 120)
    private String expectedBarcode;

    @Column(name = "actual_barcode", length = 120)
    private String actualBarcode;

    @Column(name = "expected_lot", length = 80)
    private String expectedLot;

    @Column(name = "actual_lot", length = 80)
    private String actualLot;

    @Column(name = "expected_expiry", length = 40)
    private String expectedExpiry;

    @Column(name = "actual_expiry", length = 40)
    private String actualExpiry;

    @Column(name = "barcode_pass", nullable = false)
    private boolean barcodePass;

    @Column(name = "lot_pass", nullable = false)
    private boolean lotPass;

    @Column(name = "expiry_pass", nullable = false)
    private boolean expiryPass;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 10)
    private VisionDecision decision;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @PrePersist
    void prePersist() {
        if (checkedAt == null) checkedAt = LocalDateTime.now();
    }
}
