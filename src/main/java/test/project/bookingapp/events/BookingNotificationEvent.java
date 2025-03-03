package test.project.bookingapp.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingNotificationEvent extends ApplicationEvent {
    private final String message;

    public BookingNotificationEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
}
