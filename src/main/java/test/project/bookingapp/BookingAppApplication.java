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

        SpringApplication.run(BookingAppApplication.class, args);
    }
}
