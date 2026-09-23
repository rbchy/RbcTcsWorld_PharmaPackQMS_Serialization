package com.pharmapack.qms.production;

import com.pharmapack.qms.batch.Batch;
import com.pharmapack.qms.master.Equipment;
import com.pharmapack.qms.master.PackagingLine;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private PackagingLine line;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Builder.Default
    private String status = "PLANNED";
}
