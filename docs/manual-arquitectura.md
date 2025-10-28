# 📘 Manual de Arquitectura Clean - Proyecto Athletix

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
```

- Las capas externas **SÍ** pueden depender de las internas
- Las capas internas **NO** pueden depender de las externas
- El **Domain** es el centro y no conoce a nadie

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

### 3. Separación de Entidades

**Entidades de Dominio** ≠ **Entidades JPA**

```java
// Domain Entity (sin anotaciones)
public class User {
    private UUID id;
    private String email;
    // Lógica de negocio
}

// Infrastructure JPA Entity (con anotaciones)
@Entity
@Table(name = "users")
public class UserJpaEntity {
    @Id
    private UUID id;
    private String email;
}
```

---

## Estructura del Proyecto

```
src/main/java/com/grupocaos/products/athletix/
│
├── 📁 [feature]/                    # Por cada feature/módulo
│   │
│   ├── 📁 domain/                   # ⭐ CAPA DE DOMINIO
│   │   ├── 📁 model/                # Entidades puras de negocio
│   │   ├── 📁 repository/           # Ports (interfaces)
│   │   ├── 📁 service/              # Ports de servicios
│   │   ├── 📁 usecase/              # Casos de uso (lógica de negocio)
│   │   └── 📁 exception/            # Excepciones de dominio
│   │
│   ├── 📁 application/              # 🔷 CAPA DE APLICACIÓN
│   │   ├── 📁 dto/                  # DTOs (Request/Response)
│   │   └── 📁 service/              # Servicios de aplicación
│   │
│   ├── 📁 infrastructure/           # 🔧 CAPA DE INFRAESTRUCTURA
│   │   ├── 📁 persistence/
│   │   │   ├── 📁 entity/           # Entidades JPA
│   │   │   ├── 📁 jpa/              # Spring Data Repositories
│   │   │   ├── 📁 adapter/          # Adapters de repositorio
│   │   │   └── 📁 mapper/           # Mappers Domain ↔ JPA
│   │   ├── 📁 config/               # Configuraciones
│   │   └── 📁 [other-adapters]/     # Otros adaptadores
│   │
│   └── 📁 presentation/             # 🌐 CAPA DE PRESENTACIÓN
│       └── 📁 rest/                 # Controllers REST
│           ├── AuthController.java
│           └── 📁 exception/        # Exception handlers
│
└── 📁 app/                          # Configuración global
    └── 📁 infrastructure/
        └── 📁 config/
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

public class PublicarEventoUseCase {
    
    private final EventoRepository eventoRepository;
    private final NotificationService notificationService;
    
    public PublicarEventoUseCase(EventoRepository eventoRepository,
                                 NotificationService notificationService) {
        this.eventoRepository = eventoRepository;
        this.notificationService = notificationService;
    }
    
    public PublicacionResult execute(UUID eventoId) {
        // 1. Obtener evento
        Evento evento = eventoRepository.findById(eventoId)
            .orElseThrow(() -> new EventoNotFoundException(eventoId));
        
        // 2. Aplicar lógica de negocio
        evento.publicar();
        
        // 3. Persistir
        Evento eventoPublicado = eventoRepository.save(evento);
        
        // 4. Notificar (efecto secundario)
        notificationService.notifyEventoPublicado(eventoPublicado);
        
        return new PublicacionResult(eventoPublicado);
    }
    
    public record PublicacionResult(Evento evento) {}
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
        
        // 1. Validar organizador existe
        Organizador organizador = organizadorRepository.findById(organizadorId)
            .orElseThrow(() -> new OrganizadorNotFoundException(organizadorId));
        
        // 2. Crear comando para el caso de uso
        CrearEventoUseCase.CrearEventoCommand command = 
            new CrearEventoUseCase.CrearEventoCommand(
                request.nombre(),
                request.descripcion(),
                request.fecha(),
                request.ubicacion().toDomain(),
                request.distanciaKm(),
                organizador
            );
        
        // 3. Ejecutar caso de uso
        CrearEventoUseCase useCase = new CrearEventoUseCase(
            eventoRepository,
            notificationService
        );
        
        CrearEventoUseCase.CreacionResult result = useCase.execute(command);
        
