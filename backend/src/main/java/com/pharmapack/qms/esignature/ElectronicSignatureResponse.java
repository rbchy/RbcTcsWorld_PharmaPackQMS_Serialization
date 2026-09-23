package com.pharmapack.qms.esignature; import java.time.LocalDateTime;
public record ElectronicSignatureResponse(Long id,Long userId,String username,String fullName,String entityName,String entityId,String actionType,LocalDateTime signedAt,String signatureReason){}
