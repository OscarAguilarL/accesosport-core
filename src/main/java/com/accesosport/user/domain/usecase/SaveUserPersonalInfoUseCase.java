package com.accesosport.user.domain.usecase;

import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.exception.UserNotFoundException;
import com.accesosport.user.domain.model.PersonalData;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Use case for saving personal information of a user.
 * This use case retrieves a user by their unique identifier, updates their personal
 * data if provided in the input command, and persists the updates.
 * <p>
 * It relies on the {@link UserRepository} to retrieve and save the user,
 * and throws a {@link UserNotFoundException} if the user cannot be found.
 * Only the non-null fields from the input command are used to update the user's personal data.
 */
@RequiredArgsConstructor
public class SaveUserPersonalInfoUseCase extends UseCase<SaveUserPersonalInfoUseCase.Command, SaveUserPersonalInfoUseCase.Result> {

    private final UserRepository userRepository;

    @Override
    protected Result internalExecute(Command command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(MessageKeys.AuthMessages.USER_NOT_FOUND));

        PersonalData personalData = new PersonalData();

        personalData.setFirstName(command.firstName());
        personalData.setLastName(command.lastName());
        if (command.secondLastName() != null) {
            personalData.setSecondLastName(command.secondLastName());
        }
        personalData.setBirthDate(command.birthDate());
        personalData.setGender(command.gender());
        personalData.setPhoneNumber(command.phoneNumber());

        user.setPersonalData(personalData);
        userRepository.save(user);

        return new Result(personalData);
    }

    /**
     * Data structure representing a command to update a user's personal information.
     * It is designed to be used as an input for use cases that handle the update of
     * user details.
     *
     * @param userId         Unique identifier of the user.
     * @param firstName      First name of the user. If null, this field will not be updated.
     * @param lastName       Last name of the user. If null, this field will not be updated.
     * @param secondLastName Second last name of the user. If null, this field will not be updated.
     * @param birthDate      Birthdate of the user. If null, this field will not be updated.
     * @param gender         Gender of the user. If null, this field will not be updated.
     * @param phoneNumber    Phone number of the user. If null, this field will not be updated.
     */
    public record Command(
            UUID userId,
            String firstName,
            String lastName,
            String secondLastName,
            LocalDate birthDate,
            String gender,
            String phoneNumber
    ) {
    }

    /**
     * Represents the outcome of the SaveUserPersonalInfoUseCase execution.
     * Encapsulates the personal data of a user after it has been updated and persisted.
     *
     * @param personalData Updated personal data of the user from the executed use case.
     */
    public record Result(PersonalData personalData) {
    }
}
