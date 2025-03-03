package test.project.bookingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.project.bookingapp.model.User;
import test.project.bookingapp.model.accommodation.Accommodation;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.model.booking.BookingStatus;
import test.project.bookingapp.repository.booking.BookingRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/db/add-test-bookings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookingRepositoryTests {
    private static final Long TEST_USER_ID = 20L;
    private static final Long TEST_ACCOMMODATION_ID = 10L;
    private static final Long EXISTING_BOOKING_ID = 100L;
    private static final Long NON_EXISTENT_BOOKING_ID = 999L;
    private static final BookingStatus TEST_STATUS_PENDING = BookingStatus.PENDING;
    private static final PageRequest PAGE_REQUEST = PageRequest.of(0, 10);

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private Accommodation testAccommodation;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(TEST_USER_ID);

        testAccommodation = new Accommodation();
        testAccommodation.setId(TEST_ACCOMMODATION_ID);
    }

    @Test
    @DisplayName("Find bookings by user ID")
    void shouldFindBookingsByUserId() {
        Page<Booking> bookings = bookingRepository.findByUserId(TEST_USER_ID, PAGE_REQUEST);
        assertThat(bookings).isNotEmpty().allMatch(
                booking -> booking.getUser().getId().equals(TEST_USER_ID));
    }

    @Test
    @DisplayName("Find bookings by user ID and status")
    void shouldFindBookingsByUserIdAndStatus() {
        Page<Booking> bookings = bookingRepository.findByUserIdAndStatus(TEST_USER_ID,
                TEST_STATUS_PENDING, PAGE_REQUEST);
        assertThat(bookings).isNotEmpty().allMatch(
                booking -> booking.getStatus() == TEST_STATUS_PENDING);
    }

    @Test
    @DisplayName("Find overlapping bookings for a given accommodation and date range")
    void shouldFindOverlappingBookings() {
        LocalDate checkIn = LocalDate.of(2025, 6, 1);
        LocalDate checkOut = LocalDate.of(2025, 6, 7);

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                TEST_ACCOMMODATION_ID, checkIn, checkOut);
        assertThat(overlappingBookings).isNotEmpty().allMatch(
                booking -> booking.getAccommodation().getId().equals(TEST_ACCOMMODATION_ID));
    }

    @Test
    @DisplayName("Find booking by ID")
    void shouldFindBookingById() {
        Optional<Booking> booking = bookingRepository.findById(EXISTING_BOOKING_ID);
        assertThat(booking).isPresent().map(Booking::getId).contains(EXISTING_BOOKING_ID);
    }

    @Test
    @DisplayName("Return empty when searching for a non-existent booking ID")
    void shouldNotFindNonExistentBookingById() {
        Optional<Booking> booking = bookingRepository.findById(NON_EXISTENT_BOOKING_ID);
        assertThat(booking).isEmpty();
    }

    @Test
    @DisplayName("Save a new booking")
    void shouldSaveNewBooking() {
        Booking booking = new Booking();
        booking.setCheckInDate(LocalDate.of(2025, 8, 1));
        booking.setCheckOutDate(LocalDate.of(2025, 8, 5));
        booking.setAccommodation(testAccommodation);
        booking.setUser(testUser);
        booking.setStatus(TEST_STATUS_PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        assertThat(savedBooking.getId()).isNotNull();
    }

    @Test
    @DisplayName("Delete a booking by ID")
    void shouldDeleteBooking() {
        bookingRepository.deleteById(EXISTING_BOOKING_ID);
        assertThat(bookingRepository.findById(EXISTING_BOOKING_ID)).isEmpty();
    }

    @Test
    @DisplayName("Find no bookings for a non-existent user")
    void shouldFindNoBookingsForNonExistentUser() {
        Page<Booking> bookings =
                bookingRepository.findByUserId(NON_EXISTENT_BOOKING_ID, PAGE_REQUEST);
        assertThat(bookings).isEmpty();
    }

    @Test
    @DisplayName("Find expired bookings that are not canceled")
    void shouldFindExpiredBookings() {
        LocalDate thresholdDate = LocalDate.now().minusDays(1);
        List<Booking> expiredBookings = bookingRepository.findByCheckOutDateBeforeAndStatusNot(
                thresholdDate, BookingStatus.CANCELED);

        assertThat(expiredBookings).isNotEmpty().allMatch(booking ->
                booking.getCheckOutDate().isBefore(thresholdDate)
                        && booking.getStatus() != BookingStatus.CANCELED
        );
    }
}
