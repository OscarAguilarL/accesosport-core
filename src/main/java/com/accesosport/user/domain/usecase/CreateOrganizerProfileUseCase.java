package com.accesosport.user.domain.usecase;

import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.exception.UserNotFoundException;
import com.accesosport.user.domain.model.RoleEnumeration;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.model.UserOrganizerProfile;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import com.accesosport.user.domain.repository.RoleRepository;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case to create the participant profile
 * At the moment of the creation the user is assigned with the role Organizer
 */
@RequiredArgsConstructor
public class CreateOrganizerProfileUseCase
        extends UseCase<CreateOrganizerProfileUseCase.Command, CreateOrganizerProfileUseCase.Result> {

    private final OrganizerProfileRepository organizerProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    protected Result internalExecute(Command command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(MessageKeys.AuthMessages.USER_NOT_FOUND));

        if (user.hasRole(RoleEnumeration.ROLE_ORGANIZER)) {
            throw new IllegalStateException(MessageKeys.Users.USER_PROFILE_ORGANIZER_ALREADY_HAS_ROLE);
        }

        if (organizerProfileRepository.findByUserId(command.userId()).isPresent()) {
            throw new IllegalStateException(MessageKeys.Users.USER_PROFILE_ORGANIZER_ALREADY_HAS_PROFILE);
        }

        UserOrganizerProfile userOrganizerProfile = UserOrganizerProfile.create(
                command.organizationName(),
                command.website(),
                command.facebook(),
                command.instagram(),
                command.description(),
                user
        );

        var userRole = roleRepository.findByRole(RoleEnumeration.ROLE_ORGANIZER)
                .orElseThrow(() -> new IllegalStateException(MessageKeys.Users.ROLE_NOT_FOUND));
        user.addRole(userRole);
        userRepository.save(user);

        return new Result(organizerProfileRepository.save(userOrganizerProfile));
    }

    /**
     * The command input to create the participant profile
     *
     * @param organizationName Organization name of the event organizer
     * @param website          website of the organizer (optional)
     * @param facebook         Facebook of the organizer (optional)
     * @param instagram        Instagram of the organizer (optional)
     * @param description      description of the event organizer
     * @param userId           Organizer user identifier
     */
    public record Command(
            String organizationName,
            String website,
            String facebook,
            String instagram,
            String description,
            UUID userId
    ) {
    }

    /**
     * The result of the use case
     *
     * @param profile the UserOrganizerProfile resultant
     */
    public record Result(UserOrganizerProfile profile) {
    }
}
