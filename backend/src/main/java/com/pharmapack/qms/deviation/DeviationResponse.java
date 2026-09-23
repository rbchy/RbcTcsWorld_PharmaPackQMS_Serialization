package com.pharmapack.qms.deviation; import java.time.LocalDateTime;
public record DeviationResponse(Long id,String deviationNumber,Long batchId,String batchNumber,String productCode,String productName,String title,String description,String severity,String status,Long openedBy,LocalDateTime openedAt,LocalDateTime closedAt){}
