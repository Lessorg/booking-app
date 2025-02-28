package test.project.bookingapp.dto.bookingdtos;

import java.time.LocalDate;

public record BookingResponseDto(
        Long id,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Long accommodationId,
        Long userId,
        String status
) {
}
