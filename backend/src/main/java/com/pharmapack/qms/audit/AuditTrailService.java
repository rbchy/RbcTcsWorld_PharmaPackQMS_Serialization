package com.pharmapack.qms.audit; import com.pharmapack.qms.auth.CurrentUser; import org.springframework.stereotype.Service; import java.time.LocalDateTime;
/**
 * Writes one immutable audit_trails row per tracked action. Called from the create /
 * status-change endpoints of the GMP-critical modules (Batch, Deviation, CAPA, QA Review,
 * Reconciliation) so every quality-relevant change carries a who / what / when record -
 * the kind of electronic-record traceability CGMP / 21 CFR Part 11 style systems require.
 * The acting user is always read from the authenticated JWT (never client-supplied).
 */
@Service public class AuditTrailService { final AuditTrailRepository ar; public AuditTrailService(AuditTrailRepository a){ar=a;}
 public void log(String actionType,String entityName,Object entityId,String fieldName,Object oldValue,Object newValue){
  ar.save(AuditTrail.builder()
    .username(CurrentUser.username())
    .actionType(actionType)
    .entityName(entityName)
    .entityId(entityId==null?null:String.valueOf(entityId))
    .fieldName(fieldName)
    .oldValue(oldValue==null?null:String.valueOf(oldValue))
    .newValue(newValue==null?null:String.valueOf(newValue))
    .createdAt(LocalDateTime.now())
    .build());
 }
}
