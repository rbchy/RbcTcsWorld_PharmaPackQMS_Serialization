# Phase 3 — Enterprise Portfolio Upgrade

This tracks a larger, explicitly phased upgrade requested after Phase 2 reached 100% automated
test pass (see `PHASE2_REVIEW.md`): a login screen, RbcTcsWorld watercolor branding, an icon
system, full field-level validation, 6-language help, and a benchmark against enterprise pharma
packaging software (e.g. Sharp — a real, well-known global pharmaceutical contract packaging
company at sharpservices.com, whose public site names Serialization & Aggregation as a core
capability area). The plan is delivered and verified phase by phase rather than all at once.

## Phase 3A — Authentication, branding, icon system (this update)

**Backend — production-grade JWT authentication (Spring Security)**
- Added `spring-boot-starter-security` and `jjwt` (0.12.6) to `pom.xml`.
- New `com.pharmapack.qms.auth` package:
  - `User` / `Role` JPA entities mapping the existing `users` / `roles` / `user_roles` tables (these tables existed in the schema from Phase 2 but had no Java entity yet).
  - `UserRepository` (`findByUsername`, entity-graph fetching roles).
  - `AppUserPrincipal` adapts `User` to Spring Security's `UserDetails`, exposing roles as `ROLE_<NAME>` authorities.
  - `CustomUserDetailsService`, `JwtService` (HS256 token issue/validate, secret + expiry from `application.properties` → `jwt.secret` / `jwt.expiration-ms`, overridable via `JWT_SECRET` / `JWT_EXPIRATION_MS` env vars), `JwtAuthFilter` (reads `Authorization: Bearer <token>`, populates the `SecurityContext`).
  - `SecurityConfig`: stateless sessions, CSRF disabled (pure JSON API), CORS opened for the Vite dev origins (5173/5174/4173), `/api/auth/**` open, every other `/api/**` endpoint requires a valid token. 401/403 responses reuse the existing `GlobalExceptionHandler.ApiError` JSON shape so the frontend's error handling didn't need to change.
  - `AuthController`: `POST /api/auth/login` (returns a JWT + user + roles), `GET /api/auth/me` (validates a stored token on page load).
- `database/seed.sql`: the five demo users' `password_hash` values were previously `{noop}plaintext` placeholders (fine for the pre-security demo, not fine now). Replaced with real BCrypt hashes (strength 10) of the same demo passwords — **credentials are unchanged**: `admin/admin123`, `qa_user/qa123`, `supervisor/super123`, `operator/operator123`, `inspector/inspect123`.

**Automation suite kept at 100% against the now-secured API**
- Every `/api/**` endpoint the Cucumber suite calls is now behind auth, so `ApiSteps.java` gained a `@Before` hook that logs in as `admin` once per scenario and a `Bearer` header is attached automatically to every `GET`/`POST` step — **no existing `.feature` file needed to change**.
- Added `automation/.../features/auth.feature`: valid login returns a token, an invalid password is rejected with 401, and an unauthenticated request to a protected endpoint is rejected with 401 (via a new `I GET "{path}" without authentication` step that deliberately skips the auto-attached token).
- Not yet re-run against a live backend in this sandbox (no Maven Central access here) — please run `mvn -f automation/pom.xml test` with the backend up; expect all previous scenarios plus the 3 new auth scenarios to pass.

