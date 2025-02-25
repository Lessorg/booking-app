package test.project.bookingapp.dto.userdtos;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName
) {}
