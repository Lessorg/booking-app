package test.project.bookingapp.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import test.project.bookingapp.dto.payment.CanceledPaymentResponseDto;
import test.project.bookingapp.dto.payment.PaymentRequestDto;
import test.project.bookingapp.dto.payment.PaymentResponseDto;
import test.project.bookingapp.exception.BookingDataException;
import test.project.bookingapp.exception.EntityNotFoundException;
import test.project.bookingapp.exception.InvalidStatusException;
import test.project.bookingapp.exception.StripeSessionException;
import test.project.bookingapp.exception.UnauthorizedAccessException;
import test.project.bookingapp.mapper.PaymentMapper;
import test.project.bookingapp.model.User;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.model.booking.BookingStatus;
import test.project.bookingapp.model.payment.Payment;
import test.project.bookingapp.model.payment.PaymentStatus;
import test.project.bookingapp.model.role.RoleName;
import test.project.bookingapp.repository.PaymentRepository;
import test.project.bookingapp.service.BookingService;
import test.project.bookingapp.service.NotificationService;
import test.project.bookingapp.service.PaymentService;

@Service
public class StripeService implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final BookingService bookingService;
    private final String baseUrl;
    private final NotificationService notificationService;

    public StripeService(@Value("${stripe.api.key}") String apiKey,
                         PaymentRepository paymentRepository,
                         PaymentMapper paymentMapper,
                         BookingService bookingService,
                         @Value("${app.base.url}") String baseUrl,
                         NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.bookingService = bookingService;
        this.baseUrl = baseUrl;
        this.notificationService = notificationService;
        Stripe.apiKey = apiKey;
    }

    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto request, Long userId) {
        Booking booking = bookingService.findBookingById(request.bookingId());
        Optional<Payment> existingPaymentOpt = paymentRepository.findByBookingId(booking.getId());

        if (!booking.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    "You are not the owner of this booking. Booking ID: " + booking.getId());
        }

        if (existingPaymentOpt.isPresent()) {
            Payment existingPayment = existingPaymentOpt.get();
            if (existingPayment.getStatus() == PaymentStatus.PAID) {
                throw new InvalidStatusException(
                        "Payment has already been completed for this booking.");
            } else if (existingPayment.getStatus() == PaymentStatus.EXPIRED) {
                return renewPaymentSession(existingPayment.getId());
            } else {
                return paymentMapper.toPaymentResponseDto(existingPayment);
            }
        }

        return createNewPayment(booking);
    }

    @Override
    public Page<PaymentResponseDto> getPayments(User user, Long userId, Pageable pageable) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMIN));

        if (!isAdmin && userId == null) {
            userId = user.getId();
        }

        if (userId != null) {
            return paymentRepository.findByBooking_User_Id(userId, pageable)
                    .map(paymentMapper::toPaymentResponseDto);
        } else {
            return paymentRepository.findAll(pageable)
                    .map(paymentMapper::toPaymentResponseDto);
        }
    }

    @Override
    public PaymentResponseDto processSuccessfulPayment(String sessionId) {
        String encodedSessionId = URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
        Session session;

        try {
            session = Session.retrieve(encodedSessionId);
        } catch (StripeException e) {
            throw new StripeSessionException("Failed to retrieve Stripe session for session ID: "
                    + sessionId, e);
        }

        if ("paid".equals(session.getPaymentStatus())) {
            Payment payment = paymentRepository.findBySessionId(encodedSessionId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No payment found for session ID: " + encodedSessionId));

            if (payment.getStatus() == PaymentStatus.PAID) {
                throw new InvalidStatusException("Payment has already been processed and marked as"
                        + " PAID. Current payment status: "
                        + payment.getStatus() + " for session ID: " + encodedSessionId);
            }

            payment.setStatus(PaymentStatus.PAID);
            Payment savedPayment = paymentRepository.save(payment);
            notificationService.sendPaymentSuccessNotification(savedPayment);
            return paymentMapper.toPaymentResponseDto(savedPayment);
        }

        throw new InvalidStatusException(
                "Payment session status is not 'paid'. Stripe session ID: "
                + sessionId + ", current payment status: " + session.getPaymentStatus());
    }

    @Override
    public CanceledPaymentResponseDto processCanceledPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid session ID: "
                        + sessionId));

        String message = getMessage(payment);
        return paymentMapper.toCanceledPaymentResponseDto(payment, message);
    }

    @Override
    public PaymentResponseDto renewPaymentSession(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid payment ID: "
                        + paymentId));
        Booking booking = payment.getBooking();
        BigDecimal amountToPay = calculateAmount(booking);

        try {
            Session session = Session.create(getSessionCreateParams(booking, amountToPay));

            payment.setSessionId(session.getId());
            payment.setSessionUrl(session.getUrl());
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            return paymentMapper.toPaymentResponseDto(payment);
        } catch (StripeException e) {
            throw new StripeSessionException("Failed to process renew payment", e);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkExpiredPayments() {
        paymentRepository.findAll().forEach(payment -> {
            try {
                Session session = Session.retrieve(payment.getSessionId());

                if (payment.getStatus() != PaymentStatus.PAID
                        && session.getExpiresAt() != null
                        && session.getExpiresAt() < System.currentTimeMillis() / 1000) {
                    payment.setStatus(PaymentStatus.EXPIRED);
                    paymentRepository.save(payment);
                }
            } catch (StripeException e) {
                throw new StripeSessionException("Failed to retrieve session id: "
                        + payment.getSessionId(), e);
            }
        });
    }

    private PaymentResponseDto createNewPayment(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING) {
            try {
                BigDecimal amountToPay = calculateAmount(booking);
                Session session = Session.create(getSessionCreateParams(booking, amountToPay));

                Payment payment = new Payment();
                payment.setBooking(booking);
                payment.setStatus(PaymentStatus.PENDING);
                payment.setSessionId(session.getId());
                payment.setSessionUrl(session.getUrl());
                payment.setAmount(amountToPay);

                Payment savedPayment = paymentRepository.save(payment);
                return paymentMapper.toPaymentResponseDto(savedPayment);

            } catch (StripeException e) {
                throw new StripeSessionException("Failed to create Stripe payment session", e);
            }
        } else {
            throw new InvalidStatusException("Booking is not in PENDING status: "
                    + booking.getStatus());
        }
    }

    private String getMessage(Payment payment) {
        String message;

        message = switch (payment.getStatus()) {
            case PENDING -> "The payment session is still available. "
                    + "You can complete the payment within 24 hours.";
            case EXPIRED -> "The payment session has expired. Please renew your session.";
            case PAID -> "Payment already processed.";
        };
        return message;
    }

    private BigDecimal calculateAmount(Booking booking) {
        if (booking.getAccommodation() == null || booking.getCheckInDate() == null
                || booking.getCheckOutDate() == null) {
            throw new BookingDataException("Booking details are incomplete ID: "
                    + booking.getId());
        }

        long daysBetween = ChronoUnit.DAYS.between(booking.getCheckInDate(),
                booking.getCheckOutDate());
        if (daysBetween <= 0) {
            throw new BookingDataException("Check-out date must be after check-in date ID: "
                    + booking.getId());
        }

        BigDecimal dailyRate = booking.getAccommodation().getDailyRate();
        if (dailyRate == null) {
            throw new BookingDataException("Accommodation price per day is not set ID: "
                    + booking.getId());
        }

        return dailyRate.multiply(BigDecimal.valueOf(daysBetween));
    }

    private SessionCreateParams getSessionCreateParams(Booking booking, BigDecimal amountToPay) {
        String successUrl = UriComponentsBuilder.fromUriString(baseUrl + "/payments/success")
                .queryParam("sessionId", "{placeholder}")
                .build(false)
                .toUriString()
                .replace("{placeholder}", "{CHECKOUT_SESSION_ID}");

        String cancelUrl = UriComponentsBuilder.fromUriString(baseUrl + "/payments/cancel")
                .queryParam("sessionId", "{placeholder}")
                .build(false)
                .toUriString()
                .replace("{placeholder}", "{CHECKOUT_SESSION_ID}");

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amountToPay.multiply(BigDecimal.valueOf(100))
                                        .longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData
                                        .ProductData.builder()
                                        .setName("Booking Payment for " + booking.getId())
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
