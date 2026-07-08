package com.accesosport.payment.application.usecase;

import com.accesosport.payment.application.dto.ConnectOnboardingResponse;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.model.UserOrganizerProfile;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class ConnectOnboardingUseCase extends UseCase<ConnectOnboardingUseCase.Command, ConnectOnboardingResponse> {

    public record Command(UUID organizerUserId) {}

    private final OrganizerProfileRepository organizerProfileRepository;
    private final PaymentProcessorPort paymentProcessorPort;

    @Override
    protected ConnectOnboardingResponse internalExecute(Command command) {
        UserOrganizerProfile profile = organizerProfileRepository.findByUserId(command.organizerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Organizer profile not found"));

        if (profile.getStripeAccountId() == null) {
            PaymentProcessorPort.ConnectAccountResult account = paymentProcessorPort.createConnectAccount(
                    profile.getUser().getEmail(), profile.getOrganizationName()
            );
            profile.linkStripeAccount(account.stripeAccountId());
            organizerProfileRepository.save(profile);
        }

        PaymentProcessorPort.OnboardingLinkResult link = paymentProcessorPort.createOnboardingLink(
                profile.getStripeAccountId()
        );

        return new ConnectOnboardingResponse(link.url());
    }
}
