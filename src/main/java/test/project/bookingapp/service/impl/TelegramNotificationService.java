package test.project.bookingapp.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import test.project.bookingapp.events.BookingNotificationEvent;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.service.BookingService;
import test.project.bookingapp.service.NotificationService;

@Service
public class TelegramNotificationService implements NotificationService {
    private final BookingService bookingService;
    private final RestTemplate restTemplate;
    private final String telegramApiUrl = "https://api.telegram.org/bot";
    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.chat.id}")
    private String chatId;

    public TelegramNotificationService(BookingService bookingService, RestTemplate restTemplate,
                                       @Value("${telegram.bot.token}") String botToken,
                                       @Value("${telegram.chat.id}") String chatId) {
        this.bookingService = bookingService;
        this.restTemplate = restTemplate;
        this.botToken = botToken;
        this.chatId = chatId;
    }

    @EventListener
    public void handleBookingNotification(BookingNotificationEvent event) {
        sendNotification(event.getMessage());
    }

    @Override
    public void sendNotification(String message) {
        String url = String.format("%s%s/sendMessage?chat_id=%s&text=%s",
                telegramApiUrl, botToken, chatId, message);
        restTemplate.getForObject(url, String.class);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiredBookings() {
        List<Booking> expiredBookings = bookingService.markBookingsAsExpired();

        if (expiredBookings.isEmpty()) {
            sendNotification("No expired bookings today!");
            return;
        }

        for (Booking booking : expiredBookings) {
            String message = String.format(
                    "Booking expired! 📅\n"
                            + "🏠 Accommodation: %s\n"
                            + "👤 Guest: %s\n"
                            + "📅 Check-out: %s\n"
                            + "🔗 Booking ID: %s",
                    booking.getAccommodation().getType(),
                    booking.getUser().getUsername(),
                    booking.getCheckOutDate(),
                    booking.getId()
            );
            sendNotification(message);
        }
    }
}
