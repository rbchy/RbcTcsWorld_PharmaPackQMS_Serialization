package com.pharmapack.qms.serialization; import java.time.LocalDateTime;
public record SerializedUnitResponse(Long id,Long batchId,String batchNumber,String productCode,String serialNumber,String gtin,String aggregationLevel,Long parentId,String parentSerialNumber,String status,LocalDateTime commissionedAt,Long commissionedBy){}
