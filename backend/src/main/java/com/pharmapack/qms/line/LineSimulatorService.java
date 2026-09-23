package com.pharmapack.qms.line;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LineSimulatorService {
    private volatile PlcState plcState = PlcState.STOPPED;
    private final Map<String, LineDeviceState> devices = new ConcurrentHashMap<>();

    public LineSimulatorService() {
        devices.put("PRINTER-01", LineDeviceState.READY);
        devices.put("VISION-01", LineDeviceState.READY);
        devices.put("SCANNER-01", LineDeviceState.READY);
        devices.put("REJECT-01", LineDeviceState.READY);
    }

    public synchronized PlcState start() {
        if (devices.values().stream().anyMatch(s -> s == LineDeviceState.OFFLINE || s == LineDeviceState.FAULT)) {
            plcState = PlcState.FAULT;
        } else {
            plcState = PlcState.RUNNING;
        }
        return plcState;
    }

    public synchronized PlcState stop() { plcState = PlcState.STOPPED; return plcState; }
    public synchronized PlcState fault() { plcState = PlcState.FAULT; return plcState; }
    public synchronized PlcState emergencyStop() { plcState = PlcState.EMERGENCY_STOP; return plcState; }
    public PlcState state() { return plcState; }

    public Map<String, LineDeviceState> devices() { return Map.copyOf(devices); }

    public LineDeviceState setDevice(String name, LineDeviceState state) {
        devices.put(name, state);
        if (state == LineDeviceState.OFFLINE || state == LineDeviceState.FAULT) plcState = PlcState.FAULT;
        return state;
    }
}
