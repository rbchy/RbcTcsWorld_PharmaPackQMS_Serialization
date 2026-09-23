CREATE USER IF NOT EXISTS 'pharmapack_app'@'localhost' IDENTIFIED BY 'PharmaPack@123';
GRANT ALL PRIVILEGES ON pharmapack_qms.* TO 'pharmapack_app'@'localhost';
FLUSH PRIVILEGES;
