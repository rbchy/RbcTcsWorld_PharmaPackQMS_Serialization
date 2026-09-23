package com.pharmapack.qms.serialization;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "serialization_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SerializationEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serialized_unit_id", nullable = false)
    private SerializedUnit serializedUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private SerializationEventType eventType;

    @Column(name = "event_result", length = 40)
    private String eventResult;

    @Column(name = "reason_code", length = 80)
    private String reasonCode;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @PrePersist
    void prePersist() {
        if (eventTime == null) eventTime = LocalDateTime.now();
    }
}
