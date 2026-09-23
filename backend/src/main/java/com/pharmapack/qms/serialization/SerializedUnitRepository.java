package com.pharmapack.qms.serialization; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface SerializedUnitRepository extends JpaRepository<SerializedUnit,Long>{
 @EntityGraph(attributePaths={"batch","batch.product","parent"}) List<SerializedUnit> findByBatchIdOrderByIdAsc(Long batchId);
 @EntityGraph(attributePaths={"batch","batch.product","parent"}) Optional<SerializedUnit> findBySerialNumber(String serialNumber);
 @EntityGraph(attributePaths={"batch","batch.product","parent"}) List<SerializedUnit> findByParentId(Long parentId);
 long countByBatchId(Long batchId);
}
