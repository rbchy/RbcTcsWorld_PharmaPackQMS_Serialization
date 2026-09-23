package com.pharmapack.qms.serialization;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SerializationEventRepository extends JpaRepository<SerializationEvent, Long> {
    // Eager-load the unit: controllers map it to a DTO after the session closes (open-in-view=false).
    @EntityGraph(attributePaths = "serializedUnit")
    List<SerializationEvent> findBySerializedUnitIdOrderByEventTimeDesc(Long serializedUnitId);
    // Eager-load the unit: controllers map it to a DTO after the session closes (open-in-view=false).
    @EntityGraph(attributePaths = "serializedUnit")
    List<SerializationEvent> findBySerializedUnitSerialNumberOrderByEventTimeDesc(String serialNumber);
}
