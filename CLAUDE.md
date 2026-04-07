# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AccesoSport is a backend REST API for an athletic race event ticketing system built with Java 21 and Spring Boot 3.4.4. It manages race event registration, user profiles, and event workflows for both organizers and participants.

## Commands

```bash
# Start the database (required before running the app)
docker compose up -d

# Build
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AccesosportApplicationTests

# Skip tests during build
./mvnw clean install -DskipTests
```

The app runs on port **8080**. PgAdmin is available at **http://localhost:5050**.

Default bootstrap admin: `admin@accesosport.com` / `password123`

## Architecture

The project strictly follows **Hexagonal (Clean) Architecture** with these layers:

```
Presentation → Application → Domain ← Infrastructure
```

**Dependency Rule:** Domain has zero external dependencies. Infrastructure adapters implement domain-defined port interfaces.

Each module is structured as:
```
<module>/
  application/      # Use cases (Command → Result pattern)
  domain/           # Entities, value objects, repository interfaces (ports)
  infrastructure/   # JPA entities, repository implementations, external config
  presentation/     # REST controllers, DTOs
```

### Modules

- **auth** — JWT-based authentication, Spring Security config, login/signup
- **user** — User profiles, roles, permissions, addresses
- **event** — Event lifecycle (DRAFT → PUBLISHED → REGISTRATION_OPEN → IN_PROGRESS → COMPLETED/CANCELLED)
- **image** — Image storage
- **bootstrap** — Initializes default roles and admin user on startup
- **shared** — Base `UseCase<Command, Result>` class, common value objects (Address, Distance, Location), i18n config

### Use Cases

All use cases extend `UseCase<C extends Command, R>` from the shared module and implement a single `execute(command)` method. Commands are immutable data carriers.

### Security

- JWT stateless auth (24h expiration)
- Public endpoints: `/auth/**`, `/api/v1/public/**`
- All other endpoints require a valid JWT Bearer token
- CORS enabled for `localhost` origins

### i18n

All user-facing messages are externalized to `src/main/resources/i18n/messages_en.properties` and `messages_es.properties`. Use `MessageSource` injection to look up keys — never hardcode message strings.

### Database

PostgreSQL 15.3 via Docker. `spring.jpa.hibernate.ddl-auto=update` — schema is auto-updated on startup. No migration tool (Flyway/Liquibase) is in use.

API versioning prefix: `/api/v1/`

## Decisiones de negocio pendientes (NO implementar sin confirmar)

- ¿Solo organizadores VERIFIED pueden publicar eventos? — Sin definir
- ¿Los eventos gratuitos (precio = 0) omiten el flujo de pagos? — Sin definir
- ¿Qué pasa con inscripciones al cancelar un evento? — Sin definir
- Pasarela de pagos: Stripe vs Conekta — Sin definir
- Proveedor de email: Resend vs SendGrid — Sin definir
