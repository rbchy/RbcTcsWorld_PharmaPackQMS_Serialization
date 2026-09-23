# PharmaPack QMS Form Design

## Product Master
Product Code*, Product Name*, Strength, Dosage Form, Pack Size, Status.
Actions: New, Save, Update, Delete, Reset, Search.
Grid: ID, Code, Name, Strength, Dosage, Pack Size, Status.

## Material Master
Material Code*, Material Name*, Material Type*, UOM*, Status.
Grid with search and CRUD.

## Material Lot Verification
Batch*, Material Lot*, Verified Quantity*, Verified By*, Date/Time, Status, Remarks.
Actions: Verify, Reject, Clear.

## Batch Master
Batch Number*, Product*, Lot Size*, Manufacturing Date, Expiry Date, Batch Status.
Rules: unique batch, lot size > 0, expiry >= manufacturing.

## Line Clearance
Batch*, Packaging Line*, Performed By*, Date/Time.
Checklist: previous product removed; previous labels/cartons removed; documents removed; line cleaned; equipment verified; materials available; artwork/version verified; area inspected.
Each item: PASS/FAIL/N/A, evidence, remarks.
Approval: QA Approver, Approval Date/Time, Overall Status.

## Packaging / Production
Batch, Line, Equipment, Start/End.
Entry grid: Time, Produced, Good, Reject, Operator, Remarks.
Auto totals and yield.
Rule: Good + Reject <= Produced.

## AQL Inspection
Batch, AQL Plan, Inspection Level, AQL, Sample Size, Inspector, Time.
Defect grid: Defect Code, Class, Quantity, Remarks.
Auto Total Defects and ACCEPT/REJECT based on configured limits.

## Reconciliation
Starting Quantity, Good, Reject, Unused.
Calculated: Reconciled = Good + Reject + Unused; Variance = Starting - Reconciled.
Status: RECONCILED if variance=0, otherwise INVESTIGATION_REQUIRED.

## QA Review
Batch, Line Clearance status, AQL result, Reconciliation status, Open Deviations, Decision, Reviewer, Comments, Electronic Signature.
Decisions: PENDING, RELEASED, REJECTED, HOLD.

## Deviation
Deviation Number*, Batch, Title*, Description*, Severity, Status, Opened By, Root Cause, Immediate Action, Closure Comments.

## CAPA
Deviation*, Action Type, Description*, Owner, Due Date, Completed Date, Status, Effectiveness Result.

## Audit Trail
Filters: date range, user, action, entity, entity ID.
Grid: timestamp, user, action, entity, ID, field, old value, new value.

*This is a portfolio/training design, not a validated GMP production UI.*