**Frontend — login screen, branding, icons**
- `Login.tsx`: username/password screen, calls `/api/auth/login`, shows the demo credentials, stores `{token, userId, username, fullName, roles}` in `localStorage` (this is a real deployed SPA running in the user's own browser via `npm run dev`/`npm run build`, not an embedded preview, so `localStorage` is the correct, normal place for a JWT — unlike a sandboxed in-chat preview).
- `main.tsx`: the whole app is now gated behind `auth` state — no token, no data. Every API call goes through a single `api()` helper that attaches `Authorization: Bearer <token>` and, on a `401`, logs the user out and returns them to the login screen instead of showing a raw error. A `Logout` button is in the top bar.
- `icons.tsx`: a self-contained inline-SVG icon set (no external icon font / network fetch) — Home, Dashboard, and one icon per module (Product, Batch, Production, AQL, Reconciliation, QA, Deviation, CAPA), plus Save, User, Lock, Logout, Help, Globe. Every nav tab button and every form's primary Save/submit button now carries its icon.
- **Watercolor multicolor banner**: `.watercolor-banner` in `styles.css` layers five blurred, multiply-blended radial gradients (brand colors) over a soft base — a fully CSS, self-contained "hand-painted" look (no external image asset, so nothing to go missing/broken). Used full-width on the Login screen and as the compact app header ("RbcTcsWorld · PharmaPack QMS") on every screen once logged in.
- **Home button on every screen**: a persistent top bar (`Home` + `Help` + logged-in user chip + `Logout`) sits above every module's form/table, not just the nav row, so "Home" is always one click away regardless of which module you're in.
- **Multilingual Help — mechanism shipped now, full content in Phase 3C**: `Help.tsx` adds a `Help` button to the top bar on every screen, opening a popover with a 6-language switcher (English, বাংলা, Hindi, Spanish, French, Chinese). Dashboard has real translated copy already; every other screen currently falls back to a generic (English/Bangla) explanation of "fill the required fields and press Save" until Phase 3C fills in real per-screen translated help text for all 9 modules × 6 languages.
- `npm run build` re-verified clean (0 TypeScript errors) after all of the above.

## Phase 3B — full field-level validation (this update)

**New reusable validation engine — `frontend/src/validation.ts`**
- Framework-free (no external form library), single source of truth used by every form's validate-on-change, validate-on-blur and validate-on-submit.
- `FieldRule` describes each field: `type` (`code` | `name` | `text` | `decimal` | `integer` | `date` | `select`), `required`, `min`/`max`, `maxLength`, and an optional `capitalize` mode (`words`, `first`, or `upper`).
- `validateValue()` / `validateAll()` — pattern + range checks per type:
  - **decimal** — must match `-?digits(.digits)?`, plus optional min/max (e.g. Lot Size ≥ 0.001).
  - **integer** — must match `-?digits` with *no* decimal point and no letters (e.g. Defects Found, all `(user id)` fields).
  - **code** — identifiers such as Product Code / Batch Number / Deviation Number: letters, digits, `- _ /` only, no spaces or other special characters; auto-uppercased as you type (`capitalize: 'upper'`) so `batch-2026-001` becomes `BATCH-2026-001` automatically, matching how lot/batch codes are conventionally written on paper batch records.
  - **name** / **text** — allow letters, digits, spaces and a small safe punctuation set ( `. , & ( ) ' - % /` for names, plus `: ;` for longer text like descriptions/remarks); anything else (e.g. `< > { } | \ ~ * ^`) is rejected as an invalid special character.
  - **date** — must be a real calendar date.
  - Every `required` field rejects blank/whitespace-only input with an explicit "this field cannot be blank" message.
- `capitalizeWords` / `capitalizeFirst` auto-apply live as the user types — e.g. Product Name and Deviation/CAPA Title capitalize each word ("paracetamol tablet" → "Paracetamol Tablet"); Description/Remarks/Comments capitalize only the first character.
- `validateDateOrder()` — cross-field check used on the Batch form: Expiry Date cannot be before Manufacturing Date.

**Applied to all 9 forms in `frontend/src/main.tsx`**
- Products, Batches, Deviations, QA Review, Reconciliation, CAPA, AQL Inspection, Production Run, Production Entry.
- Two new small components do the rendering so every field looks and behaves consistently: `TF` (text/number/date input + inline error) and `SF` (wraps a `<select>` with the same invalid/error treatment). Both reuse the `.field-invalid` / `.field-error` CSS already added in Phase 3A.
- **Dropdown conversion**: Product's `Dosage Form` was free text; it is now a `<select>` with the standard pharma dosage-form list (Tablet, Capsule, Syrup, Injection, Ointment, Cream, Suspension, Powder, Inhaler, Other) — a concrete example of "dropdown menu where needed" for a field whose values should be constrained, not typed.
- **Mandatory-selection validation**: every `<select>` that picks a related record (Product on Batch, Batch on QA Review/Reconciliation/AQL/Production Run, Deviation on CAPA, AQL Plan, Packaging Line, Production Run on Production Entry) is now also checked in JavaScript on submit, not just via the HTML `required` attribute, with its own inline error ("Please select a batch.", etc.).
- **Extra pharma-domain cross-checks** (beyond simple per-field rules, in the spirit of a real QMS reconciliation/yield check):
  - Batch: Expiry Date cannot be before Manufacturing Date.
  - Batch Reconciliation: Good + Reject + Unused quantity cannot exceed the Starting Quantity.
  - Production Entry: Good + Reject cannot exceed the Produced quantity.
- On submit, if any field fails validation, the record is **not** sent to the backend — all invalid fields are highlighted with a red border and their specific error message, and the banner shows "Please fix the highlighted field(s) before saving."
- `npm run build` re-verified clean (0 TypeScript errors) after all of the above.

**Scope note**: this phase implements the validation as the user described it — client-side, per form, in the UI the operator types into. The backend already declares `spring-boot-starter-validation` as a dependency (added in earlier phases) but its controllers do not yet carry `@Valid`/`@NotBlank` bean-validation annotations; adding that would be a good defense-in-depth follow-up, but is left out of this phase to avoid touching the already-100%-passing backend/automation suite without the ability to compile-check it in this environment.

## Phase 3C — full multilingual help content (this update)

**`frontend/src/Help.tsx` — real, screen-specific help text for every module, in all 6 languages**
- All 9 tabs (Dashboard, Products, Batches, Production, AQL, Reconciliation, QA Review, Deviations, CAPA) now have genuine help copy — not the Phase 3A placeholder — written independently in each of English, বাংলা (Bangla), हिन्दी (Hindi), Español (Spanish), Français (French) and 中文 (Chinese). That's 9 modules × 6 languages = 54 translated help entries, plus a 6-language fallback (kept only as a safety net for a topic key that doesn't match one of the 9 tabs).
- Each entry explains, for that screen: what the form is for, which fields are required, what format/validation each field expects (tying directly into the Phase 3B rules — e.g. "Expiry Date cannot be earlier than the Manufacturing Date", "Good + Reject cannot exceed the Produced quantity"), and which fields are optional. Domain terms that are conventionally kept in English even in translated pharma/QA documentation (AQL, CAPA, QA, IPQC, GMP-style field names like Batch Number) are kept as-is inside each translation, matching how real multinational pharma operations write bilingual SOPs.
- No new mechanism was needed — Phase 3A already built the language-switcher popover and the `HELP[topic][lang]` lookup; Phase 3C is purely the content that fills it in.
- `npm run build` re-verified clean (0 TypeScript errors) after all of the above.

