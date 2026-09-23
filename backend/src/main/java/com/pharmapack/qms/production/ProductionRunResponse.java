package com.pharmapack.qms.production; import java.time.LocalDateTime;
public record ProductionRunResponse(Long id,Long batchId,String batchNumber,String productCode,String productName,Long lineId,String lineCode,String lineName,Long equipmentId,String equipmentCode,String equipmentName,LocalDateTime startedAt,LocalDateTime endedAt,String status){}
