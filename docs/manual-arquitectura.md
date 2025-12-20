# 📘 Manual de Arquitectura Clean - Proyecto AccesoSport

## Tabla de Contenidos
1. [Introducción](#introducción)
2. [Principios Fundamentales](#principios-fundamentales)
3. [Estructura del Proyecto](#estructura-del-proyecto)
4. [Guía por Capas](#guía-por-capas)
5. [Flujo de Trabajo](#flujo-de-trabajo)
6. [Ejemplos Prácticos](#ejemplos-prácticos)
7. [Checklist de Implementación](#checklist-de-implementación)
8. [Errores Comunes](#errores-comunes)
9. [Buenas Prácticas](#buenas-prácticas)

---

## Introducción

Este proyecto sigue **Clean Architecture** (Arquitectura Hexagonal), que separa el código en capas concéntricas donde las dependencias siempre apuntan hacia adentro.

### ¿Por qué Clean Architecture?

✅ **Independencia de frameworks**: La lógica de negocio no depende de Spring, JPA, etc.  
✅ **Testabilidad**: Fácil crear tests unitarios sin levantar el contexto de Spring  
✅ **Mantenibilidad**: Cambios en una capa no afectan a otras  
✅ **Flexibilidad**: Cambiar tecnologías sin reescribir la lógica de negocio  

---

## Principios Fundamentales

### 1. Regla de Dependencia
```
Presentation → Application → Domain ← Infrastructure
                    ↓           ↓            ↓
                         Shared
```

- Las capas externas **SÍ** pueden depender de las internas
- Las capas internas **NO** pueden depender de las externas
- El **Domain** es el centro y no conoce a nadie
- El módulo **Shared** contiene elementos compartidos por todos los módulos
- Todos los módulos pueden depender de `shared`, pero `shared` no depende de ningún módulo específico

### 2. Inversión de Dependencias

El dominio define **Ports** (interfaces), la infraestructura implementa **Adapters**.

```java
// ✅ CORRECTO
// Domain define el puerto
public interface UserRepository {  }

// Infrastructure implementa el adaptador
@Repository
public class UserRepositoryAdapter implements UserRepository {  }
```

```java
// ❌ INCORRECTO
// Domain depende de Spring Data
public interface UserRepository extends JpaRepository<User, UUID> {  }
```

### 2.1 Use Cases Abstractos

Todos los casos de uso extienden de la clase base `UseCase<Command, Result>` que define el contrato de ejecución:

```java
// Clase base para todos los casos de uso
public abstract class UseCase<Command, Result> {
    public Result execute(Command command) {
        return internalExecute(command);
    }

    public Result execute() {
        return internalExecute(null);
    }

    protected abstract Result internalExecute(Command command);
}

// Implementación de un caso de uso concreto
@RequiredArgsConstructor
public class SaveUserAddressUseCase extends UseCase<
        SaveUserAddressUseCase.Command,
        SaveUserAddressUseCase.Result> {

    private final UserRepository userRepository;

    @Override
    protected Result internalExecute(Command command) {
        // Lógica del caso de uso
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(...));

        Address address = new Address(...);
        user.setAddress(address);
        userRepository.save(user);

        return new Result(address);
    }

    public record Command(UUID userId, String street, ...) {}
    public record Result(Address address) {}
}
```

### 3. Separación de Entidades

**Entidades de Dominio** ≠ **Entidades JPA** ≠ **Objetos Embebidos**

```java
// Domain Model (sin anotaciones, con lógica de negocio)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalData {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    // Lógica de negocio
}

// Infrastructure Embeddable (con anotaciones JPA)
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalDataEmbeddable {
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private LocalDate birthDate;
    @Column
    private String gender;
}
```

---

## Estructura del Proyecto

```
src/main/java/com/grupocaos/products/athletix/
│
├── 📁 [feature]/                    # Por cada feature/módulo (user, event, auth, etc.)
│   │
│   ├── 📁 domain/                   # ⭐ CAPA DE DOMINIO
│   │   ├── 📁 model/                # Entidades y modelos de dominio (sin anotaciones)
│   │   ├── 📁 repository/           # Ports (interfaces) de repositorios
│   │   ├── 📁 service/              # Ports de servicios externos
│   │   ├── 📁 usecase/              # Casos de uso (extienden UseCase<Command,Result>)
│   │   └── 📁 exception/            # Excepciones de dominio
│   │
│   ├── 📁 application/              # 🔷 CAPA DE APLICACIÓN
│   │   ├── 📁 dto/                  # DTOs (Request/Response) con validaciones
│   │   └── 📁 service/              # Servicios de aplicación (orquestadores)
│   │
│   ├── 📁 infrastructure/           # 🔧 CAPA DE INFRAESTRUCTURA
│   │   ├── 📁 persistence/
│   │   │   ├── 📁 entity/           # Entidades JPA y Embeddables
│   │   │   ├── 📁 jpa/              # Spring Data Repositories
│   │   │   ├── 📁 adapter/          # Adapters de repositorio
│   │   │   └── 📁 mapper/           # Mappers Domain ↔ JPA/Embeddable
│   │   ├── 📁 config/               # Configuraciones técnicas
│   │   └── 📁 [other-adapters]/     # Otros adaptadores (email, payment, etc.)
│   │
│   └── 📁 presentation/             # 🌐 CAPA DE PRESENTACIÓN
│       └── 📁 rest/                 # Controllers REST
│           └── 📁 exception/        # Exception handlers
│
└── 📁 shared/                       # 💎 CÓDIGO COMPARTIDO
    ├── 📁 domain/                   # Dominio compartido
    │   ├── 📁 usecase/              # UseCase<Command,Result> base
    │   ├── 📁 valueobjects/         # Value Objects (Address, Money, etc.)
    │   └── 📁 i18n/                 # Interfaces de internacionalización
    ├── 📁 application/              # DTOs compartidos
    │   └── 📁 dto/
    └── 📁 infrastructure/           # Infraestructura compartida
        ├── 📁 common/               # Utilidades comunes
        └── 📁 i18n/                 # Implementación de i18n
```

---

## Guía por Capas

### 🎯 DOMAIN LAYER (Capa de Dominio)

**¿Qué va aquí?**
- Lógica de negocio PURA
- Entidades sin anotaciones de frameworks
- Interfaces (Ports)
- Casos de uso
- Excepciones de dominio

**Reglas:**
- ❌ NO importar nada de Spring, JPA, Jackson, etc.
- ❌ NO usar `@Entity`, `@Service`, `@Repository`, `@Component`
- ✅ Solo Java puro y librerías estándar
- ✅ Todo debe ser testeable sin Spring

**Ejemplo de Entidad:**
```java
package com.grupocaos.products.athletix.evento.domain.model;

// ✅ Sin anotaciones de frameworks
public class Evento {
    private UUID id;
    private String nombre;
    private LocalDateTime fecha;
    private EstadoEvento estado;
    
    // Constructor privado
    private Evento() {}
    
    // Factory method
    public static Evento crear(String nombre, LocalDateTime fecha) {
        Evento evento = new Evento();
        evento.id = UUID.randomUUID();
        evento.nombre = nombre;
        evento.fecha = fecha;
        evento.estado = EstadoEvento.BORRADOR;
        return evento;
    }
    
    // Lógica de negocio
    public void publicar() {
        if (this.estado != EstadoEvento.BORRADOR) {
            throw new IllegalStateException("Solo se pueden publicar eventos en borrador");
        }
        if (this.fecha.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede publicar un evento en el pasado");
        }
        this.estado = EstadoEvento.PUBLICADO;
    }
    
    public boolean puedeInscribirse() {
        return this.estado == EstadoEvento.PUBLICADO 
            && this.fecha.isAfter(LocalDateTime.now());
    }
    
    // Getters (sin setters públicos)
}
```

**Ejemplo de Repository Port:**
```java
package com.grupocaos.products.athletix.evento.domain.repository;

// ✅ Interfaz pura, sin extends JpaRepository
public interface EventoRepository {
    Optional<Evento> findById(UUID id);
    List<Evento> findByEstado(EstadoEvento estado);
    Evento save(Evento evento);
    void delete(UUID id);
}
```

**Ejemplo de Use Case:**
```java
package com.grupocaos.products.athletix.evento.domain.usecase;

import com.grupocaos.products.athletix.shared.domain.usecase.UseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublicarEventoUseCase extends UseCase<
        PublicarEventoUseCase.Command,
        PublicarEventoUseCase.Result> {

    private final EventoRepository eventoRepository;
    private final NotificationService notificationService;

    @Override
    protected Result internalExecute(Command command) {
        // 1. Obtener evento
        Evento evento = eventoRepository.findById(command.eventoId())
            .orElseThrow(() -> new EventoNotFoundException(command.eventoId()));

        // 2. Aplicar lógica de negocio
        evento.publicar();

        // 3. Persistir
        Evento eventoPublicado = eventoRepository.save(evento);

        // 4. Notificar (efecto secundario)
        notificationService.notifyEventoPublicado(eventoPublicado);

        return new Result(eventoPublicado);
    }

    public record Command(UUID eventoId) {}
    public record Result(Evento evento) {}
}
```

---

### 🔷 APPLICATION LAYER (Capa de Aplicación)

**¿Qué va aquí?**
- DTOs (Request/Response)
- Servicios de aplicación (orquestadores)
- Mappers de DTO ↔ Domain
- Validaciones de entrada

**Reglas:**
- ✅ Puede usar anotaciones de validación (`@NotNull`, `@Valid`)
- ✅ Orquesta casos de uso
- ✅ Traduce DTOs a objetos de dominio
- ❌ NO contiene lógica de negocio
- ❌ NO accede directamente a repositorios

**Ejemplo de DTO:**
```java
package com.grupocaos.products.athletix.evento.application.dto;

import jakarta.validation.constraints.*;

// ✅ Usar records para DTOs inmutables
public record CrearEventoRequest(
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 5, max = 100, message = "El nombre debe tener entre 5 y 100 caracteres")
    String nombre,
    
    @NotBlank(message = "La descripción es requerida")
    @Size(max = 1000)
    String descripcion,
    
    @NotNull(message = "La fecha es requerida")
    @Future(message = "La fecha debe ser futura")
    LocalDateTime fecha,
    
    @NotNull(message = "La ubicación es requerida")
    UbicacionDto ubicacion,
    
    @Positive(message = "La distancia debe ser positiva")
    double distanciaKm
) {}

public record EventoResponse(
    UUID id,
    String nombre,
    String descripcion,
    LocalDateTime fecha,
    String estado,
    UbicacionDto ubicacion,
    double distanciaKm
) {}
```

**Ejemplo de Application Service:**
```java
package com.grupocaos.products.athletix.evento.application.service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoApplicationService {
    
    private final EventoRepository eventoRepository;
    private final OrganizadorRepository organizadorRepository;
    private final NotificationService notificationService;
    
    @Transactional
    public EventoResponse crearEvento(CrearEventoRequest request, UUID organizadorId) {
        log.info("Creating event: {} for organizer: {}", request.nombre(), organizadorId);

        // 1. Crear comando para el caso de uso
        CrearEventoUseCase.Command command =
            new CrearEventoUseCase.Command(
                request.nombre(),
                request.descripcion(),
                request.fecha(),
                request.ubicacion(),
                request.distanciaKm(),
                organizadorId
            );

        // 2. Ejecutar caso de uso
        CrearEventoUseCase useCase = new CrearEventoUseCase(
            eventoRepository,
            organizadorRepository,
            notificationService
        );

        CrearEventoUseCase.Result result = useCase.execute(command);

        // 3. Mapear a DTO de respuesta
        return EventoResponseMapper.fromDomain(result.evento());
    }

    @Transactional
    public EventoResponse publicarEvento(UUID eventoId) {
        log.info("Publishing event: {}", eventoId);

        PublicarEventoUseCase useCase = new PublicarEventoUseCase(
            eventoRepository,
            notificationService
        );

        PublicarEventoUseCase.Command command = new PublicarEventoUseCase.Command(eventoId);
        PublicarEventoUseCase.Result result = useCase.execute(command);

        return EventoResponseMapper.fromDomain(result.evento());
    }
}
```

---

### 🔧 INFRASTRUCTURE LAYER (Capa de Infraestructura)

**¿Qué va aquí?**
- Entidades JPA (con anotaciones)
- Repositorios Spring Data
- Adapters que implementan Ports
- Mappers Domain ↔ JPA
- Configuraciones técnicas
- Implementaciones de servicios externos

**Reglas:**
- ✅ Puede usar cualquier framework (Spring, JPA, etc.)
- ✅ Implementa las interfaces del dominio
- ✅ Traduce entre dominio e infraestructura
- ❌ NO contiene lógica de negocio

**Ejemplo de JPA Entity:**
```java
package com.grupocaos.products.athletix.evento.infrastructure.persistence.entity;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(length = 1000)
    private String descripcion;
    
    @Column(nullable = false)
    private LocalDateTime fecha;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEvento estado;
    
    @Embedded
    private UbicacionEmbeddable ubicacion;
    
    @Column(nullable = false)
    private double distanciaKm;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id")
    private OrganizadorJpaEntity organizador;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Ejemplo de Embeddable:**
```java
package com.grupocaos.products.athletix.user.infrastructure.persistence.entity;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalDataEmbeddable {

    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String secondLastName;
    @Column
    private LocalDate birthDate;
    @Column
    private String gender;
    @Column
    private String phoneNumber;
}
```

**Ejemplo de Mapper Domain ↔ Embeddable:**
```java
package com.grupocaos.products.athletix.user.infrastructure.persistence.mapper;

/**
 * Mapper para convertir entre PersonalData (dominio) y PersonalDataEmbeddable (infraestructura)
 */
public class PersonalDataMapper {

    /**
     * Convierte de entidad embebible a modelo de dominio
     */
    public static PersonalData toDomain(PersonalDataEmbeddable entity) {
        if (entity == null) return null;

        return PersonalData.builder()
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .secondLastName(entity.getSecondLastName())
            .birthDate(entity.getBirthDate())
            .gender(entity.getGender())
            .phoneNumber(entity.getPhoneNumber())
            .build();
    }

    /**
     * Convierte de modelo de dominio a entidad embebible
     */
    public static PersonalDataEmbeddable toEntity(PersonalData domain) {
        if (domain == null) return null;

        return PersonalDataEmbeddable.builder()
            .firstName(domain.getFirstName())
            .lastName(domain.getLastName())
            .secondLastName(domain.getSecondLastName())
            .birthDate(domain.getBirthDate())
            .gender(domain.getGender())
            .phoneNumber(domain.getPhoneNumber())
            .build();
    }
}
```

**Ventajas de usar Embeddables:**
- ✅ Evita crear múltiples tablas para datos cohesivos (nombre, apellidos, etc.)
- ✅ Mantiene la separación entre dominio e infraestructura
- ✅ Facilita el mapeo con Builders de Lombok
- ✅ Reutilizable en diferentes entidades JPA

**Ejemplo de Repository Adapter:**
```java
package com.grupocaos.products.athletix.evento.infrastructure.persistence.adapter;

@Repository
@RequiredArgsConstructor
public class EventoRepositoryAdapter implements EventoRepository {
    
    private final EventoJpaRepository jpaRepository;
    
    @Override
    public Optional<Evento> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(EventoMapper::toDomain);
    }
    
    @Override
    public List<Evento> findByEstado(EstadoEvento estado) {
        return jpaRepository.findByEstado(estado).stream()
            .map(EventoMapper::toDomain)
            .toList();
    }
    
    @Override
    public Evento save(Evento evento) {
        EventoJpaEntity entity = EventoMapper.toEntity(evento);
        EventoJpaEntity savedEntity = jpaRepository.save(entity);
        return EventoMapper.toDomain(savedEntity);
    }
    
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
```

---

### 🌐 PRESENTATION LAYER (Capa de Presentación)

**¿Qué va aquí?**
- Controllers REST
- Exception handlers globales
- Configuraciones de seguridad de endpoints
- Validaciones de entrada HTTP

**Reglas:**
- ✅ Solo maneja HTTP (request/response)
- ✅ Delega a Application Services
- ✅ Maneja autenticación/autorización
- ❌ NO contiene lógica de negocio
- ❌ NO accede directamente a repositorios

**Ejemplo de Controller:**
```java
package com.grupocaos.products.athletix.evento.presentation.rest;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Slf4j
public class EventoController {
    
    private final EventoApplicationService eventoApplicationService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<EventoResponse> crearEvento(
            @Valid @RequestBody CrearEventoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("POST /api/v1/eventos - User: {}", userDetails.getUsername());
        
        UUID organizadorId = extractOrganizadorId(userDetails);
        EventoResponse response = eventoApplicationService.crearEvento(request, organizadorId);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
    
    @PutMapping("/{id}/publicar")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<EventoResponse> publicarEvento(@PathVariable UUID id) {
        log.info("PUT /api/v1/eventos/{}/publicar", id);
        
        EventoResponse response = eventoApplicationService.publicarEvento(id);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> obtenerEvento(@PathVariable UUID id) {
        log.info("GET /api/v1/eventos/{}", id);
        
        EventoResponse response = eventoApplicationService.obtenerEvento(id);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<EventoResponse>> listarEventos(
            @RequestParam(required = false) EstadoEvento estado) {
        
        log.info("GET /api/v1/eventos?estado={}", estado);
        
        List<EventoResponse> responses = eventoApplicationService.listarEventos(estado);
        
        return ResponseEntity.ok(responses);
    }
    
    private UUID extractOrganizadorId(UserDetails userDetails) {
        // Extraer ID del organizador desde el token/principal
        return UUID.fromString(userDetails.getUsername());
    }
}
```

---

## Flujo de Trabajo

### Agregando una Nueva Feature: "Inscripción a Eventos"

#### Paso 1: Definir el Dominio

```java
// 1.1 Entidad de Dominio
package com.grupocaos.products.athletix.inscripcion.domain.model;

import com.grupocaos.products.athletix.shared.domain.usecase.UseCase;

public class Inscripcion {
    private UUID id;
    private Evento evento;
    private Corredor corredor;
    private LocalDateTime fechaInscripcion;
    private EstadoInscripcion estado;

    public void confirmar() {
        if (this.estado != EstadoInscripcion.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden confirmar inscripciones pendientes");
        }
        this.estado = EstadoInscripcion.CONFIRMADA;
    }

    public void cancelar() {
        if (this.estado == EstadoInscripcion.CANCELADA) {
            throw new IllegalStateException("La inscripción ya está cancelada");
        }
        this.estado = EstadoInscripcion.CANCELADA;
    }
}

// 1.2 Repository Port
package com.grupocaos.products.athletix.inscripcion.domain.repository;

public interface InscripcionRepository {
    Optional<Inscripcion> findById(UUID id);

    List<Inscripcion> findByCorredorId(UUID corredorId);

    List<Inscripcion> findByEventoId(UUID eventoId);

    boolean existsByEventoIdAndCorredorId(UUID eventoId, UUID corredorId);

    Inscripcion save(Inscripcion inscripcion);
}

// 1.3 Use Case
package com.grupocaos.products.athletix.inscripcion.domain.usecase;

public class InscribirCorredorUseCase extends UseCase<
            InscribirCorredorUseCase.InscripcionCommand,
            InscribirCorredorUseCase.InscripcionResult
        > {

    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final CorredorRepository corredorRepository;

    public InscripcionResult execute() {
        // Validaciones
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException(eventoId));

        if (!evento.puedeInscribirse()) {
            throw new InscripcionNoPermitidaException("El evento no está disponible para inscripción");
        }

        if (inscripcionRepository.existsByEventoIdAndCorredorId(eventoId, corredorId)) {
            throw new InscripcionDuplicadaException("El corredor ya está inscrito en este evento");
        }

        Corredor corredor = corredorRepository.findById(corredorId)
                .orElseThrow(() -> new CorredorNotFoundException(corredorId));

        // Crear inscripción
        Inscripcion inscripcion = Inscripcion.crear(evento, corredor);

        // Persistir
        Inscripcion inscripcionGuardada = inscripcionRepository.save(inscripcion);

        return new InscripcionResult(inscripcionGuardada);
    }

    public record InscripcionCommand(UUID eventoId, UUID corredorId) {
    }

    public record InscripcionResult(Inscripcion inscripcion) {
    }
}
```

#### Paso 2: Application Layer

```java
// 2.1 DTOs
package com.grupocaos.products.athletix.inscripcion.application.dto;

public record InscribirseEventoRequest(
    @NotNull UUID eventoId
) {}

public record InscripcionResponse(
    UUID id,
    UUID eventoId,
    String nombreEvento,
    UUID corredorId,
    LocalDateTime fechaInscripcion,
    String estado
) {}

// 2.2 Application Service
package com.grupocaos.products.athletix.inscripcion.application.service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InscripcionApplicationService {
    
    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final CorredorRepository corredorRepository;
    
    @Transactional
    public InscripcionResponse inscribirCorredor(UUID corredorId, InscribirseEventoRequest request) {
        InscribirCorredorUseCase useCase = new InscribirCorredorUseCase(
            inscripcionRepository,
            eventoRepository,
            corredorRepository
        );
        
        InscribirCorredorUseCase.InscripcionResult result = 
            useCase.execute(request.eventoId(), corredorId);
        
        return InscripcionResponseMapper.fromDomain(result.inscripcion());
    }
}
```

#### Paso 3: Infrastructure Layer

```java
// 3.1 JPA Entity
@Entity
@Table(name = "inscripciones")
public class InscripcionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    private EventoJpaEntity evento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corredor_id")
    private CorredorJpaEntity corredor;
    
    private LocalDateTime fechaInscripcion;
    
    @Enumerated(EnumType.STRING)
    private EstadoInscripcion estado;
}

// 3.2 Spring Data Repository
public interface InscripcionJpaRepository extends JpaRepository<InscripcionJpaEntity, UUID> {
    List<InscripcionJpaEntity> findByCorredorId(UUID corredorId);
    List<InscripcionJpaEntity> findByEventoId(UUID eventoId);
    boolean existsByEventoIdAndCorredorId(UUID eventoId, UUID corredorId);
}

// 3.3 Adapter
@Repository
@RequiredArgsConstructor
public class InscripcionRepositoryAdapter implements InscripcionRepository {
    private final InscripcionJpaRepository jpaRepository;
    
    @Override
    public Inscripcion save(Inscripcion inscripcion) {
        InscripcionJpaEntity entity = InscripcionMapper.toEntity(inscripcion);
        InscripcionJpaEntity saved = jpaRepository.save(entity);
        return InscripcionMapper.toDomain(saved);
    }
    
    // ... otros métodos
}
```

#### Paso 4: Presentation Layer

```java
@RestController
@RequestMapping("/api/v1/inscripciones")
@RequiredArgsConstructor
@Slf4j
public class InscripcionController {
    
    private final InscripcionApplicationService inscripcionApplicationService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<InscripcionResponse> inscribirse(
            @Valid @RequestBody InscribirseEventoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID corredorId = extractCorredorId(userDetails);
        InscripcionResponse response = inscripcionApplicationService.inscribirCorredor(corredorId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

---

## Checklist de Implementación

Cuando agregues una nueva feature, sigue este checklist:

### ✅ Domain Layer
- [ ] Crear modelo de dominio (sin anotaciones JPA)
- [ ] Usar `@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor` de Lombok
- [ ] Agregar lógica de negocio en la entidad/modelo
- [ ] Crear excepciones de dominio si es necesario
- [ ] Usar `MessageKeys` para mensajes de error
- [ ] Definir Repository Port (interfaz sin extends JpaRepository)
- [ ] Definir Service Ports si hay dependencias externas
- [ ] Crear Use Case extendiendo `UseCase<Command, Result>`
- [ ] Usar `@RequiredArgsConstructor` para inyección de dependencias
- [ ] Definir records `Command` y `Result` dentro del UseCase
- [ ] Implementar método `internalExecute(Command command)`
- [ ] Escribir tests unitarios del Use Case (sin Spring)

### ✅ Application Layer
- [ ] Crear DTOs (Request/Response) como records
- [ ] Agregar validaciones en DTOs
- [ ] Crear Mappers DTO ↔ Domain
- [ ] Crear Application Service
- [ ] Orquestar casos de uso desde el Application Service
- [ ] Agregar `@Transactional` donde corresponda

### ✅ Infrastructure Layer
- [ ] Crear JPA Entity (con anotaciones `@Entity`, `@Table`)
- [ ] Crear Embeddables para datos cohesivos (`@Embeddable`)
- [ ] Usar `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` de Lombok
- [ ] Crear Spring Data Repository (extends `JpaRepository`)
- [ ] Crear Mapper Domain ↔ JPA/Embeddable (métodos estáticos)
- [ ] Usar `.builder()` en los mappers para claridad
- [ ] Crear Repository Adapter implementando el Port del dominio
- [ ] Implementar Service Adapters si hay (ej: MessageTranslator)
- [ ] Configurar relaciones JPA correctamente (`@ManyToOne`, `@OneToMany`, etc.)
- [ ] Usar `@Embedded` para incluir Embeddables en entidades

### ✅ Presentation Layer
- [ ] Crear Controller REST
- [ ] Agregar `@PreAuthorize` para seguridad
- [ ] Documentar endpoints con comentarios
- [ ] Agregar logging apropiado
- [ ] Actualizar Exception Handler si es necesario

### ✅ Testing
- [ ] Tests unitarios del dominio (sin Spring)
- [ ] Tests de integración del Application Service
- [ ] Tests de API (MockMvc)
- [ ] Tests de repositorio (DataJpaTest)

---

## Errores Comunes

### ❌ Error 1: Entidades de dominio con anotaciones JPA

```java
// ❌ MAL
@Entity
@Table(name = "eventos")
public class Evento {
    @Id
    private UUID id;
    // ...
}
```

```java
// ✅ BIEN
// Domain
public class Evento {
    private UUID id;
    // ...
}

// Infrastructure
@Entity
@Table(name = "eventos")
public class EventoJpaEntity {
    @Id
    private UUID id;
    // ...
}
```

### ❌ Error 2: Repositorio extiende JpaRepository en el dominio

```java
// ❌ MAL
public interface EventoRepository extends JpaRepository<Evento, UUID> {
    // ...
}
```

```java
// ✅ BIEN
// Domain
public interface EventoRepository {
    Optional<Evento> findById(UUID id);
    Evento save(Evento evento);
}

// Infrastructure
public interface EventoJpaRepository extends JpaRepository<EventoJpaEntity, UUID> {
    // ...
}

@Repository
public class EventoRepositoryAdapter implements EventoRepository {
    // ...
}
```

### ❌ Error 3: Lógica de negocio en el Application Service

```java
// ❌ MAL - Lógica de negocio en Application Service
@Service
public class EventoApplicationService {
    public void publicarEvento(UUID id) {
        Evento evento = eventoRepository.findById(id).orElseThrow();

        // ❌ Lógica de negocio aquí
        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            throw new IllegalStateException("...");
        }
        evento.setEstado(EstadoEvento.PUBLICADO);
        eventoRepository.save(evento);
    }
}
```

```java
// ✅ BIEN - Lógica en el Use Case
// Domain Use Case extendiendo UseCase<Command, Result>
@RequiredArgsConstructor
public class PublicarEventoUseCase extends UseCase<
        PublicarEventoUseCase.Command,
        PublicarEventoUseCase.Result> {

    private final EventoRepository eventoRepository;

    @Override
    protected Result internalExecute(Command command) {
        Evento evento = eventoRepository.findById(command.eventoId()).orElseThrow();

        // ✅ Lógica de negocio en el dominio
        evento.publicar(); // El método publicar() contiene las validaciones

        eventoRepository.save(evento);
        return new Result(evento);
    }

    public record Command(UUID eventoId) {}
    public record Result(Evento evento) {}
}

// Application Service solo orquesta
@Service
public class EventoApplicationService {
    @Transactional
    public EventoResponse publicarEvento(UUID id) {
        PublicarEventoUseCase useCase = new PublicarEventoUseCase(eventoRepository);
        PublicarEventoUseCase.Command command = new PublicarEventoUseCase.Command(id);
        PublicarEventoUseCase.Result result = useCase.execute(command);
        return EventoResponseMapper.fromDomain(result.evento());
    }
}
```

### ❌ Error 4: Controller accede directamente al repositorio

```java
// ❌ MAL
@RestController
public class EventoController {
    private final EventoRepository eventoRepository;
    
    @GetMapping("/{id}")
    public EventoResponse obtener(@PathVariable UUID id) {
        Evento evento = eventoRepository.findById(id).orElseThrow();
        return EventoResponseMapper.fromDomain(evento);
    }
}
```

```java
// ✅ BIEN
@RestController
public class EventoController {
    private final EventoApplicationService eventoApplicationService;
    
    @GetMapping("/{id}")
    public EventoResponse obtener(@PathVariable UUID id) {
        return eventoApplicationService.obtenerEvento(id);
    }
}
```

### ❌ Error 5: DTOs exponen entidades de dominio

```java
// ❌ MAL
public record EventoResponse(
    UUID id,
    String nombre,
    Organizador organizador // ❌ Entidad de dominio expuesta
) {}
```

```java
// ✅ BIEN
public record EventoResponse(
    UUID id,
    String nombre,
    OrganizadorDto organizador // ✅ DTO
) {}

public record OrganizadorDto(
    UUID id,
    String nombre
) {}
```

### ❌ Error 6: Usar @Transactional en el dominio

```java
// ❌ MAL
public class PublicarEventoUseCase {
    @Transactional // ❌ Anotación de Spring en el dominio
    public void execute(UUID eventoId) {
        // ...
    }
}
```

```java
// ✅ BIEN
// Domain Use Case sin anotaciones (solo @RequiredArgsConstructor de Lombok)
@RequiredArgsConstructor
public class PublicarEventoUseCase extends UseCase<Command, Result> {
    @Override
    protected Result internalExecute(Command command) {
        // ...
    }
}

// Application Service con @Transactional
@Service
public class EventoApplicationService {
    @Transactional // ✅ Transacción en la capa de aplicación
    public EventoResponse publicarEvento(UUID id) {
        // ...
    }
}
```

### ❌ Error 7: No usar Embeddables para datos cohesivos

```java
// ❌ MAL - Crear tablas separadas para datos cohesivos
@Entity
@Table(name = "users")
public class UserJpaEntity {
    @Id
    private UUID id;
    private String email;

    @OneToOne
    @JoinColumn(name = "personal_data_id")
    private PersonalDataEntity personalData; // ❌ Tabla separada innecesaria
}

@Entity
@Table(name = "personal_data")
public class PersonalDataEntity {
    @Id
    private UUID id;
    private String firstName;
    private String lastName;
}
```

```java
// ✅ BIEN - Usar Embeddable para datos cohesivos
@Entity
@Table(name = "users")
public class UserJpaEntity {
    @Id
    private UUID id;
    private String email;

    @Embedded // ✅ Embebido en la misma tabla
    private PersonalDataEmbeddable personalData;
}

@Embeddable
public class PersonalDataEmbeddable {
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private LocalDate birthDate;
}
```

### ❌ Error 8: No usar el módulo Shared para código reutilizable

```java
// ❌ MAL - Duplicar Value Objects en cada módulo
package com.grupocaos.products.athletix.user.domain.model;
public record Address(...) {} // Duplicado en user

package com.grupocaos.products.athletix.event.domain.model;
public record Address(...) {} // Duplicado en event
```

```java
// ✅ BIEN - Value Object en módulo shared
package com.grupocaos.products.athletix.shared.domain.valueobjects;
public record Address(...) {} // ✅ Una sola definición compartida

// Importar en cualquier módulo
import com.grupocaos.products.athletix.shared.domain.valueobjects.Address;
```

---

## Buenas Prácticas

### 💡 8. Internacionalización (i18n)

El proyecto implementa soporte de internacionalización con una interfaz en el dominio compartido:

```java
// Interfaz en shared/domain/i18n
package com.grupocaos.products.athletix.shared.domain.i18n;

public interface MessageTranslator {
    /**
     * Traduce un mensaje usando el locale actual del contexto
     */
    String translate(String key, Object... args);

    /**
     * Traduce un mensaje con un locale específico
     */
    String translate(String key, String locale, Object... args);
}
```

**Uso en excepciones de dominio:**
```java
package com.grupocaos.products.athletix.user.domain.exception;

import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String messageKey) {
        super(messageKey); // Pasa la clave, no el mensaje traducido
    }
}

// En el caso de uso
throw new UserNotFoundException(MessageKeys.AuthMessages.USER_NOT_FOUND);
```

**Keys centralizadas:**
```java
package com.grupocaos.products.athletix.shared.domain.i18n;

public class MessageKeys {
    public static class AuthMessages {
        public static final String USER_NOT_FOUND = "auth.user.not.found";
        public static final String INVALID_CREDENTIALS = "auth.invalid.credentials";
    }
}
```

**Implementación en infraestructura:**
- La implementación de `MessageTranslator` se coloca en `shared/infrastructure/i18n`
- Usa Spring's `MessageSource` internamente
- Los archivos de recursos están en `resources/messages_*.properties`

**Ventajas:**
- ✅ El dominio no depende de Spring
- ✅ Mensajes centralizados y consistentes
- ✅ Fácil soporte multi-idioma
- ✅ Keys tipadas previenen errores

### 💡 1. Usa Builders en Entidades de Dominio

```java
import lombok.Builder;

@Builder
public class Evento {
    private UUID id;
    private String nombre;
    private LocalDateTime fecha;

    private Evento() {
    }
}
```

### 💡 2. Usa Records para DTOs

```java
// ✅ Inmutables por defecto
public record CrearEventoRequest(
    String nombre,
    LocalDateTime fecha,
    String descripcion
) {
    // Constructor compacto para validaciones
    public CrearEventoRequest {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre requerido");
        }
    }
}
```

### 💡 3. Usa Command y Result Objects

```java
// Command Object para entrada
public record InscribirCorredorCommand(UUID eventoId, UUID corredorId, String comentarios) {
}

// Result Object para salida
public record InscripcionResult(Inscripcion inscripcion, boolean requiereConfirmacion) {

    // Getters
}
```

### 💡 4. Manejo de Excepciones por Capa

```java
// Domain - Excepciones de negocio
public class EventoNoPublicableException extends RuntimeException {
    public EventoNoPublicableException(String mensaje) {
        super(mensaje);
    }
}

// Application - Puede lanzar excepciones de dominio
@Service
public class EventoApplicationService {
    public EventoResponse publicarEvento(UUID id) {
        try {
            PublicarEventoUseCase useCase = new PublicarEventoUseCase(eventoRepository);
            PublicacionResult result = useCase.execute(id);
            return EventoResponseMapper.fromDomain(result.evento());
        } catch (EventoNotFoundException e) {
            throw e; // Re-lanzar excepciones de dominio
        }
    }
}

// Presentation - Maneja todas las excepciones
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EventoNoPublicableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleEventoNoPublicable(EventoNoPublicableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problem.setTitle("Evento No Publicable");
        return problem;
    }
}
```

### 💡 5. Logging Estratégico

```java
// Domain - Sin logging (lógica pura)
public class PublicarEventoUseCase {
    public PublicacionResult execute(UUID eventoId) {
        // Sin logs aquí
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();
        evento.publicar();
        eventoRepository.save(evento);
        return new PublicacionResult(evento);
    }
}

// Application - Logging de operaciones
@Service
@Slf4j
public class EventoApplicationService {
    @Transactional
    public EventoResponse publicarEvento(UUID id) {
        log.info("Publishing event: {}", id);
        
        PublicarEventoUseCase useCase = new PublicarEventoUseCase(eventoRepository);
        PublicacionResult result = useCase.execute(id);
        
        log.info("Event published successfully: {}", id);
        return EventoResponseMapper.fromDomain(result.evento());
    }
}

// Presentation - Logging de requests
@RestController
@Slf4j
public class EventoController {
    @PutMapping("/{id}/publicar")
    public ResponseEntity<EventoResponse> publicarEvento(@PathVariable UUID id) {
        log.info("PUT /api/v1/eventos/{}/publicar", id);
        EventoResponse response = eventoApplicationService.publicarEvento(id);
        return ResponseEntity.ok(response);
    }
}
```

### 💡 6. Tests Unitarios del Dominio

```java
// Test del dominio sin Spring
class PublicarEventoUseCaseTest {
    
    @Mock
    private EventoRepository eventoRepository;
    
    @Mock
    private NotificationService notificationService;
    
    private PublicarEventoUseCase useCase;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new PublicarEventoUseCase(eventoRepository, notificationService);
    }
    
    @Test
    void debePublicarEventoEnBorrador() {
        // Arrange
        UUID eventoId = UUID.randomUUID();
        Evento evento = Evento.builder()
            .id(eventoId)
            .nombre("Maratón 2024")
            .fecha(LocalDateTime.now().plusDays(30))
            .estado(EstadoEvento.BORRADOR)
            .build();
        
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // Act
        PublicacionResult result = useCase.execute(eventoId);
        
        // Assert
        assertNotNull(result);
        assertEquals(EstadoEvento.PUBLICADO, result.evento().getEstado());
        verify(eventoRepository).save(evento);
        verify(notificationService).notifyEventoPublicado(evento);
    }
    
    @Test
    void debeLanzarExcepcionSiEventoNoExiste() {
        // Arrange
        UUID eventoId = UUID.randomUUID();
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EventoNotFoundException.class, 
            () -> useCase.execute(eventoId));
    }
    
    @Test
    void debeLanzarExcepcionSiEventoYaPublicado() {
        // Arrange
        UUID eventoId = UUID.randomUUID();
        Evento evento = Evento.builder()
            .id(eventoId)
            .nombre("Maratón 2024")
            .fecha(LocalDateTime.now().plusDays(30))
            .estado(EstadoEvento.PUBLICADO) // Ya publicado
            .build();
        
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, 
            () -> useCase.execute(eventoId));
    }
}
```

### 💡 7. Value Objects para Conceptos de Negocio

Los Value Objects se ubican en el módulo `shared` para reutilización. Usa `records` de Java para inmutabilidad:

```java
// Value Object compartido usando record
package com.grupocaos.products.athletix.shared.domain.valueobjects;

import jakarta.annotation.Nonnull;

/**
 * Representa la dirección de un usuario.
 * @param street         Nombre de la calle.
 * @param externalNumber Número externo.
 * @param internalNumber Número interno (opcional).
 * @param neighborhood   Colonia o barrio.
 * @param city           Ciudad.
 * @param state          Estado o provincia.
 * @param country        País.
 * @param zipCode        Código postal.
 */
public record Address(
        String street,
        String externalNumber,
        String internalNumber,
        String neighborhood,
        String city,
        String state,
        String country,
        String zipCode
) {
    @Override
    @Nonnull
    public String toString() {
        StringBuilder result = new StringBuilder(street);
        if (city != null && !city.isBlank()) {
            result.append(", ").append(city);
        }
        if (state != null && !state.isBlank()) {
            result.append(", ").append(state);
        }
        return result.toString();
    }
}
```

**Ventajas de usar Value Objects en `shared`:**
- ✅ Reutilización entre módulos (user, event, payment, etc.)
- ✅ Conceptos de dominio consistentes en toda la aplicación
- ✅ Lógica de validación centralizada
- ✅ Inmutabilidad garantizada con records

---

## Plantillas de Código

### Plantilla: Nueva Entidad de Dominio

```java
package com.grupocaos.products.athletix.[feature].domain.model;

import java.util.UUID;

public class [NombreEntidad] {
    
    private UUID id;
    private [Tipo] campo1;
    private [Tipo] campo2;
    
    // Constructor privado
    private [NombreEntidad]() {}
    
    // Factory method o Builder
    public static [NombreEntidad] crear([Parametros]) {
        [NombreEntidad] entidad = new [NombreEntidad]();
        entidad.id = UUID.randomUUID();
        entidad.campo1 = campo1;
        // Validaciones
        entidad.validar();
        return entidad;
    }
    
    // Métodos de negocio
    public void metodoDeNegocio() {
        // Validaciones
        if (condicion) {
            throw new IllegalStateException("Mensaje");
        }
        // Cambio de estado
        this.campo1 = nuevoValor;
    }
    
    // Validaciones
    private void validar() {
        if (campo1 == null) {
            throw new IllegalArgumentException("Campo1 es requerido");
        }
    }
    
    // Getters (sin setters públicos)
    public UUID getId() { return id; }
    public [Tipo] getCampo1() { return campo1; }
}
```

### Plantilla: Nuevo Use Case

```java
package com.grupocaos.products.athletix.[feature].domain.usecase;

import com.grupocaos.products.athletix.shared.domain.usecase.UseCase;
import lombok.RequiredArgsConstructor;

/**
 * Use case para [descripción del caso de uso].
 * [Descripción detallada del propósito y comportamiento]
 */
@RequiredArgsConstructor
public class [Nombre]UseCase extends UseCase<
        [Nombre]UseCase.Command,
        [Nombre]UseCase.Result> {

    private final [Dependency1]Repository dependency1Repository;
    private final [Dependency2]Service dependency2Service;

    @Override
    protected Result internalExecute(Command command) {
        // 1. Validaciones
        validar(command);

        // 2. Obtener entidades
        [Entidad] entidad = dependency1Repository.findById(command.id())
            .orElseThrow(() -> new [Entidad]NotFoundException(command.id()));

        // 3. Aplicar lógica de negocio
        entidad.metodoDeNegocio();

        // 4. Persistir cambios
        [Entidad] entidadActualizada = dependency1Repository.save(entidad);

        // 5. Efectos secundarios (opcional)
        dependency2Service.doSomething(entidadActualizada);

        // 6. Retornar resultado
        return new Result(entidadActualizada);
    }

    private void validar(Command command) {
        // Validaciones de negocio
    }

    /**
     * Record que representa el comando de entrada para [Nombre]UseCase.
     * @param id Identificador [descripción]
     * @param [otros parametros] [descripción]
     */
    public record Command(UUID id, [Parametros]) {}

    /**
     * Record que representa el resultado de [Nombre]UseCase.
     * @param entidad [descripción]
     */
    public record Result([Entidad] entidad) {}
}
```

### Plantilla: Nuevo Application Service

```java
package com.grupocaos.products.athletix.[feature].application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class [Feature]ApplicationService {
    
    private final [Dependency1]Repository dependency1Repository;
    private final [Dependency2]Repository dependency2Repository;
    
    @Transactional
    public [Response] ejecutarAccion([Request] request) {
        log.info("Executing action for: {}", request);
        
        // 1. Crear comando desde DTO
        [UseCase].[Command] command = new [UseCase].[Command](
            request.param1(),
            request.param2()
        );
        
        // 2. Crear y ejecutar caso de uso
        [UseCase] useCase = new [UseCase](
            dependency1Repository,
            dependency2Repository
        );
        
        [UseCase].[Result] result = useCase.execute(command);
        
        log.info("Action executed successfully");
        
        // 3. Mapear a DTO de respuesta
        return [Response]Mapper.fromDomain(result.entidad());
    }
}
```

### Plantilla: Nuevo Controller

```java
package com.grupocaos.products.athletix.[feature].presentation.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/[feature]")
@RequiredArgsConstructor
@Slf4j
public class [Feature]Controller {
    
    private final [Feature]ApplicationService applicationService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_[ROLE]')")
    public ResponseEntity<[Response]> crear(
            @Valid @RequestBody [Request] request) {
        
        log.info("POST /api/v1/[feature] - Request: {}", request);
        
        [Response] response = applicationService.crear(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<[Response]> obtener(@PathVariable UUID id) {
        log.info("GET /api/v1/[feature]/{}", id);
        
        [Response] response = applicationService.obtener(id);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_[ROLE]')")
    public ResponseEntity<[Response]> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody [Request] request) {
        
        log.info("PUT /api/v1/[feature]/{}", id);
        
        [Response] response = applicationService.actualizar(id, request);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_[ROLE]')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.info("DELETE /api/v1/[feature]/{}", id);
        
        applicationService.eliminar(id);
        
        return ResponseEntity.noContent().build();
    }
}
```

---

---

## Módulo Shared: Código Compartido

El módulo `shared` contiene código reutilizable entre todos los módulos de features:

### 📦 shared/domain/
- **usecase/**: Clase base `UseCase<Command, Result>` para todos los casos de uso
- **valueobjects/**: Value Objects compartidos (Address, Money, Email, etc.)
- **i18n/**: Interfaces para internacionalización (`MessageTranslator`, `MessageKeys`)

### 📦 shared/application/
- **dto/**: DTOs compartidos entre módulos (ej: `PageResponse`, `ErrorResponse`)

### 📦 shared/infrastructure/
- **common/**: Utilidades comunes (date handlers, string utils, etc.)
- **i18n/**: Implementación de `MessageTranslator` usando Spring's `MessageSource`

### Cuándo usar Shared:
✅ **SÍ usar shared para:**
- Value Objects usados en múltiples módulos
- Clase base UseCase
- Interfaces de servicios cross-cutting (i18n, logging)
- DTOs de respuesta genéricos (pagination, errors)
- Constantes globales (message keys)

❌ **NO usar shared para:**
- Lógica específica de un módulo
- Entidades de dominio de un feature
- Configuraciones específicas de infraestructura de un módulo

---

## Conclusión

Este manual debe ser tu guía de referencia al desarrollar nuevas features. Recuerda:

1. **Domain primero**: Empieza siempre por el dominio
2. **Use Case base**: Todos los casos de uso extienden `UseCase<Command, Result>`
3. **Tests unitarios**: Testea el dominio sin Spring
4. **Separación clara**: Cada capa tiene su responsabilidad
5. **Inversión de dependencias**: El dominio define, la infraestructura implementa
6. **Embeddables**: Usa `@Embeddable` para datos cohesivos, no tablas separadas
7. **Value Objects compartidos**: Ubica en `shared/domain/valueobjects/`
8. **Internacionalización**: Usa `MessageKeys` y `MessageTranslator`
9. **Consistencia**: Sigue los mismos patrones en todo el proyecto

### Recursos Adicionales

- **Clean Architecture** by Robert C. Martin
- **Domain-Driven Design** by Eric Evans
- **Implementing Domain-Driven Design** by Vaughn Vernon

---

---

## Módulo Bootstrap: Inicialización del Sistema

El módulo `bootstrap` gestiona la inicialización y configuración inicial del sistema:

### Estructura:
```
bootstrap/
├── domain/
│   ├── SystemInitializer.java           # Interfaz para inicializadores
│   └── ExecuteSystemInitializersUseCase.java
├── application/
│   └── service/
│       └── BootstrapService.java        # Orquesta la inicialización
└── infrastructure/
    └── config/
        └── SystemInitializationConfig.java
```

### Uso:
El sistema de bootstrap permite ejecutar lógica de inicialización al arrancar la aplicación:

```java
// Implementar SystemInitializer en cada módulo que necesite inicialización
public interface SystemInitializer {
    void initialize();
    int getOrder(); // Para controlar el orden de ejecución
}
```

**Casos de uso:**
- Crear roles y permisos por defecto
- Inicializar datos maestros
- Configurar valores iniciales de sistema
- Ejecutar migraciones de datos

---

**Última actualización**: 2025-12-19
**Versión**: 2.0
**Mantenido por**: Equipo AccesoSport / Athletix

---

## Resumen de Cambios Arquitectónicos (v2.0)

### Nuevos Patrones Implementados:

1. **UseCase Base Abstracto**
   - Todos los casos de uso extienden `UseCase<Command, Result>`
   - Método `internalExecute(Command)` para implementar lógica
   - Soporte para ejecución con y sin comando

2. **Embeddables JPA**
   - Uso de `@Embeddable` para agrupar datos cohesivos
   - Evita tablas separadas innecesarias
   - Mappers específicos Domain ↔ Embeddable

3. **Módulo Shared**
   - `shared/domain/usecase/`: Clase base UseCase
   - `shared/domain/valueobjects/`: Value Objects compartidos (Address, etc.)
   - `shared/domain/i18n/`: Interfaces de internacionalización
   - `shared/infrastructure/`: Implementaciones compartidas

4. **Sistema de Internacionalización**
   - `MessageTranslator` interface en dominio
   - `MessageKeys` para claves tipadas
   - Implementación en infraestructura con Spring MessageSource

5. **Records de Java**
   - DTOs como records inmutables
   - Command y Result como records dentro de los UseCases
   - Value Objects como records

6. **Módulo Bootstrap**
   - `SystemInitializer` interface para inicialización
   - `ExecuteSystemInitializersUseCase` para orquestar inicializadores
   - Útil para datos maestros y configuración inicial

### Convenciones de Código:

- **Lombok**: `@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor` para modelos
- **Lombok**: `@RequiredArgsConstructor` para casos de uso (inyección de dependencias)
- **Records**: Para DTOs, Commands, Results y Value Objects
- **Builders**: Uso preferido en mappers para claridad
- **Documentación**: JavaDoc completo en clases públicas