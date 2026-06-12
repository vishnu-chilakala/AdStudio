# AdStudio — Billing Service (microservice)

The **Billing & Reconciliation** microservice (Dev 5): `ClientInvoice` + `PublisherInvoice`, the Finance payment tracker, and the billing calendar. A **standalone, independently deployable Spring Boot service** — its own `pom.xml`, its own `main`, its own **MySQL** database, its own JWT validation, and Swagger docs. It references advertisers, briefs, publishers and insertion orders **by id** (those live in other services) and calls the Delivery service over HTTP for delivered figures.

> Datastore: **MySQL only**. There is no embedded/other database in any profile.

---

## Run it

**Prerequisites:** JDK 17+, Maven, and a running **MySQL 8** instance.

**1. Create the service's database** (it owns its own schema):
```sql
CREATE DATABASE adstudio_billing;
```
Default credentials expected are `adstudio` / `adstudio` (override with `DB_USER` / `DB_PASSWORD`).

**2a. Local run — `dev` profile (MySQL + permissive security).** Lets you call endpoints without a JWT:
```bash
cd adstudio-billing
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**2b. Default run — MySQL + JWT enforced:**
```bash
export JWT_SECRET=<the same HS256 secret the IAM service signs with>
export DELIVERY_SERVICE_URL=http://delivery-service:8083
mvn spring-boot:run
```

On startup Flyway applies `V16`/`V17`, and `ddl-auto=validate` checks the entities against the schema. The service runs on **`http://localhost:8084`**.

**Swagger / OpenAPI:**
- Swagger UI → `http://localhost:8084/swagger-ui.html`
- OpenAPI JSON → `http://localhost:8084/api/docs`
- Click **Authorize** and paste a Bearer JWT to call the secured endpoints (not needed under the `dev` profile).

**Build a jar / run tests:**
```bash
mvn clean package
java -jar target/billing-service-0.1.0.jar
mvn test          # unit tests: no DB, no Spring context
```

`generate` and `reconcile` call the Delivery service; everything else works against MySQL alone.

---

## Cross-service contracts

**JWT (IAM service).** This service is a resource server: it validates HS256 tokens with the shared `jwt.secret` and reads a `role` claim, mapping it to a `ROLE_*` authority used by `@PreAuthorize`. Expected role values: `FINANCE`, `ADMIN`, `ADVERTISER_BRAND`, `PUBLISHER` — align the claim name and secret with the IAM service's `JwtTokenProvider`.

**Delivery service (HTTP).** `generate` and `reconcile` get delivered figures from the Delivery service via `DeliveryServiceBudgetCalculation`, configured by `adstudio.services.delivery.base-url`. Assumed endpoints (returning the standard `ApiResponse` envelope with a numeric `data`) — align with Dev 4's API:
```
GET {delivery}/api/delivery/campaigns/{briefId}/delivered-spend
GET {delivery}/api/delivery/insertion-orders/{ioId}/delivered-value
```

**Audit.** `LoggingAuditLogService` writes a local audit line; swap it for an event publish / call to a central audit service without touching the rest (the services depend only on the `AuditLogService` interface).

---

## Layout

```
adstudio-billing/
├── pom.xml                                  standalone service build (MySQL only)
├── src/main/resources/
│   ├── application.yml                       MySQL datasource; dev profile = open security
│   └── db/migration/V16,V17                   Flyway: own tables, no cross-service FKs
└── src/main/java/com/adstudio/
    ├── BillingServiceApplication.java         @SpringBootApplication (main)
    ├── billing/
    │   ├── entity/        ClientInvoice, PublisherInvoice
    │   ├── enums/         ClientInvoiceStatus, PublisherInvoiceStatus
    │   ├── repository/    Spring Data repositories
    │   ├── dto/           request/response records (entities never exposed)
    │   ├── service/       ClientInvoiceService, PublisherInvoiceService
    │   ├── controller/    Client / Publisher / BillingCalendar controllers
    │   ├── config/        SecurityConfig (JWT), DevSecurityConfig, OpenApiConfig, BillingStatusTransitions
    │   ├── integration/   LoggingAuditLogService, DeliveryServiceBudgetCalculation
    │   └── exception/     InvoiceNotFoundException (404), BillingRuleException (422)
    └── shared/            this service's common layer: ApiResponse,
                           StatusTransitionValidator, AuditLogService +
                           BudgetCalculationService (interfaces), exception handler
```

