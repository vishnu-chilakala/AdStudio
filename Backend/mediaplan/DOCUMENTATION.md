# AdStudio — Media Plan & Insertion Order Management Service

**Module:** Media Plan & Insertion Order Management
**Owner:** Dev 3 (Prabhat A)
**Service name:** `mediaplan-service`
**Part of:** AdStudio — Digital Advertising & Campaign Management Platform

---

## 1. Overview

This microservice handles the **media planning and insertion order** side of AdStudio.
A media planner uses it to:

1. Build a **Media Plan** for a campaign brief (with a total budget).
2. Add **Media Line Items** to the plan — individual channel buys (Display, Video, etc.), each with its own budget, impressions target, and flight dates.
3. Generate **Insertion Orders** (IOs) — the formal orders sent to publishers to run the line items.
4. Track **delivery** against targets and automatically raise **Pacing Alerts** when a campaign is under-delivering, over-delivering, has exhausted its budget, or is near its flight end.

It backs two AdStudio portals:
- **Media Planner Console** (plan builder, line item scheduler, IO generator, pacing alert board)
- **Publisher Portal** (insertion order inbox + confirmation)

---

## 2. Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Data access | Spring Data JPA (Hibernate) |
| Database | MySQL 8 (`adstudio_mediaplan`) |
| Migrations | Flyway |
| Security | Spring Security (dev bypass — JWT added at integration) |
| Validation | Jakarta Bean Validation |
| Docs | springdoc-openapi (Swagger UI) |
| Build | Maven |
| Boilerplate | Lombok |
| Port | `8083` |

---

## 3. Architecture

Standard layered architecture. Each request flows top to bottom:

```
HTTP Request
   │
   ▼
CONTROLLER     → REST endpoints, returns ApiResponse        (@RestController)
   │
   ▼
SERVICE        → business logic, validation, workflow rules (@Service)
   │
   ▼
REPOSITORY     → database access (no SQL written by hand)   (JpaRepository)
   │
   ▼
ENTITY  ◄────► MySQL table                                  (@Entity)
```

### Package structure

```
com.cts.adstudio.mediaplanservice
├── controller     → REST controllers
├── service        → service interfaces
│   └── impl       → service implementations (business logic)
├── repository     → JPA repositories
├── entity         → JPA entities (DB tables)
├── dto
│   ├── request    → incoming request objects (validated)
│   └── response   → outgoing response objects
├── exception      → custom exceptions + global handler
├── config         → security config + pacing scheduler
└── shared         → reusable utilities (ApiResponse, AuditLogService,
                     PaginationHelper, StatusTransitionValidator)
```

---

## 4. Database Schema

Database: **`adstudio_mediaplan`** (separate DB per microservice).
All tables managed by Flyway migrations (`V1`–`V4`).

> **Note on cross-service fields:** `brief_id`, `planner_id`, and `publisher_id` are plain
> integers referencing data owned by *other* microservices (advertiser-service, user-service).
> They are **not** SQL foreign keys — those links are resolved via API calls during integration.

### 4.1 `media_plan` (owned)

| Column | Type | Notes |
|---|---|---|
| `plan_id` | INT, PK, AUTO_INCREMENT | Primary key |
| `brief_id` | INT, NOT NULL | Ref → CampaignBrief (advertiser-service) |
| `planner_id` | INT, NOT NULL | Ref → User (user-service) |
| `total_budget_allocated` | DECIMAL(15,2) | Plan's total budget cap |
| `channel_mix` | TEXT | Channels used in the plan |
| `start_date` | DATE | |
| `end_date` | DATE | |
| `status` | VARCHAR(20) | Draft / PendingApproval / Approved / Active / Completed |
| `created_at` | DATETIME | Auto-set |
| `updated_at` | DATETIME | Auto-updated |

### 4.2 `media_line_item` (owned)

