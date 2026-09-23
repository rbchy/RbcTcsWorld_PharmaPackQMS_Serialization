package com.pharmapack.qms.aql; import java.time.LocalDateTime;
public record AqlInspectionResponse(Long id,Long batchId,String batchNumber,String productCode,String productName,Long planId,String planCode,Long inspectorId,LocalDateTime inspectionTime,Integer sampleSize,Integer defectsFound,String result,String remarks){}
