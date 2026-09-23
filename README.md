# RbcTcsWorld PharmaPack QMS — Phase 2

This version updates the supplied V2.1 project into a MySQL-backed Spring Boot + React + Cucumber foundation for a pharmaceutical packaging QMS portfolio/training project.

### Main improvements
- Java 21 / Spring Boot 3.5.5
- MySQL integration with `pharmapack_app`
- Product + Batch service layers and DTO-safe Batch responses
- Production, AQL, Reconciliation, QA Review, Deviation and CAPA APIs
- Material, Equipment and Packaging Line master APIs
- React Product and Batch forms
- Cucumber/REST Assured API smoke coverage
- Database scripts and runbook

### Important
The project is designed for portfolio/training use. A real GMP-regulated deployment requires formal validation, access control, audit/e-signature controls, change control, backup/recovery, security review and qualification.


## Phase 4 — Systech-inspired Serialization & Line Control

This release extends the portfolio/training application with a functional simulation of a pharmaceutical packaging serialization line. It is inspired by publicly documented industry concepts such as Systech UniSeries full-stack serialization, commissioning, barcode validation, aggregation, rework and line/site management; it is not a copy of Systech proprietary software.

### New capabilities

- L3-style serialization workflow: CREATED -> PRINTED -> VISION_VERIFIED -> COMMISSIONED / REJECTED -> REWORK / DECOMMISSIONED -> AGGREGATED -> SHIPPED
- Vision simulator for DataMatrix/barcode, lot and expiry verification
- PLC/line simulator with printer, vision, scanner and reject device states
- Serialization event history and vision-result history
- Controlled decommissioning with reason codes and operator/comment
- Case/pallet parent-child aggregation retained from the previous serialization module
- React packaging-line HMI with line controls and serialization workflow
- REST Assured/Cucumber automation assets for the new line/serialization flows
- MySQL migration: `database/phase4-systech-serialization.sql`

### Important

This is a portfolio/training simulation. It is not a validated GxP production system, and the decommissioning/serialization rules must be adapted to the applicable market, site SOPs, validated configuration, and regulatory requirements before any real-world use.
