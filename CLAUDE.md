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
  application/
    usecase/      # Use cases (POJO, instantiated by ApplicationService with `new`)
    dto/          # HTTP request/response DTOs
    service/      # ApplicationService — orchestrates use cases, exposed to controllers
  domain/
    model/        # Entities and value objects (pure Java, zero framework dependencies)
    repository/   # Port interfaces (persistence contracts defined by domain)
    events/       # Domain events
    exception/    # Domain exceptions
  infrastructure/
    persistence/
      entity/     # JPA entities (@Entity, @Table — never in domain/)
      jpa/        # Spring Data JPA interfaces
      adapter/    # Implementations of domain repository ports
      mapper/     # Domain ↔ JPA entity conversion
    listeners/    # Domain event handlers (@TransactionalEventListener)
  presentation/
    rest/         # REST controllers
    exception/    # @ControllerAdvice per module
```

### Modules

- **auth** — JWT-based authentication, Spring Security config, login/signup
- **user** — User profiles, roles, permissions, addresses
- **event** — Event lifecycle (DRAFT → PUBLISHED → REGISTRATION_OPEN → REGISTRATION_CLOSED → IN_PROGRESS → COMPLETED/CANCELLED)
- **registration** — Participant enrollment, ticket codes, bib number assignment, kit pickup tracking
- **image** — Image storage (Cloudinary via URL, multipart upload)
- **bootstrap** — Initializes default roles and admin user on startup
- **shared** — Base `UseCase<Command, Result>` class, domain ports, common value objects, i18n config

---

## ⚠️ Reglas de arquitectura — obligatorias en todo código nuevo

Estas reglas se aplican **siempre**, sin excepciones. Antes de escribir cualquier clase nueva, verifica que cumple todas.

### Regla 1 — Use cases van en `application/usecase/`, no en `domain/`

```
✅ CORRECTO:   <module>/application/usecase/CreateEventUseCase.java
❌ INCORRECTO: <module>/domain/usecase/CreateEventUseCase.java
```

Los use cases orquestan colaboradores (repositorios, servicios, domain events). Eso es responsabilidad de la capa de aplicación. El dominio solo contiene entidades, value objects, puertos y domain events.

**Si una clase no llama ningún repositorio ni publica ningún evento**, no es un use case — es un método en una entidad de dominio.

### Regla 2 — Use cases son POJOs, instanciados con `new` en el ApplicationService

```java
// ✅ CORRECTO — POJO instanciado por el service
public class CreateEventUseCase extends UseCase<CreateEventUseCase.Command, EventResponse> {
    // sin @Component, sin @Service
    public record Command(String name, UUID organizerId) {}

    public CreateEventUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }
}

// En el ApplicationService:
public EventResponse createEvent(CreateEventRequest request) {
    var command = new CreateEventUseCase.Command(request.name(), organizerId);
    return new CreateEventUseCase(eventRepository).execute(command);
}
```

```java
// ❌ INCORRECTO — bean de Spring en el use case
@Component
public class CreateEventUseCase extends UseCase<...> { ... }
```

El command es un **record interno del use case**, no una clase separada en `dto/`.

### Regla 3 — `domain/` tiene dependencias cero de Spring, JPA y `application/`

Si aparece cualquiera de estos imports dentro de `domain/`, el diseño está mal:

```java
// ❌ NUNCA en domain/:
import org.springframework.*;
import jakarta.persistence.*;
import com.accesosport.<module>.application.*;
import com.accesosport.<module>.infrastructure.*;
```

### Regla 4 — Para templates de email, inyectar `EmailTemplatePort`, no `EmailTemplateService`

`EmailTemplateService` es una implementación de infraestructura (Thymeleaf). Las capas superiores no deben importarla directamente.

```java
// ✅ CORRECTO — inyectar el puerto
import com.accesosport.shared.domain.port.EmailTemplatePort;

