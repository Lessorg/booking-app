package test.project.bookingapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingAppApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("TELEGRAM_BOT_TOKEN", dotenv.get("TELEGRAM_BOT_TOKEN"));
        System.setProperty("TELEGRAM_CHAT_ID", dotenv.get("TELEGRAM_CHAT_ID"));
        System.setProperty("STRIPE_API_KEY", dotenv.get("STRIPE_API_KEY"));
        System.setProperty("APP_BASE_URL", dotenv.get("APP_BASE_URL"));

        SpringApplication.run(BookingAppApplication.class, args);
    }
}
