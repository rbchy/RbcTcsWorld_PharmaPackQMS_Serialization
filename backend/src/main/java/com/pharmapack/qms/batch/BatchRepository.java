package com.pharmapack.qms.batch; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface BatchRepository extends JpaRepository<Batch,Long>{ boolean existsByBatchNumber(String n);
 @EntityGraph(attributePaths="product") @Query("select b from Batch b") List<Batch> findAllWithProduct();
 @EntityGraph(attributePaths="product") @Query("select b from Batch b where b.id=:id") Optional<Batch> findWithProduct(@Param("id") Long id);
}
