package test.project.bookingapp.dto.userdtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import test.project.bookingapp.validation.FieldMatch;

@FieldMatch(firstField = "password", secondField = "repeatPassword")
public record UserRegistrationRequestDto(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 35) String password,
        @NotBlank @Size(min = 8, max = 35) String repeatPassword,
        @NotBlank String firstName,
        @NotBlank String lastName
) {}

