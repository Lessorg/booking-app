package test.project.bookingapp.dto.bookingdtos;

import test.project.bookingapp.model.booking.BookingStatus;

public record BookingSearchParametersDto(
        Long userId,
        BookingStatus status
) {
}
