# clinic-api

Phase 0 of the clinic appointment plan: bare-minimum scaffold to prove clean generation — `Patient` and `Appointment` with full CRUD and a single FK relation. No RBAC, no capabilities, no search. Later phases (Doctor, KYC-style patient fields, RBAC, appointment/doctor status workflows, round-robin assignment, a companion billing service, reporting) build on top of this.

## Domain model

- **Patient** — firstName, lastName, email (unique), phone, status (`ACTIVE` / `INACTIVE` / `SUSPENDED`, defaults to `ACTIVE`)
- **Appointment** — belongs to a Patient; appointmentDate, reason, status (`SCHEDULED` / `CONFIRMED` / `COMPLETED` / `CANCELLED` / `NO_SHOW`, defaults to `SCHEDULED` — the full enum is already here so Phase 2 can add transition validation without reshaping the field)

Status isn't writable through the API yet in Phase 0 — it's just tracked. Phase 2 is expected to add the transition capability on top.

## Run

Uses a local Postgres.app instance on port 5432 (see `food-delivery-api`'s README in this same folder for why, if you hit port conflicts). One-time setup:

```
psql -h localhost -p 5432 -U "$(whoami)" -d postgres -c "CREATE ROLE clinic WITH LOGIN PASSWORD 'clinic';"
psql -h localhost -p 5432 -U "$(whoami)" -d postgres -c "CREATE DATABASE clinic OWNER clinic;"
```

Then run:

```
mvn spring-boot:run
```

Listens on `http://localhost:4006`.

## Tests

```
mvn test
```

Tests run against an in-memory H2 database (`src/test/resources/application.properties`), so they don't need Postgres running.

## Endpoints

### Patients
- `POST /api/patients` — `{ "firstName", "lastName", "email", "phone" }`
- `GET /api/patients` — list all
- `GET /api/patients/{id}` — get one
- `PUT /api/patients/{id}` — update firstName/lastName/email/phone
- `DELETE /api/patients/{id}`

### Appointments
- `POST /api/appointments` — `{ "patientId", "appointmentDate", "reason" }`
- `GET /api/appointments` — list all
- `GET /api/appointments/{id}` — get one
- `PUT /api/appointments/{id}` — update patientId/appointmentDate/reason
- `DELETE /api/appointments/{id}`
