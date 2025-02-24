package test.project.bookingapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDto(
        @NotBlank @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName
) {}
