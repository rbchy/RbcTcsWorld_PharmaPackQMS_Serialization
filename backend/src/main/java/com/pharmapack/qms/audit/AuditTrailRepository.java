package com.pharmapack.qms.audit; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface AuditTrailRepository extends JpaRepository<AuditTrail,Long>{
 List<AuditTrail> findAllByOrderByCreatedAtDesc();
 List<AuditTrail> findByEntityNameAndEntityIdOrderByCreatedAtDesc(String entityName,String entityId);
}
