package com.accesosport.registration.application.usecase;

import com.accesosport.registration.application.dto.GetEventRegistrationsCommand;
import com.accesosport.registration.application.dto.ParticipantInEventResponse;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.model.PersonalData;
import com.accesosport.user.domain.model.UserParticipantProfile;
import com.accesosport.user.domain.repository.ParticipantProfileRepository;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class GetEventRegistrationsUseCase extends UseCase<GetEventRegistrationsCommand, List<ParticipantInEventResponse>> {

    private final RegistrationRepository registrationRepository;
    private final ParticipantProfileRepository participantProfileRepository;

    @Override
    protected List<ParticipantInEventResponse> internalExecute(GetEventRegistrationsCommand command) {
        return registrationRepository.findByEventId(command.eventId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private ParticipantInEventResponse toResponse(Registration r) {
        Optional<UserParticipantProfile> profileOpt = participantProfileRepository.findByUserId(r.getParticipantId());

        String fullName = null;
        String email = null;
        String shirtSize = null;
        String bloodType = null;
        String medicalConditions = null;
        String emergencyContactName = null;
        String emergencyContactPhone = null;

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
