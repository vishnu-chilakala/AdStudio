# AdStudio - Identity & Access Management (IAM) Service

Backend module for the **AdStudio** digital advertising platform, implementing
**Epic 4.1 - Identity & Access Management**: Authentication, RBAC, and Audit Trails.

## Stories implemented
| Story | Endpoint(s) |
|-------|-------------|
| User Registration API | `POST /api/auth/register` |
| Login API | `POST /api/auth/login` |
| Audit Log API | `POST /api/audit-logs`, `GET /api/audit-logs`, `GET /api/audit-logs/{id}`, `GET /api/audit-logs/user/{userId}` |

## Tech stack
- Java 17, Spring Boot 3.3.x
- Spring Web, Spring Data JPA, Spring Security (JWT, stateless)
- MySQL (single database: `IamDB`)
- Lombok, Bean Validation
- springdoc-openapi (Swagger UI)

## Project structure
```
src/main/java/com/adstudio/iam
├── IamApplication.java          # Spring Boot entry point
├── config/                      # SecurityConfig, OpenApiConfig, DataInitializer
├── controller/                  # AuthController, AuditLogController
├── dto/
│   ├── request/                 # RegisterRequest, LoginRequest, AuditLogRequest
│   └── response/                # UserResponse, LoginResponse, AuditLogResponse
├── entity/                      # User, AuditLog (JPA entities)
├── enums/                       # Role, UserStatus
├── exception/                   # GlobalExceptionHandler + custom exceptions
├── repository/                  # UserRepository, AuditLogRepository (JpaRepository)
├── security/                    # JwtService, JwtAuthenticationFilter, CustomUserDetails(+Service)
└── service/                     # AuthService, AuditLogService (+ impl/)
```

## Prerequisites
- JDK 17+
- Maven 3.9+
- MySQL running on `localhost:3306`

The database `IamDB` is created automatically on first run
(`createDatabaseIfNotExist=true`). Update `spring.datasource.username` /
`spring.datasource.password` in `src/main/resources/application.properties`
to match your MySQL credentials.

## Run
```bash
mvn spring-boot:run
```
The app starts on `http://localhost:8080`.

## Swagger UI
Open: `http://localhost:8080/swagger-ui.html`
Click **Authorize**, paste the JWT from `/api/auth/login`, and call the secured endpoints.

## Default admin
On first startup a default admin is seeded (dev only):
- email: `admin@adstudio.com`
- password: `Admin@123`

**Change or remove this in production** (see `config/DataInitializer.java`).

## Quick test (curl)

Register a user:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Asha Rao","email":"asha@brand.com","password":"secret123","phone":"9876543210","role":"MEDIA_PLANNER"}'
```

Log in (returns a JWT):
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@adstudio.com","password":"Admin@123"}'
```

List audit logs (ADMIN token required):
```bash
curl http://localhost:8080/api/audit-logs \
  -H "Authorization: Bearer <PASTE_JWT_HERE>"
```

## Notes
- Passwords are stored as BCrypt hashes; raw passwords are never persisted.
- `Role` is mapped to a Spring Security authority as `ROLE_<NAME>` (e.g. `ROLE_ADMIN`).
- `INACTIVE` / `SUSPENDED` users are blocked at login.
- Registration and login automatically write `REGISTER_USER` / `LOGIN` audit entries.
- `accountId` on `User` is a plain optional value (no foreign key), matching the schema.