## Phase 3D — Serialization, full audit trail / e-signatures, and the industry comparison document (this update)

**Backend — three new modules**
- **`com.pharmapack.qms.serialization`** (new `serialized_units` table in `database/schema.sql`): unit/case/pallet-level serial number commissioning (`POST /api/serialization/commission`) and hierarchical aggregation (`POST /api/serialization/aggregate`), implementing the same UNIT-under-CASE-under-PALLET three-tier model that DSCSA (US) and EU-FMD serialization regulations are built around. Aggregation is rejected (400) if the child's level isn't strictly below the parent's, or if child and parent belong to different batches. `GET /api/serialization/batch/{batchId}`, `GET /api/serialization/{serialNumber}` and `.../children` round out the read side.
- **`com.pharmapack.qms.audit`**: `AuditTrail` entity/repository/controller over the `audit_trails` table (present since Phase 2 but unused until now), plus an `AuditTrailService.log(...)` now called from the create paths of **Batch** (create + status change), **Deviation**, **CAPA**, **QA Review** and **Reconciliation** — the GMP-critical modules. Every call is automatic (no frontend or caller action needed) and records username (from the JWT), action type, entity name/id, and old/new values. Read via `GET /api/audit-trails` and `GET /api/audit-trails/entity/{entityName}/{entityId}`. Nothing about it is editable or deletable through the API, matching CGMP / 21 CFR Part 11 style electronic-record expectations.
- **`com.pharmapack.qms.esignature`**: `ElectronicSignature` entity/repository/controller over the `electronic_signatures` table (also present since Phase 2, also unused until now). `POST /api/esignatures` captures a signature against any record (entity name + id + action + optional reason) — the signer is always read from the authenticated JWT via a new `CurrentUser` helper, never accepted from the request body, so a signature can never be forged as someone else.
- A small `com.pharmapack.qms.auth.CurrentUser` helper (`username()`, `id()`) was added so the audit-trail and e-signature modules don't each duplicate the `SecurityContextHolder` cast.

