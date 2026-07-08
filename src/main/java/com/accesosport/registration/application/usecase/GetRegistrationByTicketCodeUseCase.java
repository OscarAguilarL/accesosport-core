package com.accesosport.registration.application.usecase;

import com.accesosport.registration.application.dto.ParticipantInEventResponse;
import com.accesosport.registration.domain.exception.RegistrationNotFoundException;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.model.PersonalData;
import com.accesosport.user.domain.model.UserParticipantProfile;
import com.accesosport.user.domain.repository.ParticipantProfileRepository;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class GetRegistrationByTicketCodeUseCase extends UseCase<GetRegistrationByTicketCodeUseCase.Command, ParticipantInEventResponse> {

    private final RegistrationRepository registrationRepository;
    private final ParticipantProfileRepository participantProfileRepository;

    public record Command(String ticketCode) {}

    @Override
    protected ParticipantInEventResponse internalExecute(Command command) {
        Registration registration = registrationRepository.findByTicketCode(command.ticketCode())
                .orElseThrow(() -> new RegistrationNotFoundException(command.ticketCode()));
        return toResponse(registration);
    }

    public ParticipantInEventResponse toResponse(Registration r) {
        String fullName = null;
        String email = null;
        String shirtSize = null;
        String bloodType = null;
        String medicalConditions = null;
        String emergencyContactName = null;
        String emergencyContactPhone = null;

        if (r.getParticipantId() != null) {
            Optional<UserParticipantProfile> profileOpt = participantProfileRepository.findByUserId(r.getParticipantId());
            if (profileOpt.isPresent()) {
                UserParticipantProfile profile = profileOpt.get();
                if (profile.getUser() != null) {
                    email = profile.getUser().getEmail();
                    PersonalData pd = profile.getUser().getPersonalData();
                    if (pd != null) {
                        fullName = buildFullName(pd);
                    }
                }
                shirtSize = profile.getShirtSize() != null ? profile.getShirtSize().name() : null;
                bloodType = profile.getBloodType() != null ? profile.getBloodType().name() : null;
                medicalConditions = profile.getMedicalConditions();
                emergencyContactName = profile.getEmergencyContactName();
                emergencyContactPhone = profile.getEmergencyContactPhone();
            }
        }

        // Fall back to registration snapshot if profile data is missing
        if (email == null) email = r.getParticipantEmail();
        if (fullName == null) {
            String first = r.getParticipantFirstName() != null ? r.getParticipantFirstName() : "";
            String last = r.getParticipantLastName() != null ? r.getParticipantLastName() : "";
            String combined = (first + " " + last).trim();
            if (!combined.isEmpty()) fullName = combined;
        }
        if (shirtSize == null) shirtSize = r.getShirtSize();
        if (bloodType == null) bloodType = r.getBloodType();
        if (medicalConditions == null) medicalConditions = r.getMedicalConditions();
        if (emergencyContactName == null) emergencyContactName = r.getEmergencyContactName();
        if (emergencyContactPhone == null) emergencyContactPhone = r.getEmergencyContactPhone();

        return new ParticipantInEventResponse(
                r.getId(),
                r.getParticipantId(),
                fullName,
                email,
                shirtSize,
                bloodType,
                medicalConditions,
                emergencyContactName,
                emergencyContactPhone,
                r.getStatus().name(),
                r.getTicketCode(),
                r.getBibNumber(),
                r.isKitPickedUp(),
                r.getKitPickedUpAt(),
                r.getRegisteredAt(),
                r.isWantsShirt()
        );
    }

    private String buildFullName(PersonalData pd) {
        StringBuilder sb = new StringBuilder();
        if (pd.getFirstName() != null) sb.append(pd.getFirstName());
        if (pd.getLastName() != null) sb.append(" ").append(pd.getLastName());
        if (pd.getSecondLastName() != null && !pd.getSecondLastName().isBlank()) {
            sb.append(" ").append(pd.getSecondLastName());
        }
        return sb.toString().trim();
    }
}
