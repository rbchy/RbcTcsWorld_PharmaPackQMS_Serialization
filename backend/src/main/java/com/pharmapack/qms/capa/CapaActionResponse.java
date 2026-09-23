package com.pharmapack.qms.capa; import java.time.LocalDate;
public record CapaActionResponse(Long id,Long deviationId,String deviationNumber,Long batchId,String batchNumber,String productCode,String productName,String actionType,String actionDescription,Long ownerId,LocalDate dueDate,LocalDate completedDate,String status,String effectivenessResult){}
