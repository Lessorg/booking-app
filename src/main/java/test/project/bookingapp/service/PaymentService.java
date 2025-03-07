package test.project.bookingapp.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import test.project.bookingapp.dto.payment.CanceledPaymentResponseDto;
import test.project.bookingapp.dto.payment.PaymentRequestDto;
import test.project.bookingapp.dto.payment.PaymentResponseDto;
import test.project.bookingapp.model.User;

public interface PaymentService {
    Page<PaymentResponseDto> getPayments(User user, Long userId, Pageable pageable);

    PaymentResponseDto createPayment(@Valid PaymentRequestDto request, Long userId);

    PaymentResponseDto processSuccessfulPayment(String sessionId);

    CanceledPaymentResponseDto processCanceledPayment(String sessionId);

    PaymentResponseDto renewPaymentSession(Long paymentId);
}