| Column | Type | Notes |
|---|---|---|
| `line_item_id` | INT, PK, AUTO_INCREMENT | Primary key |
| `plan_id` | INT, NOT NULL, **FK → media_plan** | Parent plan |
| `channel` | VARCHAR(20) | Display / Video / Social / Search / OOH / Print / Radio |
| `publisher` | VARCHAR(100) | Publisher name |
| `format` | VARCHAR(50) | Ad format |
| `planned_impressions` | INT | Target impressions |
| `planned_budget` | DECIMAL(15,2) | Budget for this line |
| `cpm` | DECIMAL(8,2) | Cost per 1000 impressions |
| `flight_start` | DATE | Run start date |
| `flight_end` | DATE | Run end date |
| `status` | VARCHAR(20) | Planned / Ordered / Live / Paused / Completed |
| `created_at` | DATETIME | Auto-set |
| `updated_at` | DATETIME | Auto-updated |

### 4.3 `insertion_order` (owned)

| Column | Type | Notes |
|---|---|---|
| `io_id` | INT, PK, AUTO_INCREMENT | Primary key |
| `line_item_id` | INT, NOT NULL, **FK → media_line_item** | Line item being ordered |
| `publisher_id` | INT, NOT NULL | Ref → User (user-service) |
| `order_date` | DATE | |
| `start_date` | DATE | |
| `end_date` | DATE | |
| `committed_impressions` | INT | Impressions publisher commits to |
| `order_value` | DECIMAL(15,2) | Monetary value of the order |
| `status` | VARCHAR(20) | Sent / Confirmed / Rejected / Delivered / Disputed |
| `created_at` | DATETIME | Auto-set |
| `updated_at` | DATETIME | Auto-updated |

### 4.4 `pacing_alert` (owned)

| Column | Type | Notes |
|---|---|---|
| `alert_id` | INT, PK, AUTO_INCREMENT | Primary key |
| `line_item_id` | INT, NOT NULL | Line item the alert is about |
| `alert_type` | VARCHAR(30) | UnderDelivery / OverDelivery / BudgetExhausted / FlightEndApproaching |
| `alert_date` | DATE | When raised |
| `pacing_percent` | DECIMAL(6,2) | Delivered ÷ expected × 100 (for under/over) |
| `status` | VARCHAR(20) | Open / Actioned / Closed |
| `created_at` | DATETIME | Auto-set |
| `updated_at` | DATETIME | Auto-updated |

### 4.5 `audit_log` (shared utility table)

| Column | Type | Notes |
|---|---|---|
| `audit_id` | INT, PK, AUTO_INCREMENT | Primary key |
| `user_id` | INT | Who performed the action |
| `action` | VARCHAR(100), NOT NULL | e.g. `IO_STATUS_CHANGED_TO_Confirmed` |
| `entity_type` | VARCHAR(50) | e.g. `InsertionOrder` |
| `entity_id` | INT | Affected record ID |
| `timestamp` | DATETIME | Auto-set |

### 4.6 `delivery_record` (TEMPORARY — for standalone testing)

> Will be **replaced by Dev 4's delivery-service** during integration. Kept locally so the
> pacing engine can be developed and tested independently.

| Column | Type | Notes |
|---|---|---|
| `delivery_id` | INT, PK, AUTO_INCREMENT | Primary key |
| `line_item_id` | INT, NOT NULL | Line item delivered against |
| `reporting_date` | DATE | |
| `delivered_impressions` | INT | Impressions delivered so far |
| `spend` | DECIMAL(15,2) | Amount spent so far |
| `created_at` | DATETIME | Auto-set |

### Entity Relationships

```
media_plan (1) ──< media_line_item (1) ──< insertion_order
                          │
                          ├──< pacing_alert        (by line_item_id)
                          └──< delivery_record     (by line_item_id, temporary)

audit_log  → standalone (records actions across the service)
```

---

## 5. Features

