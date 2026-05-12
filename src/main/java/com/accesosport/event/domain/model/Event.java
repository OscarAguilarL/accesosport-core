package com.accesosport.event.domain.model;

import com.accesosport.event.domain.exception.EventInvalidStatusException;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.user.domain.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Event {

    public static final String DEFAULT_WAIVER_TEMPLATE =
            "DESLINDE DE RESPONSABILIDAD Y CARTA DE CONSENTIMIENTO\n\n" +
            "Yo, {participantFullName}, declaro libremente y bajo mi propio consentimiento lo siguiente:\n\n" +
            "1. PARTICIPACIÓN VOLUNTARIA\n" +
            "   Reconozco que mi participación en el evento \"{eventName}\", a celebrarse el {eventDate}, es completamente voluntaria. " +
            "Entiendo la naturaleza física de la actividad y los riesgos que conlleva, incluyendo pero no limitándose a: caídas, lesiones " +
            "musculares, problemas cardiorrespiratorios, condiciones climáticas adversas, irregularidades del terreno y contacto con otros participantes.\n\n" +
            "2. ESTADO DE SALUD\n" +
            "   Declaro encontrarme en condiciones físicas y de salud aptas para participar en este evento. En caso de tener alguna condición " +
            "médica relevante, afirmo haber consultado a un médico y contar con su aprobación para participar. Asumo plena responsabilidad " +
            "por cualquier consecuencia derivada de mi estado de salud.\n\n" +
            "3. DESLINDE DE RESPONSABILIDAD\n" +
            "   Libero de toda responsabilidad civil, legal y económica a los organizadores del evento, a AccesoSport y a sus colaboradores, " +
            "por cualquier lesión, accidente, daño a mi persona o a mis pertenencias que pudiera ocurrir antes, durante o después del evento, " +
            "salvo en casos de negligencia grave comprobada imputable directamente a los organizadores.\n\n" +
            "4. MAYORÍA DE EDAD\n" +
            "   Declaro ser mayor de 18 años o, en caso de ser menor de edad, que cuento con la autorización expresa de mi padre, madre o " +
            "tutor legal para participar, quien asume conjuntamente las responsabilidades descritas en este documento.\n\n" +
            "5. USO DE IMAGEN\n" +
            "   Autorizo a los organizadores del evento y a AccesoSport a capturar fotografías y video durante el evento y a utilizarlos con " +
            "fines de difusión, promoción y redes sociales, sin que esto genere derecho a remuneración alguna a mi favor.\n\n" +
            "6. DATOS PERSONALES\n" +
            "   Consiento el tratamiento de mis datos personales por parte de AccesoSport y los organizadores del evento, únicamente para los " +
            "fines relacionados con mi inscripción y participación, de conformidad con la Ley Federal de Protección de Datos Personales en " +
            "Posesión de los Particulares (LFPDPPP).\n\n" +
            "Fecha y hora de aceptación: {waiverAcceptedAt}";

    @Setter
    private UUID id;
    @Setter
    private int version;
    private String name;
    private String description;
    private LocalDateTime eventDate;
    private Location location;
    private RegistrationPeriod registrationPeriod;
    @Setter
    private EventStatus status;
    private User createdBy;
    @Setter
    private LocalDateTime createdOn;
    @Setter
    private LocalDateTime updatedOn;
    @Setter
    private String coverImageUrl;
    @Setter
    private String coverImagePublicId;
    @Setter
    private LocalDateTime reminderSentAt;
    @Setter
    private String waiverTemplate;

    private Event() {
    }

    public static Event reconstitute(
            UUID id,
            int version,
            String name,
            String description,
            LocalDateTime eventDate,
            Location location,
            RegistrationPeriod registrationPeriod,
            EventStatus status,
            User createdBy,
            LocalDateTime createdOn,
            LocalDateTime updatedOn,
            String coverImageUrl,
            String coverImagePublicId,
            LocalDateTime reminderSentAt,
            String waiverTemplate
    ) {
        Event event = new Event();
        event.id = id;
        event.version = version;
        event.name = name;
        event.description = description;
        event.eventDate = eventDate;
        event.location = location;
        event.registrationPeriod = registrationPeriod;
        event.status = status;
        event.createdBy = createdBy;
        event.createdOn = createdOn;
        event.updatedOn = updatedOn;
        event.coverImageUrl = coverImageUrl;
        event.coverImagePublicId = coverImagePublicId;
        event.reminderSentAt = reminderSentAt;
        event.waiverTemplate = waiverTemplate;
        return event;
    }

    public static Event create(
            String name,
            String description,
            LocalDateTime eventDate,
            Location location,
            RegistrationPeriod registrationPeriod,
            User createdBy
    ) {
        Event event = new Event();
        event.id = UUID.randomUUID();
        event.name = name;
        event.description = description;
        event.eventDate = eventDate;
        event.location = location;
        event.registrationPeriod = registrationPeriod;
        event.status = EventStatus.DRAFT;
        event.createdBy = createdBy;
        event.createdOn = LocalDateTime.now();
        event.updatedOn = LocalDateTime.now();
        event.waiverTemplate = DEFAULT_WAIVER_TEMPLATE;

        event.validate();

        return event;
    }

    public void update(String name,
                       String description,
                       LocalDateTime eventDate,
                       Location location,
                       RegistrationPeriod registrationPeriod,
                       String waiverTemplate) {
        if (status != EventStatus.DRAFT) {
            throw new EventInvalidStatusException(MessageKeys.Events.EVENT_UPDATE_ONLY_DRAFT, status);
        }
        this.name = name;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.registrationPeriod = registrationPeriod;
        if (waiverTemplate != null) {
            this.waiverTemplate = waiverTemplate;
        }

        validate();
        this.updatedOn = LocalDateTime.now();
    }

    public void publish() {
        if (!status.canBePublished()) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_PUBLISH_ONLY_DRAFT);
        }

        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_PUBLISH_PAST_DATE);
        }
        this.status = EventStatus.PUBLISHED;
        this.updatedOn = LocalDateTime.now();
    }

    public void openRegistration() {
        if (!status.canOpenRegistration()) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_REGISTRATION_ONLY_PUBLISHED);
        }
        if (!registrationPeriod.isOpen()) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_REGISTRATION_PERIOD_CLOSED);
        }
        this.status = EventStatus.REGISTRATION_OPEN;
        this.updatedOn = LocalDateTime.now();
    }

    public void closeRegistration() {
        if (status != EventStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_REGISTRATION_NOT_OPEN);
        }
        this.status = EventStatus.REGISTRATION_CLOSED;
        this.updatedOn = LocalDateTime.now();
    }

    public void begin() {
        if (status != EventStatus.REGISTRATION_CLOSED) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_BEGIN_MUST_HAVE_CLOSED_REGISTRATION);
        }
        this.status = EventStatus.IN_PROGRESS;
        this.updatedOn = LocalDateTime.now();
    }

    public void complete() {
        if (status != EventStatus.IN_PROGRESS) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_COMPLETE_ONLY_IN_PROGRESS);
        }
        this.status = EventStatus.COMPLETED;
        this.updatedOn = LocalDateTime.now();
    }

    public void cancel() {
        if (!status.canBeCancelled()) {
            throw new EventInvalidStatusException(MessageKeys.Events.EVENT_CANCEL_INVALID_STATUS, status);
        }
        this.status = EventStatus.CANCELLED;
        this.updatedOn = LocalDateTime.now();
    }

    private void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_NAME_REQUIRED);
        }
        if (name.length() < 5 || name.length() > 200) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_NAME_LENGTH);
        }
        if (eventDate == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_DATE_REQUIRED);
        }
        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_DATE_FUTURE);
        }
        if (location == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_LOCATION_REQUIRED);
        }
        if (registrationPeriod == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_REQUIRED);
        }
        if (registrationPeriod.end().isAfter(eventDate)) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_BEFORE_EVENT);
        }
        if (createdBy == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_ORGANIZER_REQUIRED);
        }
    }
}
