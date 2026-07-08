package com.accesosport.user.application.usecase;

import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.domain.port.EmailTemplatePort;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.exception.ProfileNotFoundException;
import com.accesosport.user.domain.exception.StripeAlreadyLinkedException;
import com.accesosport.user.domain.model.UserOrganizerProfile;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class SendStripeOnboardingReminderUseCase extends UseCase<SendStripeOnboardingReminderUseCase.Command, Void> {

    private final OrganizerProfileRepository organizerProfileRepository;
    private final EmailService emailService;
    private final EmailTemplatePort emailTemplatePort;
    private final String dashboardUrl;

    @Override
    protected Void internalExecute(Command command) {
        UserOrganizerProfile profile = organizerProfileRepository.findById(command.organizerProfileId())
                .orElseThrow(() -> new ProfileNotFoundException(MessageKeys.Users.USER_PROFILE_ORGANIZER_NOT_FOUND));

        if (profile.isStripeLinked()) {
            throw new StripeAlreadyLinkedException(MessageKeys.Admin.ORGANIZER_STRIPE_ALREADY_LINKED);
        }

        String name = profile.getUser().getPersonalData() != null
                ? profile.getUser().getPersonalData().getFirstName()
                : profile.getOrganizationName();

        emailService.send(EmailMessage.of(
                profile.getUser().getEmail(),
                "Configura tus pagos para recibir inscripciones en AccesoSport",
                emailTemplatePort.buildStripeOnboardingReminderEmail(name, dashboardUrl)
        ));

        return null;
    }

    public record Command(UUID organizerProfileId) {}
}
