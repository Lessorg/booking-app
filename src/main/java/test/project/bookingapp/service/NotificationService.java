package test.project.bookingapp.service;

import test.project.bookingapp.model.payment.Payment;

public interface NotificationService {
    void sendNotification(String message);

    void sendPaymentSuccessNotification(Payment payment);

    void checkExpiredBookings();
}
