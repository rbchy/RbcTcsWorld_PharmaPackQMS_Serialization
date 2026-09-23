# Capability Comparison — RbcTcsWorld PharmaPack QMS vs. Commercial Pharma Packaging Platforms

This document benchmarks the capability *areas* this training/portfolio project covers
against what a commercial-grade pharmaceutical packaging QMS/MES platform typically
provides at enterprise scale. It was written as part of Phase 3D, alongside adding the
Serialization and full Audit Trail / Electronic Signature modules to this project.

**Scope and honesty note.** Sharp (a real global pharmaceutical contract packaging
company, sharpservices.com) is used by name only where its own public website states a
capability directly — that is the Serialization & Aggregation row below, sourced from
<https://www.sharpservices.com/solutions/packaging/serialization-aggregation/>, which
states Sharp has supported clients with serialization since 2007 and provides
"industry-leading traceability technology" for global drug-supply-chain-security
regulations, without publishing internal technical or software detail. Sharp's public
pages do not describe the internal architecture of their software, so every other row
below describes **industry-typical capability at commercial scale**, not a claim about
Sharp's specific implementation. Nothing in this document should be read as a claim
about any named company's proprietary software beyond what that company has published
itself.

## Comparison table

| Capability area | This project (RbcTcsWorld PharmaPack QMS) | Typical commercial pharma packaging QMS/MES |
|---|---|---|
| Production management | Production Run + Production Entry tracking per batch/line/equipment, with produced/good/reject quantities and a client- and server-side check that good+reject cannot exceed produced. | Full line-level MES integration: real-time OEE, PLC/SCADA data capture from packaging equipment, automatic downtime and changeover tracking. |
| Batch / lot management | Batch master with product linkage, lot size, manufacturing/expiry dates, status lifecycle, and a validation rule that expiry cannot precede manufacturing. | Same core concept, typically extended with multi-site batch genealogy, raw-material lot genealogy, and ERP (SAP/Oracle) batch record synchronization. |
| Material reconciliation | Batch Reconciliation module: starting/good/reject/unused quantities, auto-calculated reconciled quantity and variance, with a rule that outputs cannot exceed the starting quantity. | Same concept, usually tied directly to warehouse/ERP inventory transactions so reconciliation posts back to stock automatically. |
| AQL / sampling inspection | AQL Inspection module against configurable AQL Plans (inspection level, sample size, acceptance/rejection numbers per ANSI/ASQ Z1.4-style sampling), with defect recording. | Same ANSI/ASQ Z1.4 (or ISO 2859-1) sampling concept, often paired with vision-system or camera-based automated defect detection rather than manual entry. |
| QA review & batch release | QA Review module: review type (IPQC/FINAL/LINE_CLEARANCE), decision (PASS/FAIL/PENDING), reviewer and comments, now signable via the Phase 3D Electronic Signatures module. | Same workflow concept, typically with multi-step, role-gated approval chains and direct linkage to batch release/hold status in the ERP. |
| Deviation management | Deviation module: severity (MINOR/MAJOR/CRITICAL), status lifecycle, linkage to a batch, full-text description. | Same concept, usually with configurable investigation workflows, root-cause taxonomies, and SLA/escalation timers by severity. |
| CAPA | CAPA Actions module linked to a Deviation, action type (CORRECTIVE/PREVENTIVE), owner, due date, effectiveness result field. | Same concept, typically with automated due-date escalation/reminders and effectiveness-check scheduling. |
| Serialization & aggregation | **Phase 3D**: unit/case/pallet-level serial number commissioning and hierarchical aggregation (UNIT under CASE under PALLET), implementing the same three-tier model that DSCSA (US) and EU-FMD serialization regulations are built around. | Sharp's own site names "Serialization and Aggregation Solutions" as a core capability, supported since 2007, positioned as "industry-leading traceability technology" for global drug-supply-chain-security regulation compliance — see source above. At enterprise scale this typically also includes real-time integration with contract manufacturers' and distributors' repository systems (e.g. via EPCIS-standard data exchange) — a scope well beyond a training project. |
| Audit trail | **Phase 3D**: every Batch create/status-change, Deviation, CAPA action, QA review and Reconciliation calculation automatically writes an immutable who/what/when row (`audit_trails` table), read-only via `/api/audit-trails`. | Same 21 CFR Part 11 style expectation — a full commercial platform typically extends this to every field-level change across every module, with tamper-evident storage and long-term regulatory retention/archival. |
| Electronic signatures | **Phase 3D**: any record can be signed (`/api/esignatures`) with the signer always taken from the authenticated session (JWT) — never client-supplied — recording who/what/when/why. | Same 21 CFR Part 11 concept, typically extended with signature meaning codes tied to specific workflow transitions (e.g. a release signature that also flips ERP batch status) and biometric/PIN re-authentication at the moment of signing. |
| GMP / CGMP scope | Demonstrates the core GMP quality-event and batch-record concepts (deviations, CAPA, QA review, reconciliation, audit trail, e-signatures) as a coherent, working reference implementation. | A commercial platform is typically validated (IQ/OQ/PQ) and audited against a specific site's Quality Management System, and integrated with LIMS, ERP, building-management and environmental-monitoring systems that sit outside a packaging QMS's own scope. |
| Security & access | Spring Security + JWT authentication, BCrypt password hashing, role-based authorities (ADMIN/QA/SUPERVISOR/OPERATOR/INSPECTOR/VIEWER). | Same RBAC concept, typically extended with SSO/LDAP/Active Directory integration and periodic access recertification workflows. |
| Multilingual UI | 6-language help content (English, বাংলা, हिन्दी, Español, Français, 中文) built into every screen (Phase 3C). | Commercial platforms vary widely; multilingual UI is common for multinational operators but language coverage depends on the specific vendor and deployment. |
| Field-level validation | Numeric/decimal/integer, text/special-character, date, blank-field, capitalization and dropdown-constrained validation on every form (Phase 3B), plus pharma-domain cross-checks (expiry vs. manufacturing date, good+reject+unused vs. starting quantity, good+reject vs. produced). | Same baseline expectation for any production system; commercial platforms typically also enforce these rules server-side with full bean-validation and business-rule engines. |

## How to read this table

The point of this comparison is not to claim this training project matches a commercial
platform's scale or validation status — it does not, and is explicitly a portfolio/
training application. The point is to show, capability area by capability area, that the
project's design *understands* what each of these GMP/CGMP/QA-automation concepts is for
and implements a working, testable version of it: a real JWT-secured API, a real
audit trail that writes itself automatically from actual business actions, a real
DSCSA/EU-FMD-style serialization hierarchy, and a real Cucumber/REST-Assured automation
suite exercising all of it end to end.

## Sources

- [Pharmaceutical Serialization & Aggregation — Sharp](https://www.sharpservices.com/solutions/packaging/serialization-aggregation/)
- [From Serialization to Aggregation: Sharp Packaging Provides DSCSA Compliance with TraceLink — TraceLink](https://www.tracelink.com/resources/resource-center/sharp-provides-dscsa-compliance-with-tracelink)
