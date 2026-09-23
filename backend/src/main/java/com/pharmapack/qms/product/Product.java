package com.pharmapack.qms.product;
import jakarta.persistence.*; import jakarta.validation.constraints.NotBlank; import lombok.*;
@Entity @Table(name="products") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @NotBlank @Column(name="product_code",nullable=false,unique=true) String productCode;
 @NotBlank @Column(name="product_name",nullable=false) String productName;
 String strength; @Column(name="dosage_form") String dosageForm; @Column(name="pack_size") String packSize;
 @Builder.Default String status="ACTIVE";
}
