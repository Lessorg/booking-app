package test.project.bookingapp.dto;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName
) {}