        // 4. Mapear a DTO de respuesta
        return EventoResponseMapper.fromDomain(result.evento());
    }
    
    @Transactional
    public EventoResponse publicarEvento(UUID eventoId) {
        log.info("Publishing event: {}", eventoId);
        
        PublicarEventoUseCase useCase = new PublicarEventoUseCase(
            eventoRepository,
            notificationService
        );
        
        PublicarEventoUseCase.PublicacionResult result = useCase.execute(eventoId);
        
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

**Ejemplo de Mapper:**
```java
package com.grupocaos.products.athletix.evento.infrastructure.persistence.mapper;

public class EventoMapper {
    
    public static Evento toDomain(EventoJpaEntity entity) {
        if (entity == null) return null;
        
        return Evento.builder()
            .id(entity.getId())
            .nombre(entity.getNombre())
            .descripcion(entity.getDescripcion())
            .fecha(entity.getFecha())
            .estado(entity.getEstado())
            .ubicacion(UbicacionMapper.toDomain(entity.getUbicacion()))
            .distanciaKm(entity.getDistanciaKm())
            .organizador(OrganizadorMapper.toDomain(entity.getOrganizador()))
            .build();
    }
    
    public static EventoJpaEntity toEntity(Evento domain) {
        if (domain == null) return null;
        
        EventoJpaEntity entity = new EventoJpaEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setDescripcion(domain.getDescripcion());
        entity.setFecha(domain.getFecha());
        entity.setEstado(domain.getEstado());
        entity.setUbicacion(UbicacionMapper.toEmbeddable(domain.getUbicacion()));
        entity.setDistanciaKm(domain.getDistanciaKm());
        entity.setOrganizador(OrganizadorMapper.toEntity(domain.getOrganizador()));
        return entity;
    }
}
```

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

public class InscribirCorredorUseCase {
    
    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final CorredorRepository corredorRepository;
    
    public InscripcionResult execute(UUID eventoId, UUID corredorId) {
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
    
    public record InscripcionResult(Inscripcion inscripcion) {}
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
- [ ] Crear entidad de dominio (sin anotaciones)
- [ ] Agregar lógica de negocio en la entidad
- [ ] Crear excepciones de dominio si es necesario
- [ ] Definir Repository Port (interfaz)
- [ ] Definir Service Ports si hay dependencias externas
- [ ] Crear Use Case con la lógica de negocio
- [ ] Escribir tests unitarios del Use Case (sin Spring)

### ✅ Application Layer
- [ ] Crear DTOs (Request/Response) como records
- [ ] Agregar validaciones en DTOs
- [ ] Crear Mappers DTO ↔ Domain
- [ ] Crear Application Service
- [ ] Orquestar casos de uso desde el Application Service
- [ ] Agregar `@Transactional` donde corresponda

### ✅ Infrastructure Layer
- [ ] Crear JPA Entity (con anotaciones)
- [ ] Crear Spring Data Repository
- [ ] Crear Mapper Domain ↔ JPA
- [ ] Crear Repository Adapter
- [ ] Implementar Service Adapters si hay
- [ ] Configurar relaciones JPA correctamente

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
// Domain Use Case
public class PublicarEventoUseCase {
    public PublicacionResult execute(UUID eventoId) {
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();
        
        // ✅ Lógica de negocio en el dominio
        evento.publicar(); // El método publicar() contiene las validaciones
        
        eventoRepository.save(evento);
        return new PublicacionResult(evento);
    }
}

// Application Service solo orquesta
@Service
public class EventoApplicationService {
    public EventoResponse publicarEvento(UUID id) {
        PublicarEventoUseCase useCase = new PublicarEventoUseCase(eventoRepository);
        PublicacionResult result = useCase.execute(id);
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
// Domain Use Case sin anotaciones
public class PublicarEventoUseCase {
    public PublicacionResult execute(UUID eventoId) {
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

---

## Buenas Prácticas

### 💡 1. Usa Builders en Entidades de Dominio

```java
public class Evento {
    private UUID id;
    private String nombre;
    private LocalDateTime fecha;
    
    private Evento() {}
    
    public static EventoBuilder builder() {
        return new EventoBuilder();
    }
    
    public static class EventoBuilder {
        private final Evento evento = new Evento();
        
        public EventoBuilder nombre(String nombre) {
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("Nombre no puede estar vacío");
            }
            evento.nombre = nombre;
            return this;
        }
        
        public EventoBuilder fecha(LocalDateTime fecha) {
            if (fecha.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Fecha debe ser futura");
            }
            evento.fecha = fecha;
            return this;
        }
        
        public Evento build() {
            if (evento.nombre == null || evento.fecha == null) {
                throw new IllegalStateException("Evento incompleto");
            }
            if (evento.id == null) {
                evento.id = UUID.randomUUID();
            }
            return evento;
        }
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
public class InscribirCorredorCommand {
    private final UUID eventoId;
    private final UUID corredorId;
    private final String comentarios;
    
    public InscribirCorredorCommand(UUID eventoId, UUID corredorId, String comentarios) {
        this.eventoId = eventoId;
        this.corredorId = corredorId;
        this.comentarios = comentarios;
    }
    
    // Getters
}

// Result Object para salida
public class InscripcionResult {
    private final Inscripcion inscripcion;
    private final boolean requiereConfirmacion;
    
    public InscripcionResult(Inscripcion inscripcion, boolean requiereConfirmacion) {
        this.inscripcion = inscripcion;
        this.requiereConfirmacion = requiereConfirmacion;
    }
    
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

```java
// Value Object para Ubicación
public class Ubicacion {
    private final String direccion;
    private final String ciudad;
    private final String pais;
    private final double latitud;
    private final double longitud;
    
    private Ubicacion(String direccion, String ciudad, String pais, 
                      double latitud, double longitud) {
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
    }
    
    public static Ubicacion of(String direccion, String ciudad, String pais,
                               double latitud, double longitud) {
        validarCoordenadas(latitud, longitud);
        return new Ubicacion(direccion, ciudad, pais, latitud, longitud);
    }
    
    private static void validarCoordenadas(double latitud, double longitud) {
        if (latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("Latitud inválida");
        }
        if (longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Longitud inválida");
        }
    }
    
    public double calcularDistanciaA(Ubicacion otra) {
        // Lógica de negocio para calcular distancia
        return calcularHaversine(this.latitud, this.longitud, 
                                otra.latitud, otra.longitud);
    }
    
    // Getters
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ubicacion ubicacion = (Ubicacion) o;
        return Double.compare(latitud, ubicacion.latitud) == 0 &&
               Double.compare(longitud, ubicacion.longitud) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(latitud, longitud);
    }
}
```

### 💡 8. Domain Events para Efectos Secundarios

```java
// Domain Event
public abstract class DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredOn;
    
    protected DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
    }
    
    public UUID getEventId() { return eventId; }
    public LocalDateTime getOccurredOn() { return occurredOn; }
}

public class EventoPublicadoEvent extends DomainEvent {
    private final UUID eventoId;
    private final String nombreEvento;
    
    public EventoPublicadoEvent(UUID eventoId, String nombreEvento) {
        super();
        this.eventoId = eventoId;
        this.nombreEvento = nombreEvento;
    }
    
    // Getters
}

// Entidad con eventos
public class Evento {
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    public void publicar() {
        if (this.estado != EstadoEvento.BORRADOR) {
            throw new IllegalStateException("...");
        }
        this.estado = EstadoEvento.PUBLICADO;
        
        // Registrar evento de dominio
        this.registerEvent(new EventoPublicadoEvent(this.id, this.nombre));
    }
    
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

// Application Service publica eventos
@Service
public class EventoApplicationService {
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public EventoResponse publicarEvento(UUID id) {
        PublicarEventoUseCase useCase = new PublicarEventoUseCase(eventoRepository);
        PublicacionResult result = useCase.execute(id);
        
        // Publicar eventos de dominio
        result.evento().getDomainEvents().forEach(eventPublisher::publishEvent);
        result.evento().clearDomainEvents();
        
        return EventoResponseMapper.fromDomain(result.evento());
    }
}

// Event Listener en Infrastructure
@Component
@Slf4j
public class EventoEventListener {
    
    private final EmailService emailService;
    
    @EventListener
    @Async
    public void handleEventoPublicado(EventoPublicadoEvent event) {
        log.info("Handling EventoPublicadoEvent: {}", event.getEventoId());
        // Enviar notificaciones, actualizar estadísticas, etc.
        emailService.notifySubscribers(event.getEventoId());
    }
}
```

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

public class [Nombre]UseCase {
    
    private final [Dependency1]Repository dependency1Repository;
    private final [Dependency2]Service dependency2Service;
    
    public [Nombre]UseCase([Dependency1]Repository dependency1Repository,
                          [Dependency2]Service dependency2Service) {
        this.dependency1Repository = dependency1Repository;
        this.dependency2Service = dependency2Service;
    }
    
    public [Result] execute([Command] command) {
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
        return new [Result](entidadActualizada);
    }
    
    private void validar([Command] command) {
        // Validaciones de negocio
    }
    
    // Command object
    public record [Command]([Parametros]) {}
    
    // Result object
    public record [Result]([Entidad] entidad) {}
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

## Conclusión

Este manual debe ser tu guía de referencia al desarrollar nuevas features. Recuerda:

1. **Domain primero**: Empieza siempre por el dominio
2. **Tests unitarios**: Testea el dominio sin Spring
3. **Separación clara**: Cada capa tiene su responsabilidad
4. **Inversión de dependencias**: El dominio define, la infraestructura implementa
5. **Consistencia**: Sigue los mismos patrones en todo el proyecto

### Recursos Adicionales

- **Clean Architecture** by Robert C. Martin
- **Domain-Driven Design** by Eric Evans
- **Implementing Domain-Driven Design** by Vaughn Vernon

---

**Última actualización**: [Fecha]  
**Versión**: 1.0  
**Mantenido por**: Equipo Athletix