package com.pharmapack.qms.line;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/** The simulated PLC: the line runs only when every device is healthy. */
class LineSimulatorServiceTest {

    @Test
    void aNewLineIsStoppedWithFourReadyDevices() {
        LineSimulatorService line = new LineSimulatorService();
        assertEquals(PlcState.STOPPED, line.state());
        assertEquals(4, line.devices().size());
        assertTrue(line.devices().values().stream().allMatch(s -> s == LineDeviceState.READY));
    }

    @Test
    void startWithHealthyDevicesRuns() {
        LineSimulatorService line = new LineSimulatorService();
        assertEquals(PlcState.RUNNING, line.start());
        assertEquals(PlcState.RUNNING, line.state());
    }

    @ParameterizedTest
    @EnumSource(value = LineDeviceState.class, names = {"OFFLINE", "FAULT"})
    void anUnhealthyDeviceFaultsTheLineAndBlocksStart(LineDeviceState bad) {
        LineSimulatorService line = new LineSimulatorService();
        line.start();
        line.setDevice("VISION-01", bad);
        assertEquals(PlcState.FAULT, line.state(), "the running line must stop on a device " + bad);
        assertEquals(PlcState.FAULT, line.start(), "start must be refused while a device is " + bad);
    }

    @Test
    void repairingTheDeviceAllowsARestart() {
        LineSimulatorService line = new LineSimulatorService();
        line.setDevice("PRINTER-01", LineDeviceState.FAULT);
        line.setDevice("PRINTER-01", LineDeviceState.READY);
        assertEquals(PlcState.RUNNING, line.start());
    }

    @Test
    void stopAndEmergencyStopWin() {
        LineSimulatorService line = new LineSimulatorService();
        line.start();
        assertEquals(PlcState.STOPPED, line.stop());
        line.start();
        assertEquals(PlcState.EMERGENCY_STOP, line.emergencyStop());
        assertEquals(PlcState.FAULT, line.fault());
    }

    @Test
    void devicesViewIsReadOnly() {
        LineSimulatorService line = new LineSimulatorService();
        assertThrows(UnsupportedOperationException.class,
                () -> line.devices().put("PRINTER-01", LineDeviceState.FAULT));
        assertEquals(LineDeviceState.READY, line.devices().get("PRINTER-01"));
    }
}
