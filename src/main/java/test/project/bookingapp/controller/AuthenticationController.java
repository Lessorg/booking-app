package test.project.bookingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import test.project.bookingapp.dto.UserLoginRequestDto;
import test.project.bookingapp.dto.UserLoginResponseDto;
import test.project.bookingapp.dto.UserRegistrationRequestDto;
import test.project.bookingapp.dto.UserResponseDto;
import test.project.bookingapp.service.AuthenticationService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user",
            description = "Creates a new user account with the provided registration details")
    @PostMapping("/register")
    public UserResponseDto register(@Valid @RequestBody UserRegistrationRequestDto request) {
        return authenticationService.register(request);
    }

    @Operation(summary = "User login",
            description = "Authenticates the user and returns a JWT token")
    @PostMapping("/login")
    public UserLoginResponseDto login(@Valid @RequestBody UserLoginRequestDto request) {
        return authenticationService.authenticate(request);
    }
}
