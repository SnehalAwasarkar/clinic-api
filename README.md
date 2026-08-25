# clinic-api

Phase 0 of the clinic appointment plan: bare-minimum scaffold to prove clean generation — `Patient` and `Appointment` with full CRUD and a single FK relation. No RBAC, no capabilities, no search. Later phases (Doctor, KYC-style patient fields, RBAC, appointment/doctor status workflows, round-robin assignment, a companion billing service, reporting) build on top of this.

## Domain model

- **Patient** — firstName, lastName, email (unique), phone, dateOfBirth (required, must be in the past), gender (`MALE` / `FEMALE` / `OTHER`, required), address (optional), status (`ACTIVE` / `INACTIVE` / `SUSPENDED`, defaults to `ACTIVE`)
- **Doctor** — firstName, lastName, specialization, licenseNumber (unique, required), status (`ACTIVE` / `INACTIVE`, defaults to `ACTIVE`)
- **Appointment** — belongs to a Patient and a Doctor (both required); appointmentDate, reason, status (`SCHEDULED` / `CONFIRMED` / `COMPLETED` / `CANCELLED` / `NO_SHOW`, defaults to `SCHEDULED` — the full enum is already here so Phase 2 can add transition validation without reshaping the field)

Status isn't writable through the API yet in Phase 0 — it's just tracked. Phase 2 is expected to add the transition capability on top.

## Run

Uses a local Postgres.app instance on port 5432 (see `food-delivery-api`'s README in this same folder for why, if you hit port conflicts). One-time setup:


psql -h localhost -p 5432 -U "$(whoami)" -d postgres -c "CREATE ROLE clinic WITH LOGIN PASSWORD 'clinic';"
psql -h localhost -p 5432 -U "$(whoami)" -d postgres -c "CREATE DATABASE clinic OWNER clinic;"


Then run:


mvn spring-boot:run


Listens on `http://localhost:4006`.

## Tests


mvn test


Tests run against an in-memory H2 database (`src/test/resources/application.properties`), so they don't need Postgres running.

## Endpoints

### Patients
- `POST /api/patients` — `{ "firstName", "lastName", "email", "phone", "dateOfBirth", "gender", "address" }` — dateOfBirth (must be in the past) and gender (`MALE`/`FEMALE`/`OTHER`) are required; address is optional
- `GET /api/patients` — list all; accepts an optional `q` query param that does a case-insensitive, partial (substring) match across firstName, lastName, the combined "firstName lastName", email, and phone (phone is matched with spaces/dashes normalized out of both sides). Matching is parameterized (no literal wildcard/injection risk from `q`), results are distinct and ordered by id ascending, and no matches returns `200` with `[]`. When `q` is omitted, behavior is unchanged (all patients). Note: search currently ignores any future role-based scoping.
- `GET /api/patients/{id}` — get one
- `PUT /api/patients/{id}` — update firstName/lastName/email/phone (always overwritten); dateOfBirth/gender/address are partial — only supplied fields are updated, and at least one of the three must be present
- `DELETE /api/patients/{id}`

### Doctors
- `POST /api/doctors` — `{ "firstName", "lastName", "specialization", "licenseNumber", "status" }` — firstName, lastName, and licenseNumber (must be unique) are required; specialization is optional; status (`ACTIVE`/`INACTIVE`) defaults to `ACTIVE` when omitted. A duplicate licenseNumber returns `409`.
- `GET /api/doctors` — list all
- `GET /api/doctors/{id}` — get one
- `PUT /api/doctors/{id}` — update firstName/lastName/specialization/licenseNumber/status (all overwritten); a licenseNumber collision with another doctor returns `409`
- `DELETE /api/doctors/{id}` — blocked with `409` if the doctor has any existing appointments (no soft-delete or reassignment flow yet)

### Appointments
- `POST /api/appointments` — `{ "patientId", "doctorId", "appointmentDate", "reason" }` — patientId and doctorId are both required and must reference existing records (`404` otherwise)
- `GET /api/appointments` — list all
- `GET /api/appointments/{id}` — get one
- `PUT /api/appointments/{id}` — update patientId/doctorId/appointmentDate/reason
- `DELETE /api/appointments/{id}`
