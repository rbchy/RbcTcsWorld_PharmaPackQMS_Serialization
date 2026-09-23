package com.pharmapack.qms.audit; import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="audit_trails") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditTrail { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @Column(nullable=false) String username;
 @Column(name="action_type",nullable=false) String actionType;
 @Column(name="entity_name") String entityName;
 @Column(name="entity_id") String entityId;
 @Column(name="field_name") String fieldName;
 @Column(name="old_value",columnDefinition="TEXT") String oldValue;
 @Column(name="new_value",columnDefinition="TEXT") String newValue;
 @Column(name="created_at") LocalDateTime createdAt;
}