| # | Feature | Description |
|---|---|---|
| 1 | **Media Plan CRUD** | Create, read, update, delete, list (paginated) media plans |
| 2 | **Line Item CRUD** | Manage channel buys under a plan |
| 3 | **Insertion Order management** | Generate IOs, list by publisher (IO inbox), confirm/reject |
| 4 | **Budget validation** | Sum of line item budgets cannot exceed the plan's total budget |
| 5 | **Channel validation** | Channel must be a valid enum value |
| 6 | **Status transition validation** | Statuses follow legal workflows (no illegal jumps) |
| 7 | **Audit logging** | Every IO create/status-change is recorded in `audit_log` |
| 8 | **Pacing alert engine** | Scheduled daily job raises alerts on delivery problems |
| 9 | **Delivery recording** | Record delivered impressions + spend (temporary, for testing) |
| 10 | **Pagination** | List endpoints return paged results |
| 11 | **Global exception handling** | Consistent error responses (400/404/500) |
| 12 | **Swagger / OpenAPI** | Auto-generated interactive API docs |

---

## 6. Status Workflows

Enforced by the shared `StatusTransitionValidator`. Illegal jumps return `400 Bad Request`.

### Media Plan
```
Draft → PendingApproval → Approved → Active → Completed
PendingApproval → Draft   (can be sent back)
```

### Media Line Item
```
Planned → Ordered → Live → Completed
Live ⇄ Paused
Paused → Completed
```

### Insertion Order
```
Sent → Confirmed → Delivered → Disputed
Sent → Rejected
Confirmed → Disputed
```

---

## 7. Pacing Alert Engine

A scheduled job (`@Scheduled`, daily at 01:00) — also triggerable manually via
`POST /api/pacing-alerts/run` — checks every active line item.

### Logic
```
flightProgress = days elapsed ÷ total flight days
expectedByNow  = plannedImpressions × flightProgress
pacingPercent  = deliveredImpressions ÷ expectedByNow × 100
```

### Alert rules
| Alert Type | Condition |
|---|---|
| `UnderDelivery` | pacingPercent < 80% |
| `OverDelivery` | pacingPercent > 110% |
| `BudgetExhausted` | total spend ≥ planned budget |
| `FlightEndApproaching` | flight ends within 3 days |

Duplicate **Open** alerts of the same type for the same line item are **not** re-created.

---

## 8. API Reference

Base URL: `http://localhost:8083`
All responses are wrapped in a standard `ApiResponse` envelope:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "timestamp": "2026-06-03T10:00:00"
}
```

### Media Plans
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/media-plans` | Create a media plan |
| GET | `/api/media-plans` | List all (paginated: `?page=0&size=20&sort=...`) |
| GET | `/api/media-plans/{id}` | Get one |
| PUT | `/api/media-plans/{id}` | Update |
| PUT | `/api/media-plans/{id}/status` | Change status |
| DELETE | `/api/media-plans/{id}` | Delete |

### Media Line Items
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/media-plans/{planId}/line-items` | Create line item under a plan |
| GET | `/api/media-plans/{planId}/line-items` | List line items of a plan |
| GET | `/api/line-items/{id}` | Get one |
| PUT | `/api/line-items/{id}` | Update |
| PUT | `/api/line-items/{id}/status` | Change status |
| DELETE | `/api/line-items/{id}` | Delete |

### Insertion Orders
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/insertion-orders` | Generate an insertion order |
| GET | `/api/insertion-orders` | List all (or `?publisherId=` for publisher inbox) |
| GET | `/api/insertion-orders/{id}` | Get one |
| PUT | `/api/insertion-orders/{id}/status` | Confirm/Reject/etc. (audited) |

### Pacing Alerts
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/pacing-alerts` | List alerts (or `?status=Open`) |
| PUT | `/api/pacing-alerts/{id}/status` | Action/close an alert |
| POST | `/api/pacing-alerts/run` | Manually trigger the pacing engine |

### Delivery Records (temporary)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/delivery-records` | Record delivered impressions + spend |
| GET | `/api/line-items/{lineItemId}/delivery` | List delivery records for a line item |

---

## 9. Request Examples

### Create a Media Plan
```
POST /api/media-plans
```
```json
{
  "briefId": 1,
  "plannerId": 2,
  "totalBudgetAllocated": 100000.00,
  "channelMix": "Display, Social, Video",
  "startDate": "2026-07-01",
  "endDate": "2026-07-31"
}
```

