USE pharmapack_qms;
SHOW TABLES;
SELECT id,product_code,product_name,status FROM products ORDER BY id;
SELECT b.id,b.batch_number,b.product_id,p.product_code,p.product_name,b.lot_size,b.batch_status FROM batches b LEFT JOIN products p ON p.id=b.product_id ORDER BY b.id;
