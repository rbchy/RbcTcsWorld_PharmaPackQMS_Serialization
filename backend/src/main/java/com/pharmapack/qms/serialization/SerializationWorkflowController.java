package com.pharmapack.qms.serialization;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/serialization")
@RequiredArgsConstructor
public class SerializationWorkflowController {
    private final SerializationWorkflowService service;
    private final SerializationEventRepository eventRepository;
    private final VisionResultRepository visionRepository;

    public record VerifyRequest(String serialNumber, String actualBarcode, String actualLot, String actualExpiry) {}
    public record DecommissionRequest(String serialNumber, DecommissionReason reason, String comment) {}

    @PostMapping("/print/{serialNumber}")
    public SerializedUnit print(@PathVariable String serialNumber) {
        return service.print(serialNumber);
    }

    @PostMapping("/verify")
    public VisionResult verify(@RequestBody VerifyRequest request) {
        return service.verify(request.serialNumber(), request.actualBarcode(),
                request.actualLot(), request.actualExpiry());
    }

    @PostMapping("/commission/{serialNumber}")
    public SerializedUnit commission(@PathVariable String serialNumber) {
        return service.commission(serialNumber);
    }

    @PostMapping("/decommission")
    public SerializedUnit decommission(@RequestBody DecommissionRequest request) {
        return service.decommission(request.serialNumber(), request.reason(), request.comment());
    }

    public record EventResponse(Long id, String serialNumber, SerializationEventType eventType,
                                String eventResult, String reasonCode, Long operatorId,
                                String details, java.time.LocalDateTime eventTime) {}

    @GetMapping("/{serialNumber}/events")
    public List<EventResponse> events(@PathVariable String serialNumber) {
        return eventRepository.findBySerializedUnitSerialNumberOrderByEventTimeDesc(serialNumber)
                .stream().map(e -> new EventResponse(e.getId(), e.getSerializedUnit().getSerialNumber(),
                        e.getEventType(), e.getEventResult(), e.getReasonCode(), e.getOperatorId(),
                        e.getDetails(), e.getEventTime())).toList();
    }

    public record VisionResponse(Long id, String serialNumber, String expectedBarcode, String actualBarcode,
                                 String expectedLot, String actualLot, String expectedExpiry,
                                 String actualExpiry, boolean barcodePass, boolean lotPass,
                                 boolean expiryPass, VisionDecision decision, String deviceName,
                                 java.time.LocalDateTime checkedAt) {}

    @GetMapping("/{serialNumber}/vision")
    public List<VisionResponse> vision(@PathVariable String serialNumber) {
        return visionRepository.findBySerializedUnitSerialNumberOrderByCheckedAtDesc(serialNumber)
                .stream().map(v -> new VisionResponse(v.getId(), v.getSerializedUnit().getSerialNumber(),
                        v.getExpectedBarcode(), v.getActualBarcode(), v.getExpectedLot(), v.getActualLot(),
                        v.getExpectedExpiry(), v.getActualExpiry(), v.isBarcodePass(), v.isLotPass(),
                        v.isExpiryPass(), v.getDecision(), v.getDeviceName(), v.getCheckedAt())).toList();
    }
}