public class RequestPasswordResetUseCase extends UseCase<...> {
    private final EmailTemplatePort emailTemplatePort; // puerto, no implementación
}
```

```java
// ❌ INCORRECTO — importar la implementación desde capas superiores
import com.accesosport.shared.infrastructure.email.EmailTemplateService;
```

`EmailTemplatePort` está en `shared/domain/port/`. `EmailTemplateService` lo implementa en `shared/infrastructure/email/`.

### Regla 5 — Controllers solo delegan, no toman decisiones de negocio

Los controllers extraen datos del request (path variables, body, `@AuthenticationPrincipal`) y los pasan al `ApplicationService`. La lógica de negocio y la orquestación van en el service.

```java
// ✅ CORRECTO — controller pasa datos crudos, service decide
@PostMapping("/{eventId}/publish")
public ResponseEntity<Void> publishEvent(
        @PathVariable UUID eventId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    eventApplicationService.publishEvent(eventId, userDetails.getUserId(), userDetails.isAdmin());
    return ResponseEntity.ok().build();
}

// En el ApplicationService:
public void publishEvent(UUID eventId, UUID userId, boolean isAdmin) {
    UUID requesterId = isAdmin ? null : userId; // ← decisión de negocio: aquí, no en el controller
    new PublishEventUseCase(eventRepository).execute(new Command(eventId, requesterId));
}
```

```java
// ❌ INCORRECTO — lógica de negocio en el controller
UUID requesterId = isAdmin(userDetails) ? null : userDetails.getUserId(); // ← no va aquí
eventApplicationService.publishEvent(eventId, requesterId);
```

### Regla 6 — Entidades de dominio sin setters públicos

Los cambios de estado en entidades ocurren solo a través de métodos con semántica de negocio.

```java
// ✅ CORRECTO
public void changePassword(String newPasswordHash) {
    this.passwordHash = newPasswordHash;
}

public void markAsUsed() {
    this.usedAt = LocalDateTime.now();
}

