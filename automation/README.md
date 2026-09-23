# Phase 2 Automation
Cucumber API smoke coverage for the Phase 2 endpoints plus Selenium can be added against the Vite UI.
Run after backend startup: `mvn -f automation/pom.xml test`. Do not claim all scenarios pass until the local environment is running and the report is reviewed.


## Phase 4 — Systech-inspired serialization automation

The automation layer now covers the simulated packaging line and serialization workflow:

- REST Assured: `/api/line`, `/api/serialization/verify`, print, commission and decommission APIs
- Selenium: React packaging-line HMI and serialization screens
- Cucumber: end-to-end business scenarios in `src/test/resources/features/serialization_line.feature`
- JDBC: validate `serialized_units`, `serialization_events`, and `vision_results`
- Negative tests: wrong DataMatrix, wrong lot/expiry, device fault, duplicate serial and aggregation mismatch

This is a portfolio/functional simulation inspired by public industry concepts; it is not a validated Systech product or a GxP production implementation.
