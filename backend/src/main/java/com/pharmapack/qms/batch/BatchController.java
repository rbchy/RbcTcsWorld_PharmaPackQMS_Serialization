package com.pharmapack.qms.batch;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.math.*; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/batches") public class BatchController { final BatchService s; public BatchController(BatchService s){this.s=s;}
 @GetMapping public List<BatchResponse> list(){return s.list();} @GetMapping("/{id}") public BatchResponse get(@PathVariable Long id){return s.get(id);}
 public record Create(String batchNumber,Long productId,BigDecimal lotSize,LocalDate manufacturingDate,LocalDate expiryDate){}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public BatchResponse create(@RequestBody Create r){return s.create(r.batchNumber(),r.productId(),r.lotSize(),r.manufacturingDate(),r.expiryDate());}
 public record Status(String status){} @PatchMapping("/{id}/status") public BatchResponse status(@PathVariable Long id,@RequestBody Status r){try{return s.status(id,BatchStatus.valueOf(r.status()));}catch(Exception e){throw new IllegalArgumentException("Unknown batch status: "+r.status());}}
}
