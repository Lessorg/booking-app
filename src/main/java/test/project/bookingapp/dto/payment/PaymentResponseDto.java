package test.project.bookingapp.dto.payment;

import java.math.BigDecimal;
import test.project.bookingapp.model.payment.PaymentStatus;

public record PaymentResponseDto(
        Long id,
        Long bookingId,
        PaymentStatus status,
        String sessionUrl,
        BigDecimal amount
) {
}
