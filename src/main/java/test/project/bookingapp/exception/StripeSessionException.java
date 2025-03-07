package test.project.bookingapp.exception;

import com.stripe.exception.StripeException;

public class StripeSessionException extends RuntimeException {
    public StripeSessionException(String message, StripeException e) {
        super(message);
    }
}
