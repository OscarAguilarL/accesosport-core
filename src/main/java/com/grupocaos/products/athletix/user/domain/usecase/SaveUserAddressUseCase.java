package com.grupocaos.products.athletix.user.domain.usecase;

import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;
import com.grupocaos.products.athletix.shared.domain.usecase.UseCase;
import com.grupocaos.products.athletix.shared.domain.valueobjects.Address;
import com.grupocaos.products.athletix.user.domain.exception.UserNotFoundException;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case for saving or updating the address of a user in the system.
 * This class orchestrates the address update process, validates the user's existence,
 * and persists the new address in the user repository.
 */
@RequiredArgsConstructor
public class SaveUserAddressUseCase extends UseCase<SaveUserAddressUseCase.Command, SaveUserAddressUseCase.Result> {

    private final UserRepository userRepository;

    @Override
    protected Result internalExecute(Command command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(MessageKeys.AuthMessages.USER_NOT_FOUND));

        Address address = new Address(
                command.street(),
                command.externalNumber(),
                command.internalNumber(),
                command.neighborhood(),
                command.city(),
                command.state(),
                command.country(),
                command.zipCode()
        );
        user.setAddress(address);
        userRepository.save(user);

        return new Result(address);
    }

    /**
     * Record representing a command to save a user's address information.
     * This command is used as input in use cases where a user's address needs to be updated
     * or saved in the system.
     *
     * @param userId         Unique identifier of the user whose address is being updated.
     * @param street         Street name of the user's address.
     * @param externalNumber External number associated with the user's address.
     * @param internalNumber Internal number associated with the user's address, if applicable.
     * @param neighborhood   Neighborhood or locality of the user's address.
     * @param city           City of the user's address.
     * @param state          State or province where the user's address is located.
     * @param country        Country of the user's address.
     * @param zipCode        Postal code or ZIP code of the user's address.
     */
    public record Command(
            UUID userId,
            String street,
            String externalNumber,
            String internalNumber,
            String neighborhood,
            String city,
            String state,
            String country,
            String zipCode
    ) {
    }

    /**
     * Represents the result of executing the SaveUserAddressUseCase.
     * Encapsulates the updated address information of a user after the operation is completed.
     *
     * @param address The updated address of the user. This includes information such as
     *                street name, external and internal numbers, neighborhood, city, state,
     *                country, and zip code.
     */
    public record Result(Address address) {
    }
}
