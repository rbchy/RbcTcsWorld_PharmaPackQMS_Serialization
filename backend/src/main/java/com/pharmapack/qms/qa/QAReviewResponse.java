package com.pharmapack.qms.qa; import java.time.LocalDateTime;
public record QAReviewResponse(Long id,Long batchId,String batchNumber,String productCode,String productName,Long reviewerId,String reviewType,String decision,LocalDateTime reviewDate,String comments){}