### Create a Line Item
```
POST /api/media-plans/1/line-items
```
```json
{
  "channel": "Display",
  "publisher": "Times Network",
  "format": "Banner 728x90",
  "plannedImpressions": 1000000,
  "plannedBudget": 25000.00,
  "cpm": 25.00,
  "flightStart": "2026-07-01",
  "flightEnd": "2026-07-15"
}
```

### Generate an Insertion Order
```
POST /api/insertion-orders
```
```json
{
  "lineItemId": 1,
  "publisherId": 5,
  "orderDate": "2026-06-20",
  "startDate": "2026-07-01",
  "endDate": "2026-07-15",
  "committedImpressions": 1000000,
  "orderValue": 25000.00
}
```

### Change a Status (any entity)
```
PUT /api/media-plans/1/status
```
```json
{ "status": "Approved" }
```

### Record Delivery (temporary)
```
POST /api/delivery-records
```
```json
{
  "lineItemId": 1,
  "reportingDate": "2026-07-05",
  "deliveredImpressions": 300000,
  "spend": 7500.00
}
```

---

## 10. Shared Utilities (`shared` package)

| Class | Purpose |
|---|---|
| `ApiResponse<T>` | Standard response envelope for every endpoint |
| `PagedResponse<T>` | Paged list payload (content + page metadata) |
| `PaginationHelper` | Converts a Spring `Page` of entities into a `PagedResponse` of DTOs |
| `AuditLogService` | `log(userId, action, entityType, entityId)` — writes to `audit_log` |
| `StatusTransitionValidator` | Validates legal status transitions for all 3 entities |

---

## 11. Configuration (`application.properties`)

| Property | Value |
|---|---|
| `server.port` | `8083` |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/adstudio_mediaplan` |
| `spring.jpa.hibernate.ddl-auto` | `validate` (Flyway owns the schema) |
| `spring.flyway.locations` | `classpath:db/migration` |
| `springdoc.api-docs.path` | `/api/docs` |
| Swagger UI | `http://localhost:8083/swagger-ui/index.html` |

---

## 12. How to Run

1. Ensure MySQL is running and the database exists:
   ```sql
   CREATE DATABASE IF NOT EXISTS adstudio_mediaplan;
   ```
2. Set your DB username/password in `src/main/resources/application.properties`.
3. Run the app (VS Code Run, or `mvnw spring-boot:run`).
4. Flyway auto-creates all tables on first start.
5. Open Swagger UI: `http://localhost:8083/swagger-ui/index.html`

---

## 13. Integration Notes (for the team / Phase 2)

These are intentionally **deferred** until teammates' services exist:

| Item | Depends on | Action at integration |
|---|---|---|
| Validate `brief_id` & plan-vs-brief budget | Dev 2 (advertiser-service) | Add FeignClient call to fetch CampaignBrief |
| Validate `planner_id` / `publisher_id` | Dev 1 (user-service) | Add FeignClient call to fetch User |
| Real delivery data for pacing | Dev 4 (delivery-service) | Replace local `delivery_record` lookups with a FeignClient call |
| Notifications when alerts fire | Dev 5 | Publish notification events |
| JWT auth + RBAC | Dev 1 | Replace dev `SecurityConfig` with JWT filter |
| Service discovery & routing | Common modules | Register with Eureka, route via API Gateway, pull config from Config Server |

**Conventions agreed with the team:**
- Base package: `com.cts.adstudio.<module>`
- Database per service (this one: `adstudio_mediaplan`)
- Cross-service references stored as plain ID fields (not JPA relationships)
- Spring Boot version: 4.0.6
- Standard `ApiResponse` envelope on all endpoints

---

## 14. Summary

This service is **functionally complete and tested standalone**. It implements all
Dev 3 responsibilities from the backend plan: the three core entities with full CRUD,
budget and channel validation, status-transition workflow enforcement, audit logging,
pagination, and the pacing-alert engine — plus auto-generated Swagger documentation.
Remaining work is **cross-service integration**, to be done once teammates' services
are ready.
