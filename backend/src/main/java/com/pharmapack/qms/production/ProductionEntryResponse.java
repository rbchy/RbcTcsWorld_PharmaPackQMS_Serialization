package com.pharmapack.qms.production; import java.math.BigDecimal; import java.time.LocalDateTime;
public record ProductionEntryResponse(Long id,Long productionRunId,Long batchId,String batchNumber,LocalDateTime entryTime,BigDecimal quantityProduced,BigDecimal quantityGood,BigDecimal quantityReject,Long operatorId,String remarks){}
