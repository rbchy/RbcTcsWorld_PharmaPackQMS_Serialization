USE pharmapack_qms;

-- MySQL's CREATE INDEX does not support IF NOT EXISTS, so each index is
-- created conditionally via information_schema to keep this script re-runnable.

SET @idx := (SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='products' AND index_name='idx_products_status');
SET @sql := IF(@idx=0,'CREATE INDEX idx_products_status ON products(status)','SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='batches' AND index_name='idx_batches_status');
SET @sql := IF(@idx=0,'CREATE INDEX idx_batches_status ON batches(batch_status)','SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='aql_inspections' AND index_name='idx_aql_batch');
SET @sql := IF(@idx=0,'CREATE INDEX idx_aql_batch ON aql_inspections(batch_id)','SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='reconciliations' AND index_name='idx_reconciliation_batch');
SET @sql := IF(@idx=0,'CREATE INDEX idx_reconciliation_batch ON reconciliations(batch_id)','SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='deviations' AND index_name='idx_deviation_batch');
SET @sql := IF(@idx=0,'CREATE INDEX idx_deviation_batch ON deviations(batch_id)','SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
