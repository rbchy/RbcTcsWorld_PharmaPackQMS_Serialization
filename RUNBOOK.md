# Phase 2 Runbook

## 1. MySQL
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
mysql -u root -p < database/create_app_user.sql
mysql -u root -p < database/phase2-migration.sql
```
Verify:
```sql
USE pharmapack_qms;
SELECT id,product_code,product_name,status FROM products;
SELECT id,batch_number,product_id,batch_status FROM batches;
```

## 2. Backend
Set credentials if different from defaults:
```bash
export DB_USERNAME=pharmapack_app
export DB_PASSWORD='PharmaPack@123'
```
Run from project root:
```bash
mvn clean test
mvn spring-boot:run
```

## 3. API checks
```bash
curl -i http://localhost:8080/api/products
curl -i http://localhost:8080/api/batches
curl -i http://localhost:8080/api/aql/plans
```

## 4. Frontend
```bash
cd frontend
npm install
npm run build
npm run dev
```
Open `http://localhost:5173`.

## 5. Automation
In another terminal after backend is running:
```bash
mvn -f automation/pom.xml test
```

## Expected data flow
Product → Material → Batch → Production → AQL → Reconciliation → QA Review → Deviation/CAPA.


## Phase 4 setup — Serialization Line Simulation

1. Start MySQL and the `pharmapack_qms` database.
2. Apply `database/phase4-systech-serialization.sql` after the existing schema/phase migrations.
3. Start the Spring Boot application from the project root:
   `mvn spring-boot:run`
4. Start the React UI from `frontend`:
   `npm install`
   `npm run dev`
5. Log in and open **Serialization**.
6. Use **START LINE** to start the PLC simulator.
7. Generate serials for a batch. New serials are provisioned as `CREATED`.
8. Use **Print**, then the Vision Simulator with the correct DataMatrix/lot/expiry.
9. Commission only after a PASS vision result.
10. Use aggregation to create parent-child relationships.
11. Use controlled decommissioning for simulated exceptions.
12. Run automation with the backend running:
   `cd automation && mvn test -DbaseUrl=http://localhost:8080/api`

The simulation intentionally separates the concepts of line control, vision verification, serialization events, aggregation and QMS/audit data so they can be tested independently with Selenium, REST Assured, Cucumber and JDBC.
