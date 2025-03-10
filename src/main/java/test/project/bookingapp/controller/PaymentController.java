package test.project.bookingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import test.project.bookingapp.dto.payment.CanceledPaymentResponseDto;
import test.project.bookingapp.dto.payment.PaymentRequestDto;
import test.project.bookingapp.dto.payment.PaymentResponseDto;
import test.project.bookingapp.model.User;
import test.project.bookingapp.service.PaymentService;

@RequiredArgsConstructor
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Endpoints for managing payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Retrieve payments", description =
            "Allows customers to view their payments and managers to view all payments.")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')")
    public Page<PaymentResponseDto> getPayments(
            @RequestParam(required = false) Long userId,
            @ParameterObject @PageableDefault Pageable pageable,
            @AuthenticationPrincipal User user) {

        return paymentService.getPayments(user, userId, pageable);
    }

    @Operation(summary = "Initiate a payment",
            description = "Creates a payment session for a booking.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public PaymentResponseDto createPayment(@Valid @RequestBody PaymentRequestDto request,
                                            @AuthenticationPrincipal User user) {
        return paymentService.createPayment(request, user.getId());
    }

    @Operation(summary = "Handle successful payment",
            description = "Handles Stripe payment success callback.")
    @GetMapping("/success")
    public PaymentResponseDto handlePaymentSuccess(@RequestParam String sessionId) {
        return paymentService.processSuccessfulPayment(sessionId);
    }

    @Operation(summary = "Handle payment cancellation",
            description = "Handles Stripe payment cancellation callback.")
    @GetMapping("/cancel")
    public CanceledPaymentResponseDto handlePaymentCancel(@RequestParam String sessionId) {
        return paymentService.processCanceledPayment(sessionId);
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PutMapping("/renew/{paymentId}")
    public PaymentResponseDto renewPaymentSession(@PathVariable Long paymentId) {
        return paymentService.renewPaymentSession(paymentId);
    }
}