**Frontend — three new screens in `frontend/src/main.tsx`**
- **Serialization**: commission serials for a batch at a chosen aggregation level, aggregate serials under a parent, and browse a batch's serialized units.
- **Audit Trail**: read-only table of every automatically-recorded change, newest first.
- **E-Signatures**: sign any record (record type as a dropdown, record id, action as a dropdown, optional reason) and browse recorded signatures; the form explicitly tells the user the signer identity comes from their session and cannot be typed in.
- All three new forms use the Phase 3B validation engine (`validation.ts`) and the Phase 3C help mechanism — `Help.tsx` now has real, independently-translated help text for these 3 screens in all 6 languages too, so Phase 3C's multilingual coverage now spans all 12 tabs, not just the original 9.
- Three new inline-SVG icons (`IconSerial`, `IconAudit`, `IconSignature`) were added to `icons.tsx`, following the same self-contained pattern as every other icon in the app.

**Automation — 3 new Cucumber feature files**, all using the existing generic REST-Assured steps in `ApiSteps.java` (no step-definition changes needed): `serialization.feature` (commission, aggregate, the CASE-under-UNIT rejection case, list-by-batch), `audit-trail.feature` (creating a Batch/Deviation writes a matching audit row), `esignature.feature` (sign, missing-field rejection, list by entity, list all).

**`docs/INDUSTRY_COMPARISON.md`** (new) — the feature-comparison document, benchmarking this project's capability areas (Production, Batch/Lot, Reconciliation, AQL, QA Review, Deviation, CAPA, Serialization, Audit Trail, E-Signatures, GMP/CGMP scope, Security, Multilingual UI, Validation) against what a commercial pharma packaging QMS/MES typically provides. Sharp (sharpservices.com, a real global pharma contract packaging company) is named only where its own public website states a capability directly — its Serialization & Aggregation page — everything else is described as industry-typical capability, not a claim about any named company's proprietary software.

**Verification note**: the sandbox this was built in has no Maven Central access, so the new/edited Java files were verified with a static brace/paren-balance check (all clean) rather than a real `mvn compile` — the same verification level used for the Phase 3A auth package, which the user's own `mvn -f automation/pom.xml test` run confirmed worked end to end (and caught one real bug that static checking couldn't). The frontend changes (`main.tsx`, `Help.tsx`, `icons.tsx`) were verified with a real `npm run build` (0 TypeScript errors). **Please run `mvn -f automation/pom.xml test` against a running, freshly-migrated backend** (the new `serialized_units` table needs `database/schema.sql` re-applied) to get a real compile + the 3 new feature files' results — as in every previous phase, report back anything that fails and it will be fixed immediately.
