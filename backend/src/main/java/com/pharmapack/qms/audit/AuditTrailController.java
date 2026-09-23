package com.pharmapack.qms.audit; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/audit-trails") public class AuditTrailController { final AuditTrailRepository ar; public AuditTrailController(AuditTrailRepository a){ar=a;}
 @GetMapping public List<AuditTrail> list(){return ar.findAllByOrderByCreatedAtDesc();}
 @GetMapping("/entity/{entityName}/{entityId}") public List<AuditTrail> forEntity(@PathVariable String entityName,@PathVariable String entityId){return ar.findByEntityNameAndEntityIdOrderByCreatedAtDesc(entityName,entityId);}
}
