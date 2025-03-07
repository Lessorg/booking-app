package test.project.bookingapp.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import test.project.bookingapp.dto.payment.PaymentRequestDto;
import test.project.bookingapp.dto.payment.PaymentResponseDto;
import test.project.bookingapp.exception.EntityNotFoundException;
import test.project.bookingapp.exception.InvalidStatusException;
import test.project.bookingapp.exception.StripeSessionException;
import test.project.bookingapp.mapper.PaymentMapper;
import test.project.bookingapp.model.User;
import test.project.bookingapp.model.accommodation.Accommodation;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.model.booking.BookingStatus;
import test.project.bookingapp.model.payment.Payment;
import test.project.bookingapp.model.payment.PaymentStatus;
import test.project.bookingapp.repository.PaymentRepository;
import test.project.bookingapp.service.BookingService;
import test.project.bookingapp.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class StripeServiceTests {
    private static final Long USER_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    private static final String SESSION_ID = "session-id";
    private static final String SESSION_URL = "http://stripe.url";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(700.0);
    private static final Long PAYMENT_ID = 1L;

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingService bookingService;
    @Mock private PaymentMapper paymentMapper;
    @Mock private NotificationService notificationService;
    @InjectMocks private StripeService stripeService;

    private User mockUser;
    private Accommodation mockAccommodation;
    private Booking mockBooking;
    private Payment mockPayment;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(USER_ID);

        mockAccommodation = new Accommodation();
        mockAccommodation.setId(1L);
        mockAccommodation.setLocation("Some Location");
        mockAccommodation.setSize("Large");
        mockAccommodation.setDailyRate(AMOUNT);
        mockAccommodation.setAvailability(5);

        mockBooking = new Booking();
        mockBooking.setId(BOOKING_ID);
        mockBooking.setCheckInDate(LocalDate.of(2025, 3, 7));
        mockBooking.setCheckOutDate(LocalDate.of(2025, 3, 14));
        mockBooking.setAccommodation(mockAccommodation);
        mockBooking.setUser(mockUser);
        mockBooking.setStatus(BookingStatus.PENDING);

        mockPayment = new Payment();
        mockPayment.setBooking(mockBooking);
        mockPayment.setStatus(PaymentStatus.PENDING);
        mockPayment.setSessionId("mock_session_id");
        mockPayment.setSessionUrl("http://mocked-session-url.com");
        mockPayment.setAmount(AMOUNT);
    }

    @Test
    @DisplayName("Create Payment - Success")
    void createPayment_Success() {
        PaymentResponseDto expectedResponseDto = new PaymentResponseDto(PAYMENT_ID,
                BOOKING_ID, PaymentStatus.PENDING, "http://mocked-session-url.com", AMOUNT);
        PaymentRequestDto requestDto = new PaymentRequestDto(BOOKING_ID);

        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("mock_session_id");
        when(mockSession.getUrl()).thenReturn("http://mocked-session-url.com");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            when(bookingService.findBookingById(BOOKING_ID)).thenReturn(mockBooking);
            when(paymentRepository.findByBookingId(mockBooking.getId())).thenReturn(
                    Optional.empty());
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);
            when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
            when(paymentMapper.toPaymentResponseDto(mockPayment)).thenReturn(expectedResponseDto);

            PaymentResponseDto response = stripeService.createPayment(requestDto,
                    mockBooking.getUser().getId());
            assertNotNull(response);
            assertEquals(expectedResponseDto, response);
        }
    }

    @Test
    @DisplayName("Create Payment - Payment Already Exists and Paid")
    void createPayment_AlreadyPaid() {
        Payment existingPayment = mock(Payment.class);
        when(existingPayment.getStatus()).thenReturn(PaymentStatus.PAID);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(USER_ID);

        Booking mockBooking = mock(Booking.class);
        when(mockBooking.getId()).thenReturn(BOOKING_ID);
        when(mockBooking.getUser()).thenReturn(mockUser);
        when(bookingService.findBookingById(BOOKING_ID)).thenReturn(mockBooking);
        when(paymentRepository.findByBookingId(BOOKING_ID))
                .thenReturn(Optional.of(existingPayment));

        PaymentRequestDto requestDto = new PaymentRequestDto(BOOKING_ID);
        assertThrows(InvalidStatusException.class,
                () -> stripeService.createPayment(requestDto, mockBooking.getUser().getId()));
    }

    @Test
    @DisplayName("Process Successful Payment - Success")
    void processSuccessfulPayment_Success() throws StripeException {
        String sessionId = SESSION_ID;
        Session mockSession = mock(Session.class);
        when(mockSession.getPaymentStatus()).thenReturn("paid");
        Payment savedPayment = mock(Payment.class);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId)).thenReturn(mockSession);
            when(paymentRepository.findBySessionId(sessionId))
                    .thenReturn(Optional.of(savedPayment));
            when(paymentRepository.save(savedPayment)).thenReturn(savedPayment);
            PaymentResponseDto expectedResponseDto = new PaymentResponseDto(PAYMENT_ID, BOOKING_ID,
                    PaymentStatus.PAID, SESSION_URL, AMOUNT);
            when(paymentMapper.toPaymentResponseDto(savedPayment)).thenReturn(expectedResponseDto);

            PaymentResponseDto response = stripeService.processSuccessfulPayment(sessionId);
            assertNotNull(response);
            Assertions.assertEquals(expectedResponseDto, response);
            verify(paymentRepository, times(1)).save(savedPayment);
            verify(notificationService, times(1))
                    .sendPaymentSuccessNotification(savedPayment);
        }
    }

    @Test
    @DisplayName("Process Canceled Payment - Payment Not Found")
    void processCanceledPayment_NotFound() {
        String sessionId = "invalid-session-id";

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> stripeService.processCanceledPayment(sessionId));
    }

    @Test
    @DisplayName("Renew Payment Session - Success")
    void renewPaymentSession_Success() {
        Long paymentId = 1L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPayment));
        PaymentResponseDto expectedResponseDto = new PaymentResponseDto(mockPayment.getId(),
                mockBooking.getId(), PaymentStatus.PENDING, SESSION_URL, AMOUNT);
        when(paymentMapper.toPaymentResponseDto(mockPayment)).thenReturn(expectedResponseDto);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);
            when(mockSession.getId()).thenReturn("mock_session_id");
            when(mockSession.getUrl()).thenReturn(SESSION_URL);
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);
            PaymentResponseDto response = stripeService.renewPaymentSession(paymentId);

            assertNotNull(response);
            assertEquals(expectedResponseDto, response);
            verify(paymentRepository, times(1)).save(mockPayment);
        }
    }

    @Test
    @DisplayName("Check Expired Payments - Handle StripeException")
    void checkExpiredPayments_StripeException() {
        StripeException mockException = mock(StripeException.class);

        when(paymentRepository.findAll()).thenReturn(List.of(mockPayment));
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(mockPayment.getSessionId()))
                    .thenThrow(mockException);

            assertThrows(StripeSessionException.class, () -> stripeService.checkExpiredPayments());
        }
    }
}
