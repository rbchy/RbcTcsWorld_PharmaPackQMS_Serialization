package com.pharmapack.qms.esignature; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface ElectronicSignatureRepository extends JpaRepository<ElectronicSignature,Long>{
 @EntityGraph(attributePaths="user") List<ElectronicSignature> findAllByOrderBySignedAtDesc();
 @EntityGraph(attributePaths="user") List<ElectronicSignature> findByEntityNameAndEntityIdOrderBySignedAtDesc(String entityName,String entityId);
}
