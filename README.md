# ERASM — Enterprise Resource Allocation & Skill Management System

A Spring Boot 3.3 / Java 17 REST API built to satisfy the ERASM Java competency
assessment requirements: 10 functional modules, JWT + role-based security,
Hibernate/JPA with MySQL, SLF4J logging, global exception handling, and JUnit/Mockito tests.

## 1. Project Overview

ERASM manages how employees' skills are tracked and how they get allocated to
client projects, from a manager raising a resource request through to
approval and allocation, with full auditing and utilization reporting along
the way.

## 2. Tech Stack

| Concern            | Choice                                   |
|---------------------|-------------------------------------------|
| Language / Runtime  | Java 17                                    |
| Framework           | Spring Boot 3.3.4                          |
| Security            | Spring Security 6, JWT (jjwt 0.12.6), BCrypt |
| Persistence         | Spring Data JPA / Hibernate, MySQL 8       |
| Logging             | SLF4J + Logback                            |
| Testing             | JUnit 5, Mockito                           |
| API Docs            | springdoc-openapi (Swagger UI)             |
| Build tool          | Maven                                      |

## 3. Functional Modules (from the requirements document)

1. **User Management** — register/login/update/delete users, change password, assign roles
2. **Skill Management** — Admin CRUD on the master skill list
3. **Employee Skill Profile** — employees add skills, proficiency level, experience, certifications
4. **Project Management** — create/update/close projects, assign technology stack
5. **Resource Request Management** — Delivery Managers raise requests for a project + skill + count
6. **Approval Workflow** — `DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED → ALLOCATED → COMPLETED` (with `REJECTED` from `UNDER_REVIEW`)
7. **Resource Allocation** — allocate/reallocate/release employees, with a **100% total allocation cap enforced across ALL of an employee's active projects**, not just the one being requested
8. **Utilization Dashboard** — billable % and bench % per employee
9. **Audit Management** — every critical create/update/delete/approve/allocate action is written to `audit_logs`
10. **Reports** — Skill Report, Utilization Report, Project Allocation Report

## 4. Security Model

Five roles, seeded automatically on first startup: `ADMIN`, `DELIVERY_MANAGER`,
`RESOURCE_MANAGER`, `EMPLOYEE`, `AUDITOR`.

A default admin account is also seeded automatically:
```
email:    admin@erasm.com
password: Admin@123
```
Use this to log in first, then register/promote other users as needed.

Every protected endpoint uses `@PreAuthorize("hasRole('...')")` /
`hasAnyRole(...)` matching the module's business rules (see the controller
classes for the exact role matrix per endpoint).

## 5. Database Design

10 tables exactly as specified in the requirements document:
`users, roles, employees, skills, employee_skill_profile (Employee↔Skill join
with proficiency data — kept distinct in name from a plain join table to
avoid a naming clash), certifications, projects, resource_requests,
allocations, audit_logs`.

JPA relationships implemented:
- **One-To-One**: `User ↔ Employee`
- **One-To-Many**: `Project → ResourceRequest`
- **Many-To-One**: `Employee → Role`
- **Many-To-Many**: `Employee ↔ Skill` (via the `EmployeeSkill` association entity, which also carries proficiency level and years of experience)

See `database/erasm_database_script.sql` for the full DDL + seed data (this is
also created automatically by Hibernate on first run, so running the script
manually is optional).

## 6. Getting Started

See `SIMPLE_STEPS_GUIDE.md` for full click-by-click Spring Tool Suite / Eclipse
instructions. Quick summary:

1. Extract this zip into your STS/Eclipse workspace and import as an
   **Existing Maven Project**.
2. Create the MySQL database (or let Hibernate do it automatically — see
   `application.properties`, `createDatabaseIfNotExist=true`).
3. Update `src/main/resources/application.properties` with your own MySQL
   username/password if different from `root` / `root`.
