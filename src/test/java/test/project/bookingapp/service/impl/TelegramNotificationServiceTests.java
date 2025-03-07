package test.project.bookingapp.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import test.project.bookingapp.model.User;
import test.project.bookingapp.model.accommodation.Accommodation;
import test.project.bookingapp.model.accommodation.AccommodationType;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.model.payment.Payment;
import test.project.bookingapp.service.BookingService;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTests {
    @Mock
    private BookingService bookingService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TelegramNotificationService telegramNotificationService;

    private final String botToken = "test-bot-token";
    private final String chatId = "test-chat-id";
    private final String telegramApiUrl = "https://api.telegram.org/bot";

    @BeforeEach
    void setUp() {
        telegramNotificationService = new TelegramNotificationService(bookingService, restTemplate,
                botToken, chatId);
    }

    @Test
    @DisplayName("Send Notification - Success")
    void sendNotification_Success() {
        String message = "Test notification message";
        String expectedUrl = String.format("%s%s/sendMessage?chat_id=%s&text=%s",
                telegramApiUrl, botToken, chatId, message);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("OK");

        telegramNotificationService.sendNotification(message);

        verify(restTemplate, times(1)).getForObject(eq(expectedUrl), eq(String.class));
    }

    @Test
    @DisplayName("Send Notification - Telegram API Failure")
    void sendNotification_ApiFailure() {
        String message = "Test failure notification";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Telegram API Down"));

        assertThrows(RestClientException.class,
                () -> telegramNotificationService.sendNotification(message));

        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("Check Expired Bookings - No Expired Bookings")
    void checkExpiredBookings_NoExpired() {
        when(bookingService.markBookingsAsExpired()).thenReturn(List.of());
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("OK");

        telegramNotificationService.checkExpiredBookings();

        verify(restTemplate, times(1)).getForObject(contains("No expired bookings today!"),
                eq(String.class));
    }

    @Test
    @DisplayName("Check Expired Bookings - With Expired Bookings")
    void checkExpiredBookings_WithExpired() {
        User user = new User();
        user.setFirstName("testUser");

        Accommodation accommodation = new Accommodation();
        accommodation.setType(AccommodationType.HOTEL);

        Booking mockBooking = mock(Booking.class);
        when(mockBooking.getId()).thenReturn(1L);
        when(mockBooking.getCheckOutDate()).thenReturn(LocalDate.parse("2025-03-03"));
        when(mockBooking.getUser()).thenReturn(user);
        when(mockBooking.getAccommodation()).thenReturn(accommodation);

        when(bookingService.markBookingsAsExpired()).thenReturn(List.of(mockBooking));
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("OK");

        telegramNotificationService.checkExpiredBookings();

        verify(restTemplate, times(1)).getForObject(contains("Booking expired!"), eq(String.class));
    }

    @Test
    @DisplayName("Check Expired Bookings - API Failure")
    void checkExpiredBookings_ApiFailure() {
        Booking mockBooking = mock(Booking.class);
        Accommodation mockAccommodation = mock(Accommodation.class);
        User mockUser = mock(User.class);

        when(mockBooking.getAccommodation()).thenReturn(mockAccommodation);
        when(mockAccommodation.getType()).thenReturn(AccommodationType.HOTEL);
        when(mockBooking.getUser()).thenReturn(mockUser);
        when(mockUser.getUsername()).thenReturn("testUser");
        when(bookingService.markBookingsAsExpired()).thenReturn(List.of(mockBooking));
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Telegram API Down"));

        assertThrows(RestClientException.class, () ->
                telegramNotificationService.checkExpiredBookings());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("Send Payment Success Notification - Valid Payment")
    void sendPaymentSuccessNotification_Valid() {
        User user = new User();

        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(1L);
        when(booking.getUser()).thenReturn(user);

        Payment payment = new Payment();
        payment.setSessionId("session_123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setBooking(booking);

        telegramNotificationService.sendPaymentSuccessNotification(payment);

        verify(restTemplate, times(1))
                .getForObject(contains("Payment successful!"), eq(String.class));
    }

    @Test
    @DisplayName("Send Payment Success Notification - Null Payment")
    void sendPaymentSuccessNotification_NullPayment() {
        assertThrows(NullPointerException.class,
                () -> telegramNotificationService.sendPaymentSuccessNotification(null));
    }

    @Test
    @DisplayName("Send Payment Success Notification - API Failure")
    void sendPaymentSuccessNotification_ApiFailure() {
        User user = new User();

        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(1L);
        when(booking.getUser()).thenReturn(user);

        Payment payment = new Payment();
        payment.setSessionId("session_123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setBooking(booking);

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Telegram API Down"));

        assertThrows(RestClientException.class,
                () -> telegramNotificationService.sendPaymentSuccessNotification(payment));

        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }
}
