package com.pharmapack.qms.serialization;

import com.pharmapack.qms.auth.CurrentUser;
import com.pharmapack.qms.line.LineSimulatorService;
import com.pharmapack.qms.line.PlcState;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SerializationWorkflowService {
    private final SerializedUnitRepository unitRepository;
    private final SerializationEventRepository eventRepository;
    private final VisionResultRepository visionRepository;
    private final LineSimulatorService line;

    public SerializationWorkflowService(SerializedUnitRepository unitRepository,
                                        SerializationEventRepository eventRepository,
                                        VisionResultRepository visionRepository,
                                        LineSimulatorService line) {
        this.unitRepository = unitRepository;
        this.eventRepository = eventRepository;
        this.visionRepository = visionRepository;
        this.line = line;
    }

    /** DEF-04: printing, vision inspection and commissioning happen ON the line, so the PLC must be RUNNING. */
    private void requireLineRunning(String operation) {
        if (line.state() != PlcState.RUNNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot " + operation + ": packaging line is " + line.state() + " (must be RUNNING)");
        }
    }

    @Transactional
    public VisionResult verify(String serial, String actualBarcode, String actualLot, String actualExpiry) {
        requireLineRunning("run vision verification");
        SerializedUnit unit = unitRepository.findBySerialNumber(serial)
                .orElseThrow(() -> new IllegalArgumentException("Serial number not found: " + serial));
        // DEF-03: the camera can only inspect a code that has actually been printed.
        if (!"PRINTED".equals(unit.getStatus())) {
            throw new IllegalArgumentException("Only PRINTED serials can be vision-verified (status is "
                    + unit.getStatus() + ")");
        }

        String expectedLot = unit.getBatch().getBatchNumber();
        String expectedExpiry = unit.getBatch().getExpiryDate() == null ? null : unit.getBatch().getExpiryDate().toString();
        String expectedBarcode = unit.getSerialNumber();

        boolean barcodePass = expectedBarcode.equals(actualBarcode);
        boolean lotPass = expectedLot.equals(actualLot);
        boolean expiryPass = expectedExpiry == null ? actualExpiry == null : expectedExpiry.equals(actualExpiry);
        VisionDecision decision = barcodePass && lotPass && expiryPass ? VisionDecision.PASS : VisionDecision.FAIL;

        VisionResult result = visionRepository.save(VisionResult.builder()
                .serializedUnit(unit)
                .expectedBarcode(expectedBarcode).actualBarcode(actualBarcode)
                .expectedLot(expectedLot).actualLot(actualLot)
                .expectedExpiry(expectedExpiry).actualExpiry(actualExpiry)
                .barcodePass(barcodePass).lotPass(lotPass).expiryPass(expiryPass)
                .decision(decision).deviceName("VISION-SIMULATOR-01")
                .build());

        eventRepository.save(SerializationEvent.builder()
                .serializedUnit(unit)
                .eventType(SerializationEventType.VISION_VERIFIED)
                .eventResult(decision.name())
                .operatorId(CurrentUser.id())
                .details("Simulated DataMatrix/OCR/OCV verification")
                .build());

        if (decision == VisionDecision.PASS) {
            unit.setStatus("VISION_VERIFIED");
        } else {
            unit.setStatus("REJECTED");
        }
        unitRepository.save(unit);
        return result;
    }

    @Transactional
    public SerializedUnit commission(String serial) {
        requireLineRunning("commission");
        SerializedUnit unit = unitRepository.findBySerialNumber(serial)
                .orElseThrow(() -> new IllegalArgumentException("Serial number not found: " + serial));
        // GMP: only a serial with a PASS vision result (status VISION_VERIFIED) may be commissioned.
        // PRINTED used to be accepted too, which let an unverified carton be commissioned.
        if (!"VISION_VERIFIED".equals(unit.getStatus())) {
            throw new IllegalArgumentException("Serial must pass vision verification before commissioning (status is "
                    + unit.getStatus() + ")");
        }
        unit.setStatus("COMMISSIONED");
        unit.setCommissionedAt(LocalDateTime.now());
        unit.setCommissionedBy(CurrentUser.id());
        unitRepository.save(unit);
        eventRepository.save(SerializationEvent.builder().serializedUnit(unit)
                .eventType(SerializationEventType.COMMISSIONED).eventResult("PASS")
                .operatorId(CurrentUser.id()).build());
        return unit;
    }

    @Transactional
    public SerializedUnit print(String serial) {
        requireLineRunning("print");
        SerializedUnit unit = unitRepository.findBySerialNumber(serial)
                .orElseThrow(() -> new IllegalArgumentException("Serial number not found: " + serial));
        if (!"CREATED".equals(unit.getStatus())) {
            throw new IllegalArgumentException("Only CREATED serials can be printed");
        }
        unit.setStatus("PRINTED");
        unitRepository.save(unit);
        eventRepository.save(SerializationEvent.builder().serializedUnit(unit)
                .eventType(SerializationEventType.PRINTED).eventResult("PASS")
                .operatorId(CurrentUser.id()).build());
        return unit;
    }

    @Transactional
    public SerializedUnit decommission(String serial, DecommissionReason reason, String comment) {
        SerializedUnit unit = unitRepository.findBySerialNumber(serial)
                .orElseThrow(() -> new IllegalArgumentException("Serial number not found: " + serial));
        if ("SHIPPED".equals(unit.getStatus())) {
            throw new IllegalArgumentException("Shipped serial cannot be decommissioned in this demo workflow");
        }
        unit.setStatus("DECOMMISSIONED");
        unitRepository.save(unit);
        eventRepository.save(SerializationEvent.builder().serializedUnit(unit)
                .eventType(SerializationEventType.DECOMMISSIONED)
                .eventResult("CONTROLLED_DISPOSITION")
                .reasonCode(reason == null ? null : reason.name())
                .operatorId(CurrentUser.id()).details(comment).build());
        return unit;
    }
}
