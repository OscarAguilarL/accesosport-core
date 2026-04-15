package com.accesosport.registration.application.usecase;

import com.accesosport.registration.application.dto.AssignBibNumbersCommand;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
public class AssignBibNumbersUseCase extends UseCase<AssignBibNumbersCommand, Void> {

    private final RegistrationRepository registrationRepository;

    @Override
    protected Void internalExecute(AssignBibNumbersCommand command) {
        List<Registration> allConfirmed = registrationRepository.findConfirmedByEventId(command.eventId());

        int nextBib = allConfirmed.stream()
                .filter(r -> r.getBibNumber() != null)
                .mapToInt(Registration::getBibNumber)
                .max()
                .orElse(0) + 1;

        List<Registration> unassigned = allConfirmed.stream()
                .filter(r -> r.getBibNumber() == null)
                .sorted(Comparator.comparing(Registration::getRegisteredAt))
                .toList();

        for (Registration r : unassigned) {
            r.assignBibNumber(nextBib++);
        }

        if (!unassigned.isEmpty()) {
            registrationRepository.saveAll(unassigned);
        }

        return null;
    }
}