`shared/*` is this service's common code; in a larger estate you'd publish it as an `adstudio-common` library and depend on it instead of vendoring a copy per service.

---

## Endpoints & RBAC

Roles map to `ROLE_FINANCE` / `ROLE_ADMIN` / `ROLE_ADVERTISER_BRAND` / `ROLE_PUBLISHER`. Status changes use `PUT .../{id}/status` with body `{ "status": "ISSUED" }`. All endpoints are browsable in Swagger UI.

| Method | Path | Roles |
|---|---|---|
| POST | `/api/client-invoices` | FINANCE, ADMIN |
| POST | `/api/client-invoices/generate` | FINANCE, ADMIN |
| GET | `/api/client-invoices/{id}` | FINANCE, ADMIN, ADVERTISER_BRAND |
| GET | `/api/client-invoices?status=&page=&size=` | FINANCE, ADMIN, ADVERTISER_BRAND |
| PUT | `/api/client-invoices/{id}` | FINANCE, ADMIN |
| PUT | `/api/client-invoices/{id}/status` | FINANCE, ADMIN |
| GET | `/api/client-invoices/summary?advertiserId=` | FINANCE, ADMIN, ADVERTISER_BRAND |
| POST | `/api/publisher-invoices` | PUBLISHER, ADMIN |
| GET | `/api/publisher-invoices/{id}` | PUBLISHER, FINANCE, ADMIN |
| GET | `/api/publisher-invoices?publisherId=&status=&page=&size=` | PUBLISHER, FINANCE, ADMIN |
| PUT | `/api/publisher-invoices/{id}/reconcile` | FINANCE, ADMIN |
| PUT | `/api/publisher-invoices/{id}/status` | FINANCE, ADMIN |
| GET | `/api/invoices/calendar?month=YYYY-MM` | FINANCE, ADMIN, ADVERTISER_BRAND |

`postman/AdStudio_Billing.postman_collection.json` also covers all of them.

---

## Status machines

Registered into `StatusTransitionValidator` at startup by `BillingStatusTransitions` (no inline status checks). A disallowed transition returns HTTP 422.

- **ClientInvoice:** `DRAFT -> ISSUED -> {PAID | DISPUTED | OVERDUE}`; `OVERDUE -> {PAID | DISPUTED}`; `DISPUTED -> {ISSUED | PAID}`; `PAID` terminal. Moving to `ISSUED` stamps `issuedDate`.
- **PublisherInvoice:** `RECEIVED -> {RECONCILED | DISCREPANCY}`; `DISCREPANCY -> RECONCILED`; `RECONCILED -> PAID`; `PAID` terminal.

---

## Confirm the commission model

`ClientInvoiceService.computeCommercials` bills commission **on top**:
```
agencyCommission = invoiceAmount * commissionRate     (default 15%)
netBillable      = invoiceAmount + agencyCommission
```
If the agency is commission-inclusive, change the `netBillable` line to subtract and flip the matching tests. Confirm with Finance/BA.

## Known decisions (from the 17-table schema)

- **No `Payment` table** -> "paid" is a status; the payment tracker derives totals from invoice status. Partial payments need a payments table.
- **No `DueDate`** -> `OVERDUE` is set explicitly via the status endpoint, not computed.
- **No cross-service DB foreign keys** -> `advertiser_id` / `campaign_brief_id` / `publisher_id` / `io_id` are plain indexed columns; cross-service integrity is enforced at the application/contract level.
