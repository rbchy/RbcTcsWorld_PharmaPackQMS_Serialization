package com.pharmapack.qms.batch; import java.math.BigDecimal; import java.time.LocalDate;
public record BatchResponse(Long id,String batchNumber,Long productId,String productCode,String productName,BigDecimal lotSize,BatchStatus batchStatus,LocalDate manufacturingDate,LocalDate expiryDate){}
