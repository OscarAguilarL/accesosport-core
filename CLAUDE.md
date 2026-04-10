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

## Decisiones de negocio ✅

### Pasarela de pagos — Mercado Pago Marketplace
- Métodos aceptados: tarjeta (débito/crédito), OXXO, SPEI
- Modelo: AccesoSport cobra 3% de `marketplace_fee` por inscripción automáticamente
- Organizador recibe el 97% restante en T+2 días hábiles
- El dinero del participante va directo al organizador — no pasa por el RFC de AccesoSport
- Retención hasta el evento: aplazada a fase 2 (cuando AccesoSport se constituya como empresa)
- Operación legal: como PFAE — Mercado Pago maneja la regulación financiera

### Proveedor de email — Resend
- API moderna, integración simple, acepta adjuntos vía base64
- No requiere dependencia externa en pom.xml — usar RestClient de Spring Boot 3.4+

### Eventos gratuitos (precio = 0)
- Omiten completamente el flujo de pagos
- La inscripción se confirma directamente al registrarse, sin crear preferencia de pago en MP
- `RegistrationStatus` pasa directamente a `CONFIRMED`

### Cancelación de evento con inscritos
- Al cancelar un evento: todas las inscripciones `CONFIRMED` pasan a `CANCELLED`
- Si hubo pago: AccesoSport emite el reembolso vía Mercado Pago API automáticamente
- AccesoSport devuelve también su 3% de comisión al reembolsar
- Los participantes reciben email de aviso con confirmación del reembolso

### Cancelación de inscripción por el participante
- Reembolso total siempre, sin importar la anticipación
- AccesoSport absorbe el fee de MP (no se lo cobra al participante)
- Con tarjeta: reembolso en 1-2 días. Con OXXO/SPEI: 3-5 días hábiles (limitación de MP)

### Fee de Mercado Pago en reembolsos
- MP cobra ~3.6% + $4 MXN por transacción procesada — ese fee no se devuelve en reembolsos
- El participante recibe el monto pagado menos el fee de MP
- Se informa claramente en el checkout al momento de inscribirse

### Verificación de organizadores para eventos de pago
- Organizadores no verificados pueden crear y publicar eventos **gratuitos** libremente
- Para eventos con precio > 0: requieren `verificationStatus = VERIFIED` Y cuenta de MP vinculada
- La verificación es manual por un admin de AccesoSport
- La vinculación de cuenta MP se solicita durante el **onboarding inicial** del organizador (siempre, aunque no tenga eventos de pago todavía)

### Protección al participante
- Derecho a contracargo con su banco/tarjeta
- Solo organizadores VERIFIED pueden crear eventos de pago

---

## Domain Events

El proyecto usa **Spring `ApplicationEventPublisher`** con **`@TransactionalEventListener(AFTER_COMMIT)`** para desacoplar módulos de forma asíncrona. La estructura es idéntica a un consumer Kafka — la migración futura es solo cambiar la anotación.

### Clases base (shared)
- `shared/domain/events/DomainEvent.java` — clase base abstracta; contiene `eventId (UUID)`, `occurredAt (Instant)`, `eventType (String)`
- `shared/domain/events/DomainEventPublisher.java` — puerto (interfaz); los casos de uso inyectan este, no la implementación
- `shared/infrastructure/events/SpringDomainEventPublisher.java` — implementación que delega a `ApplicationEventPublisher`
- `shared/infrastructure/async/AsyncConfig.java` — `@EnableAsync` + `ThreadPoolTaskExecutor` bean llamado `domainEventExecutor` (4 core, 20 max, 500 queue, `CallerRunsPolicy`)

### Eventos de dominio existentes
- `event/domain/events/EventCancelledEvent.java` — `event.cancelled`; campos: `eventId`, `eventName`, `cancellationReason`, `affectedRegistrationIds`
- `registration/domain/events/RegistrationConfirmedEvent.java` — `registration.confirmed`; campos: `registrationId`, `eventId`, `participantId`, `ticketCode`, `bibNumber`
- `registration/domain/events/RegistrationCancelledEvent.java` — `registration.cancelled`; campos: `registrationId`, `eventId`, `participantId`

### Patrón de listener (seguir siempre este patrón exacto)
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NombreEventHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("domainEventExecutor")
    public void handle(TipoDeDomainEvent event) {
        try {
            // lógica del handler
        } catch (Exception e) {
            log.error("Failed to handle {} for {}", event.getEventType(), event.getEventId(), e);
            // NO relanzar — la transacción principal ya committed
        }
    }
}
```

`AFTER_COMMIT` — el listener solo se ejecuta si la transacción principal confirmó en BD.  
`@Async("domainEventExecutor")` — no bloquea el hilo HTTP que respondió al cliente.  
`CallerRunsPolicy` — si la cola está llena, el hilo llamador ejecuta la tarea; ningún evento se pierde sin Kafka.

### Nota de migración a Kafka (fase 2)
1. Agregar `spring-kafka` al `pom.xml`
2. Cambiar `@TransactionalEventListener` por `@KafkaListener(topics = "...")` en cada handler
3. Cambiar `SpringDomainEventPublisher` para publicar a un topic Kafka
4. Agregar **Outbox Pattern** (tabla `outbox_events` + CDC con Debezium) para garantía at-least-once
5. El código de los handlers y los eventos de dominio **no cambia**
