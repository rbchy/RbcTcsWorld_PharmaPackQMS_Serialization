package com.pharmapack.qms.esignature; import com.pharmapack.qms.auth.User; import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="electronic_signatures") @Getter @Setter @NoArgsConstructor
public class ElectronicSignature { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) User user;
 @Column(name="entity_name",nullable=false) String entityName;
 @Column(name="entity_id",nullable=false) String entityId;
 @Column(name="action_type",nullable=false) String actionType;
 @Column(name="signed_at") LocalDateTime signedAt;
 @Column(name="signature_reason") String signatureReason;
}
