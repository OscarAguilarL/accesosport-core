package com.accesosport.auth.application.usecase;

import com.accesosport.auth.domain.exception.InvalidCurrentPasswordException;
import com.accesosport.auth.domain.service.PasswordEncoder;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.user.domain.exception.UserNotFoundException;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public record Command(UUID userId, String currentPassword, String newPassword) {}

    public void execute(Command command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(MessageKeys.AuthMessages.USER_NOT_FOUND));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }

        user.changePassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
    }
}
