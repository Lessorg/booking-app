package test.project.bookingapp.service;

public interface NotificationService {
    void sendNotification(String message);

    void checkExpiredBookings();
}
