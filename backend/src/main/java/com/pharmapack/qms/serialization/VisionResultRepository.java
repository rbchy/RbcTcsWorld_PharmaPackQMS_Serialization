package com.pharmapack.qms.serialization;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VisionResultRepository extends JpaRepository<VisionResult, Long> {
    // Eager-load the unit: controllers map it to a DTO after the session closes (open-in-view=false).
    @EntityGraph(attributePaths = "serializedUnit")
    List<VisionResult> findBySerializedUnitSerialNumberOrderByCheckedAtDesc(String serialNumber);
}
