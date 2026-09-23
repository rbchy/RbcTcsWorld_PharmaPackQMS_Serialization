package com.pharmapack.qms.batch;
import com.pharmapack.qms.product.Product; import jakarta.persistence.*; import jakarta.validation.constraints.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="batches") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Batch { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @NotBlank @Column(name="batch_number",nullable=false,unique=true) String batchNumber;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id",nullable=false) Product product; @NotNull @Positive @Column(name="lot_size",nullable=false) BigDecimal lotSize;
 @Enumerated(EnumType.STRING) @Column(name="batch_status",nullable=false) @Builder.Default BatchStatus batchStatus=BatchStatus.CREATED; LocalDate manufacturingDate; LocalDate expiryDate; }