// ❌ INCORRECTO — setter público
public void setPasswordHash(String hash) { this.passwordHash = hash; }
public void setUsedAt(LocalDateTime t) { this.usedAt = t; }
```

No usar `@Data` de Lombok en entidades de dominio — genera setters públicos automáticamente. Usar `@Getter` + constructor privado + factory methods estáticos (`create(...)`, `reconstitute(...)`).

---

## Deuda técnica conocida — no replicar estos patrones

Los siguientes módulos tienen código que viola las reglas anteriores. Están documentados como deuda técnica (tareas ARCH-03, ARCH-04, ARCH-05). **No replicar estos patrones en código nuevo aunque los veas en el código existente.**

| Deuda | Módulos afectados | Tarea |
|---|---|---|
| Use cases en `domain/usecase/` | `auth`, `event`, `user`, `image`, `bootstrap` | ARCH-05 |
| `EmailTemplateService` importado fuera de infrastructure | `shared`, `event`, `registration` | ARCH-03 |
| Lógica de negocio en controllers (`requesterId`, defaults) | `event`, `registration` | ARCH-04 |

---

### Módulo registration

El módulo de inscripciones es independiente del módulo de eventos: depende de `EventRepository` y `EventCapacityRepository` (puertos definidos en el módulo event), pero el módulo event no depende del módulo registration. Esto evita dependencias circulares.

**Entidad de dominio clave:** `Registration` — campos: `id`, `eventId`, `participantId`, `status` (`PENDING_PAYMENT | CONFIRMED | CANCELLED`), `ticketCode` (ej. `ACSP-4X7K`), `bibNumber` (asignado posteriormente), `paymentMethod`, `kitPickedUp`, `kitPickedUpAt`, `registeredAt`, `cancelledAt`.

La entidad solo expone comportamiento mediante métodos (`cancel()`, `assignBibNumber(int)`, `markKitPickedUp()`); los setters están prohibidos para preservar invariantes de dominio.

**Servicio de aplicación:** `RegistrationApplicationService` — orquesta todos los casos de uso del módulo. Los casos de uso se instancian directamente en el servicio con `new`. Este es el patrón correcto para todos los módulos.

### Scheduler

`EventLifecycleScheduler` ejecuta dos tareas periódicas:

| Tarea | Frecuencia | Configurable con |
|---|---|---|
| Transiciones de ciclo de vida del evento | cada 60 s (default) | `app.scheduler.event-lifecycle.fixed-delay-ms` |
| Envío de recordatorios por email | cada 60 min (default) | `app.scheduler.reminder.fixed-delay-ms` |

Las transiciones automáticas son: `autoOpenRegistrations`, `autoCloseRegistrations`, `autoBeginEvents`, `autoCompleteEvents`, y `cleanupExpiredPendingPayments`. Esto significa que **los eventos transicionan de estado sin intervención manual** una vez que el organizador los publica y configura fechas.

### Security

- JWT stateless auth (24h expiration)
- Public endpoints: `/auth/**`, `/api/v1/public/**`
  - `GET /api/v1/public/events` — eventos publicados (disponibles para registro)
  - `GET /api/v1/public/events/available` — eventos con inscripciones abiertas
  - `GET /api/v1/public/events/published` — eventos publicados
  - `GET /api/v1/public/events/{id}` — detalle de un evento
  - `GET /api/v1/public/events/{id}/modalities` — modalidades del evento
  - `GET /api/v1/public/events/{id}/categories` — categorías del evento
  - `GET /api/v1/public/events/{id}/images` — galería de imágenes del evento
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

---

## Decisiones de negocio ✅

### Pasarela de pagos — Stripe Connect
- Métodos aceptados: tarjeta de débito, tarjeta de crédito, OXXO
- Fee de Stripe: 3.6% + $3 MXN por transacción (IVA incluido)
- Modelo: AccesoSport cobra un **cargo por servicio** al participante: `Math.max(20, precio * 0.10)` — el mayor entre $20 MXN fijo o 10% del precio de inscripción; punto de quiebre en $200 MXN
- El organizador absorbe el fee de Stripe de su transferencia; AccesoSport retiene el cargo por servicio completo (sin deducciones)
- `OrganizerFeeCalculator`: `max($10, precio * 5%)` — comisión descontada de la transferencia al organizador; punto de quiebre en $200 MXN
- Split del pago vía Stripe Connect: participante paga (inscripción + cargo por servicio); `application_fee_amount = serviceFee + organizerFee`; Stripe transfiere `basePrice − organizerFee` al organizador; AccesoSport retiene el application_fee_amount menos el fee de Stripe (~3.6% + $3 MXN)
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
- Si hubo pago: AccesoSport emite el reembolso total vía Stripe API automáticamente (precio base + cargo por servicio)
- El organizador absorbe el fee de Stripe no reembolsable (~3.6% + $3 MXN)
- Los participantes reciben email de aviso con confirmación del reembolso

### Cancelación de inscripción por el participante
- **Con más de 15 días de anticipación al evento:** reembolso parcial — precio base + cargo por servicio menos una comisión del 8% por cancelación
- **Con menos de 15 días de anticipación al evento:** sin reembolso
- Con tarjeta: reembolso en 5-10 días hábiles (tiempo estándar de Stripe). Con OXXO: no hay reembolso automático — se gestiona manualmente por transferencia

### Fee de Stripe en reembolsos
- Stripe no devuelve su fee de procesamiento (~3.6% + $3 MXN) en reembolsos
- Cuando el organizador cancela el evento: el organizador absorbe ese costo — el participante recibe el monto total que pagó
- Cuando el participante cancela: aplica la política de reembolso parcial/nulo según anticipación

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

### Puertos de dominio en `shared`

`shared/domain/port/` contiene puertos que los módulos usan para comunicarse con infraestructura compartida:

- `EmailTemplatePort.java` — interfaz para construir HTML de emails; implementada por `shared/infrastructure/email/EmailTemplateService.java`

**Regla:** inyectar siempre el puerto, nunca la implementación:
```java
private final EmailTemplatePort emailTemplatePort;  // ✅
private final EmailTemplateService emailTemplateService;  // ❌
```

### Eventos de dominio existentes
- `event/domain/events/EventCancelledEvent.java` — `event.cancelled`; campos: `eventId`, `eventName`, `cancellationReason`, `affectedRegistrationIds`
- `registration/domain/events/RegistrationConfirmedEvent.java` — `registration.confirmed`; campos: `registrationId`, `eventId`, `participantId`, `ticketCode`, `bibNumber`
- `registration/domain/events/RegistrationCancelledEvent.java` — `registration.cancelled`; campos: `registrationId`, `eventId`, `participantId`, `daysUntilEvent`

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