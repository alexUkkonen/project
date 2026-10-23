# Moodle Deadline Calendar

**Non functional outside intended environment. Built to utilize SQL from Jukka Juslin's server localy**

This is a Spring Boot web application that lets students or teachers register, store the private iCal export URL of their Moodle calendar, and view all upcoming Moodle deadlines in an interactive monthly/weekly/list calendar. The program is intended to be run locally on home computers, but has been modifyed to run on a server for the final assignment. Each user's Moodle URL is encrypted at rest, passwords are BCrypt‑hashed, and an e‑mail based password‑reset flow is included.

> **Package root:** `fi.haagahelia.project`
> **Build tool:** Maven (wrapper 3.3.4 → Apache Maven 3.9.12)
> **Runtime:** Spring Boot 3.x (Jakarta EE namespace), Java 17+

---

### Table of Contents

1. [Features](#features)
2. [Technology Stack](#technology-stack)
3. [Architecture Overview](#architecture-overview)
4. [Project Structure](#project-structure)
5. [Code Navigation Guide](#code-navigation-guide)
6. [Request Flows](#request-flows)
7. [Data Model](#data-model)
8. [Configuration](#configuration)
9. [Getting Started](#getting-started)
10. [Endpoint Reference](#endpoint-reference)
11. [Known Issues & Security Notes](#known-issues--security-notes)
12. [Contributing Conventions](#contributing-conventions)

---

### Features

- **User registration & login** – form‑based authentication backed by a MySQL user table.
- **Per‑user Moodle calendar** – each account stores one Moodle iCal export URL.
- **Deadline calendar view** – fetches the iCal feed on every page load, filters out "attendance" events, and renders deadlines with [FullCalendar](https://fullcalendar.io/) (month / week / list views).
- **Encrypted URL storage** – the Moodle URL is AES‑encrypted before it is persisted and decrypted only when needed.
- **Account settings** – change the Moodle URL or permanently delete the account.
- **Forgot / reset password** – time‑limited (15 min) single‑use token e‑mailed via SMTP.
- **HTTPS out of the box** – the embedded server is configured with a PKCS12 keystore.

---

### Technology Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot (Web MVC, Data JPA, Security, Mail) |
| Templating | Thymeleaf (`th:*` attributes, inline JS for calendar data) |
| Persistence | Spring Data JPA / Hibernate → MySQL (`com.mysql.cj.jdbc.Driver`) |
| Security | Spring Security 6 (`SecurityFilterChain`, BCrypt) |
| iCal parsing | [biweekly](https://github.com/mangstadt/biweekly) |
| HTTP client | `java.net.http.HttpClient` (JDK 11+) |
| Front‑end calendar | FullCalendar 6.1.11 (CDN) |
| E‑mail | Spring `JavaMailSender` over SMTP (STARTTLS) |
| Build | Maven Wrapper (`mvnw`) |

---

### Architecture Overview

The application is a classic three‑layer Spring MVC app:

```
Browser
   │  HTTPS (port 7010)
   ▼
┌──────────────────────── Spring Security filter chain ────────────────────────┐
│  public: /login /register /forgot-password /reset-password   everything else │
│                                                               → authenticated│
└──────────────────────────────────────────────────────────────────────────────┘
   │
   ▼
Controllers  (fi.haagahelia.project.web)
   UserController · calendarController · PasswordResetController
   │
   ▼
Services     (fi.haagahelia.project.service)
   CalendarService · EmailService · UserDetailsServiceImpl
   │                       │
   │                       └──► SMTP server
   └──► Moodle iCal feed (HTTP)
   │
   ▼
Repositories (fi.haagahelia.project.repository)
   AppUserRepo · PasswordResetTokenRepo
   │
   ▼
MySQL  (AppUser, PasswordResetToken tables)

Cross‑cutting (fi.haagahelia.project.config)
   SecurityConfig · EncryptionUtil
```

---

### Project Structure

The uploaded sources map onto the standard Maven layout as follows:

```
project/
├── .mvn/wrapper/
│   └── maven-wrapper.properties          # Maven wrapper version pin
├── src/main/java/fi/haagahelia/project/
│   ├── ProjectApplication.java           # @SpringBootApplication entry point
│   ├── config/
│   │   ├── SecurityConfig.java           # Security filter chain + BCrypt bean
│   │   └── EncryptionUtil.java           # AES encrypt/decrypt helper
│   ├── model/
│   │   ├── AppUser.java                  # JPA entity – registered user
│   │   ├── PasswordResetToken.java       # JPA entity – reset token
│   │   └── Event.java                    # Plain DTO – a calendar deadline
│   ├── repository/
│   │   ├── AppUserRepo.java              # CrudRepository<AppUser, Long>
│   │   └── PasswordResetTokenRepo.java   # CrudRepository<PasswordResetToken, Long>
│   ├── service/
│   │   ├── CalendarService.java          # Fetch + parse Moodle iCal feed
│   │   ├── EmailService.java             # Send password‑reset e‑mail
│   │   └── UserDetailsServiceImpl.java   # Bridges AppUser → Spring Security
│   └── web/
│       ├── UserController.java           # /login /register /settings
│       ├── calendarController.java       # /calendar
│       └── PasswordResetController.java  # /forgot-password /reset-password
└── src/main/resources/
    ├── application.properties            # DB, SSL, mail, encryption key, logging
    ├── keystore.p12                      # PKCS12 TLS keystore
    └── templates/
        ├── login.html
        ├── register.html
        ├── calendar.html                 # FullCalendar page
        ├── settings.html
        ├── forgot-password.html
        └── reset-password.html
```

---

### Code Navigation Guide

Use this section to jump straight to the file that owns a given responsibility.

#### Entry point

| File | What to look at |
|---|---|
| `ProjectApplication.java` | `main()` boots Spring. Note: after `SpringApplication.run()` it also constructs a `CalendarService` by hand and calls `fetchEvents("")` — this is leftover debug code and does nothing useful (see *Known Issues*). |

#### Configuration (`config/`)

| File | Key members | Purpose |
|---|---|---|
| `SecurityConfig.java` | `passwordEncoder()` | Exposes a `BCryptPasswordEncoder` bean used by registration and password reset. |
| | `filterChain(HttpSecurity)` | Whitelists `/register`, `/login`, `/forgot-password`, `/reset-password`; requires authentication elsewhere; custom login page `/login`; success redirect to `/calendar`; logout permitted for all. CSRF is left at Spring's default (enabled), which is why every template includes a hidden `_csrf` field. |
| `EncryptionUtil.java` | `init()` (`@PostConstruct`) | Base64‑decodes `app.security.encryption.key` into raw key bytes at startup. |
| | `encrypt(String)` / `decrypt(String)` | AES (`Cipher.getInstance("AES")`) round‑trip, Base64‑encoded output. Throws `RuntimeException` on any failure. |

#### Domain model (`model/`)

| File | Notes |
|---|---|
| `AppUser.java` | `@Entity`. Fields: `id`, `username` (unique, not null), `passwordHash` (not null), `email` (not null), `moodleUrl` (length 1000, stores the **encrypted** URL). |
| `PasswordResetToken.java` | `@Entity`. `token` (unique), `@OneToOne` → `AppUser` via `user_id`, `expiryDate` (`LocalDateTime`). |
| `Event.java` | Not persisted. Simple DTO with `title`, `description`, `dueDate` (`java.util.Date`) that is serialised into the calendar page. |

#### Repositories (`repository/`)

| File | Derived queries |
|---|---|
| `AppUserRepo.java` | `findByUsername(String)` – used by authentication, settings, calendar. `findByEmail(String)` – used by forgot‑password. |
| `PasswordResetTokenRepo.java` | `findByToken(String)` – validate a reset link. `findByUser(AppUser)` – find/replace an existing token or clean up on account deletion. |

#### Services (`service/`)

| File | Key method | What it does |
|---|---|---|
| `CalendarService.java` | `fetchEvents(String calUrl)` | 1) GET the URL with `HttpClient`; 2) return empty list on non‑200; 3) parse body with `Biweekly.parse(...).first()`; 4) iterate `VEvent`s, skip titles containing "attendance", map `DTSTART` → `Event.dueDate`; 5) swallow all exceptions and return whatever was collected. |
| `EmailService.java` | `sendPasswordResetEmail(to, link)` | Builds a `SimpleMailMessage` (from `noreply@gmail.com`, subject *Password Reset Request*) and sends via `JavaMailSender`. |
| `UserDetailsServiceImpl.java` | `loadUserByUsername(String)` | Looks up `AppUser`, throws `UsernameNotFoundException` if missing, otherwise returns a Spring `User` with role `USER`. Automatically picked up by Spring Security because it is the only `UserDetailsService` bean. |

#### Controllers (`web/`)

| File | Mapping | Handler | Behaviour |
|---|---|---|---|
| `UserController.java` | `GET /login` | `showLoginForm` | Renders `login.html`. |
| | `GET /register` | `showRegistrationForum` | Renders `register.html`. |
| | `POST /register` | `saveNewUser` | Validates password match / ≥ 8 chars / username free, encrypts Moodle URL, hashes password, saves, redirects to `/login`. Validation failures redirect back with `?error=...`. |
| | `GET /settings` | `showSettings` | Loads current user, decrypts the URL and exposes it to the view (model key `currnetUrl` – see *Known Issues*). |
| | `POST /settings` | `updateSettings` | Encrypts and stores `newMoodleUrl`, redirects to `/calendar`. |
| | `POST /settings/delete` | `deleteAccount` | Deletes any reset token, deletes the user, calls `request.logout()`, redirects to `/login?accountDeleted`. |
| `calendarController.java` | `GET /calendar` | `showChalendar` | Loads user → decrypts URL → `CalendarService.fetchEvents` → model attribute `events` → `calendar.html`. |
| `PasswordResetController.java` | `GET /forgot-password` | `showForgotPasswordForm` | Renders form. |
| | `POST /forgot-password` | `processForgotPassword` | If e‑mail exists: replace any old token, create UUID token valid 15 min, e‑mail a reset link. Always shows the same neutral message. |
| | `GET /reset-password?token=` | `showResetPasswordForm` | Validates token existence/expiry, renders form (with `error` if invalid). |
| | `POST /reset-password` | `processResetPassword` | Re‑validates token, checks password match / length, BCrypt‑hashes new password, deletes token, redirects to `/login?message=...`. |

#### Templates (`templates/`)

| Template | Rendered by | Notable details |
|---|---|---|
| `login.html` | `GET /login` | Standard Spring Security form (`username`, `password`, CSRF). |
| `register.html` | `GET /register` | Fields: `username`, `email`, `moodleUrl`, `password`, `confirmPassword`. |
| `calendar.html` | `GET /calendar` | Uses `th:inline="javascript"` to inject `${events}` as a JS array, maps it to FullCalendar event objects, renders `dayGridMonth` / `timeGridWeek` / `listWeek`. Contains Settings link and POST logout form. |
| `settings.html` | `GET /settings` | URL update form (`newMoodleUrl`, binds `${currentUrl}`) and a "Danger Zone" delete form with JS confirm. |
| `forgot-password.html` | `/forgot-password` | E‑mail input; displays `${message}`. |
| `reset-password.html` | `/reset-password` | `password` + `confirmPassword`; carries the token as a hidden field / query param; displays `${error}`. |

---

### Request Flows

#### Registration → first calendar view

```
POST /register ──► UserController.saveNewUser
                     ├─ validate passwords / username
                     ├─ EncryptionUtil.encrypt(moodleUrl)
                     ├─ PasswordEncoder.encode(password)
                     └─ AppUserRepo.save ──► redirect /login
POST /login    ──► Spring Security ──► UserDetailsServiceImpl.loadUserByUsername
                     └─ BCrypt match ──► redirect /calendar
GET  /calendar ──► calendarController.showChalendar
                     ├─ AppUserRepo.findByUsername(principal)
                     ├─ EncryptionUtil.decrypt(moodleUrl)
                     ├─ CalendarService.fetchEvents(url)  ──► HTTP GET Moodle ──► biweekly parse
                     └─ model "events" ──► calendar.html ──► FullCalendar
```

#### Password reset

```
POST /forgot-password ──► PasswordResetController.processForgotPassword
                            ├─ AppUserRepo.findByEmail
                            ├─ PasswordResetTokenRepo.findByUser / delete old
                            ├─ new UUID token, expiry = now + 15 min, save
                            └─ EmailService.sendPasswordResetEmail(link)
GET  /reset-password?token=… ──► showResetPasswordForm (validate) ──► reset-password.html
POST /reset-password          ──► processResetPassword
                            ├─ validate token + expiry + passwords
                            ├─ PasswordEncoder.encode ──► AppUserRepo.save
                            └─ PasswordResetTokenRepo.delete ──► redirect /login
```

---

### Data Model

```
┌──────────────────────┐          ┌───────────────────────────┐
│       app_user       │          │   password_reset_token    │
├──────────────────────┤          ├───────────────────────────┤
│ id          PK       │◄────┐    │ id           PK           │
│ username    UNIQUE   │     └────│ user_id      FK, NOT NULL │  (@OneToOne)
│ password_hash        │          │ token        UNIQUE       │
│ email       NOT NULL │          │ expiry_date  NOT NULL     │
│ moodle_url  (1000)   │          └───────────────────────────┘
└──────────────────────┘
```

- Tables are created/updated automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`); actual column names follow Spring's default `SNAKE_CASE` naming strategy.
- `Event` is **not** a table; it is built on the fly from the iCal feed on every request.

---

### Configuration

All settings live in `src/main/resources/application.properties`.

| Property | Purpose |
|---|---|
| `server.port` | HTTPS port (currently `7010`). |
| `server.ssl.*` | PKCS12 keystore location, password, type and alias (`springboot`). |
| `spring.datasource.*` | MySQL JDBC URL, username, password. |
| `spring.jpa.hibernate.ddl-auto` | Schema management (`update`). |
| `spring.jpa.show-sql`, `logging.level.org.hibernate.*` | SQL / bind‑parameter logging (verbose; intended for development). |
| `app.security.encryption.key` | Base64 AES key consumed by `EncryptionUtil`. |
| `spring.mail.*` | SMTP host/port/credentials and STARTTLS flags for `EmailService`. |

> **The committed `application.properties` contains real credentials (DB password, SMTP app password, keystore password, AES key).** Do not keep these values if you intend to deploy. It is also recommended to never share your own file with anyone else for this reason.

Recommended externalisation pattern (conceptual): keep placeholders such as `${DB_PASSWORD}` in the properties file and supply the real values through environment variables, a `.env` ignored by Git, or a Spring profile that is not committed.

---

### Getting Started

#### Prerequisites

- JDK 17 or newer
- MySQL 8.x running locally
- A Gmail (or other SMTP) account with an app‑specific password for outbound e‑mail
- A Moodle account to obtain your personal iCal export URL  
  (Moodle → Calendar → *Export calendar* → "All events", "Recent and next 60 days" → copy URL)

#### Before starting

- Go in to properties and update them to reflect current system

#### Database setup

Create the schema and user that `spring.datasource.url` expects (defaults: database `alexu`, user `alexu`), or change the properties to point at your own. Hibernate will create the tables on first start.

#### Run

```bash
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

Then open **https://localhost:7010/login**. Because the bundled certificate is self‑signed, your browser will show a trust warning the first time.

#### Build a runnable jar

```bash
./mvnw clean package
java -jar target/project-*.jar
```

---

### Endpoint Reference

| Method | Path | Auth | Controller |
|---|---|---|---|
| GET | `/login` | public | `UserController` |
| POST | `/login` | public | Spring Security (form login) |
| GET / POST | `/register` | public | `UserController` |
| GET / POST | `/forgot-password` | public | `PasswordResetController` |
| GET / POST | `/reset-password` | public | `PasswordResetController` |
| GET | `/calendar` | authenticated | `calendarController` |
| GET / POST | `/settings` | authenticated | `UserController` |
| POST | `/settings/delete` | authenticated | `UserController` |
| POST | `/logout` | authenticated | Spring Security |

All POST endpoints require the CSRF token that Thymeleaf injects via `${_csrf.parameterName}` / `${_csrf.token}`.
