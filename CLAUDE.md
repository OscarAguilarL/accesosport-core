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
- **event** — Event lifecycle (DRAFT → PUBLISHED → REGISTRATION_OPEN → REGISTRATION_CLOSED → IN_PROGRESS → COMPLETED/CANCELLED)
- **registration** — Participant enrollment, ticket codes, bib number assignment, kit pickup tracking
- **image** — Image storage (Cloudinary via URL, multipart upload)
- **bootstrap** — Initializes default roles and admin user on startup
- **shared** — Base `UseCase<Command, Result>` class, common value objects (Address, Distance, Location), i18n config

### Módulo registration

El módulo de inscripciones es independiente del módulo de eventos: depende de `EventRepository` y `EventCapacityRepository` (puertos definidos en el módulo event), pero el módulo event no depende del módulo registration. Esto evita dependencias circulares.

**Entidad de dominio clave:** `Registration` — campos: `id`, `eventId`, `participantId`, `status` (`PENDING_PAYMENT | CONFIRMED | CANCELLED`), `ticketCode` (ej. `ACSP-4X7K`), `bibNumber` (asignado posteriormente), `paymentMethod`, `kitPickedUp`, `kitPickedUpAt`, `registeredAt`, `cancelledAt`.

La entidad solo expone comportamiento mediante métodos (`cancel()`, `assignBibNumber(int)`, `markKitPickedUp()`); los setters están prohibidos para preservar invariantes de dominio.

**Servicio de aplicación:** `RegistrationApplicationService` — orquesta todos los casos de uso del módulo. Los casos de uso se instancian directamente en el servicio (no son beans de Spring): esto es una decisión deliberada para mantener los use cases como clases POJO puras sin dependencia del framework.

### Scheduler

`EventLifecycleScheduler` ejecuta dos tareas periódicas:

| Tarea | Frecuencia | Configurable con |
|---|---|---|
| Transiciones de ciclo de vida del evento | cada 60 s (default) | `app.scheduler.event-lifecycle.fixed-delay-ms` |
| Envío de recordatorios por email | cada 60 min (default) | `app.scheduler.reminder.fixed-delay-ms` |

Las transiciones automáticas son: `autoOpenRegistrations`, `autoCloseRegistrations`, `autoBeginEvents`, `autoCompleteEvents`, y `cleanupExpiredPendingPayments`. Esto significa que **los eventos transicionan de estado sin intervención manual** una vez que el organizador los publica y configura fechas.

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

PostgreSQL 15.3 via Docker. El esquema se gestiona con **Flyway** — `ddl-auto=validate` (Hibernate solo verifica, no modifica).

Scripts en `src/main/resources/db/migration/`:
- `V1__init.sql` — estado inicial del esquema
- `V2__cleanup_orphan_schema.sql` — elimina columnas y tablas huérfanas del diseño anterior
- Scripts futuros: `V3__descripcion.sql`, `V4__descripcion.sql`, etc.

**Reglas:**
- Nunca modifiques un script `V` ya commiteado — Flyway guarda su checksum y fallará al detectar cambios
- Un script = un cambio lógico; no acumules varios cambios en un solo archivo
- Para entornos con BD existente: `baseline-on-migrate=true` marca el estado actual como versión 1 y aplica desde `V2` en adelante

API versioning prefix: `/api/v1/`

## Decisiones de negocio ✅

### Pasarela de pagos — Stripe Connect
- Métodos aceptados: tarjeta de débito, tarjeta de crédito, OXXO
- Fee de Stripe: 3.6% + $3 MXN por transacción (IVA incluido)
- Modelo: AccesoSport cobra un **cargo por servicio** al participante: `Math.max(20, precio * 0.08)` — el mayor entre $20 MXN fijo o 8% del precio de inscripción; punto de quiebre en $250 MXN
- El organizador recibe el **100% del precio base** de inscripción sin descuentos ni deducciones
- AccesoSport absorbe el fee de Stripe de su propio cargo por servicio
- Split del pago vía Stripe Connect: participante paga (inscripción + cargo por servicio) en un solo checkout; Stripe transfiere el precio base al organizador; AccesoSport retiene el cargo por servicio (neto del fee de Stripe)
- Retención hasta el evento: aplazada a fase 2 (cuando AccesoSport se constituya como empresa)
- Operación legal: como PFAE — Stripe maneja la regulación financiera

### Proveedor de email — Resend
- API moderna, integración simple, acepta adjuntos vía base64
- No requiere dependencia externa en pom.xml — usar RestClient de Spring Boot 3.4+

### Eventos gratuitos (precio = 0)
- Omiten completamente el flujo de pagos
- La inscripción se confirma directamente al registrarse, sin crear preferencia de pago en MP
- `RegistrationStatus` pasa directamente a `CONFIRMED`

### Cancelación de evento con inscritos
- Al cancelar un evento: todas las inscripciones `CONFIRMED` pasan a `CANCELLED`
- Si hubo pago: AccesoSport emite el reembolso vía Stripe API automáticamente (precio base + cargo por servicio)
- AccesoSport devuelve el cargo por servicio completo al reembolsar
- Los participantes reciben email de aviso con confirmación del reembolso

### Cancelación de inscripción por el participante
- Reembolso total siempre, sin importar la anticipación (precio base + cargo por servicio)
- AccesoSport absorbe el fee de Stripe (no se lo cobra al participante)
- Con tarjeta: reembolso en 5-10 días hábiles (tiempo estándar de Stripe). Con OXXO: no hay reembolso automático — se gestiona manualmente por transferencia

### Fee de Stripe en reembolsos
- Stripe no devuelve su fee de procesamiento (~3.6% + $3 MXN) en reembolsos
- AccesoSport absorbe ese costo — el participante recibe el monto total que pagó
- Se informa claramente en el checkout al momento de inscribirse

### Verificación de organizadores para eventos de pago
- Organizadores no verificados pueden crear y publicar eventos **gratuitos** libremente
- Para eventos con precio > 0: requieren `verificationStatus = VERIFIED` Y cuenta de Stripe Connect vinculada
- La verificación es manual por un admin de AccesoSport
- La vinculación de cuenta Stripe Connect se solicita durante el **onboarding inicial** del organizador (siempre, aunque no tenga eventos de pago todavía)

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
