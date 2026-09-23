-- Phase 4: Systech-inspired serialization / line simulation extension
-- Educational/portfolio simulation only. Do not use as a validated GxP production migration.

ALTER TABLE serialized_units
    MODIFY COLUMN status VARCHAR(30) NOT NULL DEFAULT 'CREATED';

CREATE TABLE IF NOT EXISTS serialization_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serialized_unit_id BIGINT NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    event_result VARCHAR(40),
    reason_code VARCHAR(80),
    operator_id BIGINT,
    details VARCHAR(1000),
    event_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_serial_event_unit FOREIGN KEY (serialized_unit_id) REFERENCES serialized_units(id),
    CONSTRAINT fk_serial_event_operator FOREIGN KEY (operator_id) REFERENCES users(id)
);
CREATE INDEX idx_serial_event_unit ON serialization_events(serialized_unit_id);
CREATE INDEX idx_serial_event_time ON serialization_events(event_time);

CREATE TABLE IF NOT EXISTS vision_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serialized_unit_id BIGINT NOT NULL,
    expected_barcode VARCHAR(120),
    actual_barcode VARCHAR(120),
    expected_lot VARCHAR(80),
    actual_lot VARCHAR(80),
    expected_expiry VARCHAR(40),
    actual_expiry VARCHAR(40),
    barcode_pass BOOLEAN NOT NULL,
    lot_pass BOOLEAN NOT NULL,
    expiry_pass BOOLEAN NOT NULL,
    decision VARCHAR(10) NOT NULL,
    device_name VARCHAR(100),
    checked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vision_unit FOREIGN KEY (serialized_unit_id) REFERENCES serialized_units(id)
);
CREATE INDEX idx_vision_unit ON vision_results(serialized_unit_id);

-- Recommended seed/reference devices for the simulator
-- The line simulator itself is in-memory so it can be used without hardware.
