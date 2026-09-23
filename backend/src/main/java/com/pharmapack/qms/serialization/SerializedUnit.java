package com.pharmapack.qms.serialization; import com.pharmapack.qms.batch.Batch; import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="serialized_units") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SerializedUnit { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="batch_id",nullable=false) Batch batch;
 @Column(name="serial_number",nullable=false,unique=true) String serialNumber;
 String gtin;
 @Column(name="aggregation_level",nullable=false) @Builder.Default String aggregationLevel="UNIT";
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_id") SerializedUnit parent;
 @Column(nullable=false) @Builder.Default String status="COMMISSIONED";
 @Column(name="commissioned_at") LocalDateTime commissionedAt;
 @Column(name="commissioned_by") Long commissionedBy;
}
