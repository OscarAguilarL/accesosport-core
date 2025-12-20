package com.grupocaos.products.athletix.user.presentation.rest;

import com.grupocaos.products.athletix.auth.infrastructure.security.CustomUserDetails;
import com.grupocaos.products.athletix.user.application.dto.SavePersonalDataRequest;
import com.grupocaos.products.athletix.user.application.dto.SaveUserAddressRequest;
import com.grupocaos.products.athletix.user.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller for managing user-related operations in the system.
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Saves the personal information of the authenticated user.
     * This endpoint accepts a request with personal data, validates it,
     * and stores it in the system for the corresponding user.
     *
     * @param request     the personal information to be saved, encapsulated in a
     *                    {@link SavePersonalDataRequest} object. It includes details like
     *                    first name, last name, second last name, birth date, gender, and phone number.
     * @param userDetails the details of the currently authenticated user, including their unique user ID.
     *                    Provided by the authentication principal.
     * @return a {@link ResponseEntity} with an empty body and a status of {@code 201 Created}
     * if the information is successfully saved.
     */
    @PutMapping("/personal-information")
    public ResponseEntity<Void> saveUserPersonalData(
            @Valid @RequestBody SavePersonalDataRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        userService.saveUserPersonalInformation(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Saves the address details of the authenticated user.
     * This endpoint accepts a request with address information and stores it
     * in the system for the corresponding user.
     *
     * @param request     the address information to be saved, encapsulated in a
     *                    {@code SaveUserAddressRequest} object. It includes details
     *                    like street, external number, city, state, country, and zip code.
     * @param userDetails the details of the currently authenticated user, including their unique user ID.
     *                    Provided by the authentication principal.
     * @return a {@code ResponseEntity} with an empty body and a status of {@code 201 Created}
     * if the address information is successfully saved.
     */
    @PutMapping("/address")
    public ResponseEntity<Void> saveUserAddress(
            @Valid @RequestBody SaveUserAddressRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        userService.saveUserAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
