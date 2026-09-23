package com.pharmapack.qms.line;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/line")
@RequiredArgsConstructor
public class LineSimulatorController {
    private final LineSimulatorService service;

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("plcState", service.state(), "devices", service.devices());
    }

    @PostMapping("/start") public Map<String, Object> start() {
        return Map.of("plcState", service.start());
    }

    @PostMapping("/stop") public Map<String, Object> stop() {
        return Map.of("plcState", service.stop());
    }

    @PostMapping("/fault") public Map<String, Object> fault() {
        return Map.of("plcState", service.fault());
    }

    @PostMapping("/emergency-stop") public Map<String, Object> emergencyStop() {
        return Map.of("plcState", service.emergencyStop());
    }

    @PutMapping("/devices/{name}")
    public Map<String, Object> setDevice(@PathVariable String name, @RequestParam LineDeviceState state) {
        return Map.of("device", name, "state", service.setDevice(name, state), "plcState", service.state());
    }
}