4. Run `ErasmApplication.java` as a Java Application.
5. On first startup, the app auto-seeds the 5 roles + a default `ADMIN` user.
6. Import `postman/ERASM_Postman_Collection.json` into Postman and start with
   the **01 - Authentication → Login - Default Admin** request; the token is
   saved automatically into the collection variables for every other request.

Swagger UI (optional, for exploring the API in the browser):
```
http://localhost:8080/swagger-ui.html
```

## 7. Logging

SLF4J + Logback logs to console and to `logs/erasm.log`. `INFO` for normal
business events (login, project creation, allocation), `WARN` for invalid
requests / unauthorized access, `ERROR` for unhandled exceptions. Passwords,
JWT tokens, and other personal information are never logged (see
`JwtUtil` / `JwtAuthenticationFilter` for how validation failures are logged
without exposing the token).

## 8. Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) centralizes all error
responses into a consistent JSON shape (`status`, `error`, `message`, `path`,
`timestamp`), including bean-validation field errors. Custom exceptions:
`UserNotFoundException`, `ProjectNotFoundException`, `SkillNotFoundException`,
`AllocationException`, plus a few supporting ones
(`EmployeeNotFoundException`, `ResourceRequestNotFoundException`,
`InvalidWorkflowStateException`, `DuplicateResourceException`).

## 9. Testing

`mvn test` runs the JUnit/Mockito suite in `src/test/java`, focused on the
business rules most worth defending in an assessment: the corrected 100%
allocation cap logic, the approval workflow state machine, skill uniqueness
validation, and registration/login validation.

## 10. Deliverables Included In This Zip

- Full Maven source project (`src/main/java`, `src/main/resources`)
- `database/erasm_database_script.sql` — database creation script
- `postman/ERASM_Postman_Collection.json` — Postman collection for every API
- `src/test/java` — JUnit/Mockito test report source (run `mvn test` to generate the execution report)
- This `README.md` and `SIMPLE_STEPS_GUIDE.md`

Still to prepare on your side for full submission per the rubric: ER diagram,
screenshots, and the GitHub repository link/push (see Section 6 of the
guide for a quick `git init` walkthrough).

## 11. Update Log (v2)

Everything from v1 is unchanged and still works exactly as before. This
update is purely additive:

- **Dedicated Certification module**: `CertificationService` (interface),
  `CertificationServiceImpl`, `CertificationController`
  (`/api/certifications/**`) — full CRUD, separate from (and in addition to)
  the nested endpoints already under `/api/employees/{id}/certifications`.
- **Dedicated Employee Skill module**: `EmployeeSkillService` (interface),
  `EmployeeSkillServiceImpl`, `EmployeeSkillController`
  (`/api/employee-skills/**`) — full CRUD, separate from (and in addition to)
  the nested endpoints already under `/api/employees/{id}/skills`.
- Two new custom exceptions wired into `GlobalExceptionHandler`:
  `CertificationNotFoundException`, `EmployeeSkillNotFoundException`.
- **Module 9 activity tracking endpoints**: `GET /api/audit-logs/recent` (last
  50 audit entries system-wide) and `GET /api/audit-logs/user/{email}`
  (activity by a specific user).
- **Module 10 report filtering**: `GET /api/reports/skill-report?skillName=`
  and `GET /api/reports/project-allocation-report?projectId=` now accept
  optional filters; calling them with no query params behaves exactly as
  before.
- Extra `WARN`-level SLF4J logging on the two most safety-critical rejections
  (the 100% allocation cap breach, and invalid workflow transitions).
- 6 additional JUnit/Mockito test classes: `CertificationServiceImplTest`,
  `EmployeeSkillServiceImplTest`, `DashboardServiceTest`, `ProjectServiceTest`,
  `UserServiceTest` (on top of the original `AllocationServiceTest`,
  `ResourceRequestServiceTest`, `SkillServiceTest`, `AuthServiceTest`).
- Postman collection gained a new **"04b - Certification & Employee Skill
  (Dedicated APIs)"** folder plus extra requests in the Audit and Reports
  folders — all existing requests/folders are untouched.

