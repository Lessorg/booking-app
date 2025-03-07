package test.project.bookingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.project.bookingapp.model.payment.Payment;
import test.project.bookingapp.model.payment.PaymentStatus;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/db/add-test-payments.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PaymentRepositoryTests {
    private static final String EXISTING_SESSION_ID = "test-session-1";
    private static final String NON_EXISTENT_SESSION_ID = "invalid-session";
    private static final Long EXISTING_BOOKING_ID = 1L;
    private static final Long NON_EXISTENT_BOOKING_ID = 999L;
    private static final Long EXISTING_USER_ID = 1L;
    private static final Long NON_EXISTENT_USER_ID = 999L;
    private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 10);

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Should find payment by session ID")
    void shouldFindPaymentBySessionId() {
        Optional<Payment> payment = paymentRepository.findBySessionId(EXISTING_SESSION_ID);
        assertThat(payment).isPresent();
        assertThat(payment.get().getSessionId()).isEqualTo(EXISTING_SESSION_ID);
    }

    @Test
    @DisplayName("Should return empty for non-existent session ID")
    void shouldReturnEmptyForNonExistentSessionId() {
        Optional<Payment> payment = paymentRepository.findBySessionId(NON_EXISTENT_SESSION_ID);
        assertThat(payment).isNotPresent();
    }

    @Test
    @DisplayName("Should find payment by booking ID")
    void shouldFindPaymentByBookingId() {
        Optional<Payment> payment = paymentRepository.findByBookingId(EXISTING_BOOKING_ID);
        assertThat(payment).isPresent();
        assertThat(payment.get().getBooking().getId()).isEqualTo(EXISTING_BOOKING_ID);
    }

    @Test
    @DisplayName("Should return empty for non-existent booking ID")
    void shouldReturnEmptyForNonExistentBookingId() {
        Optional<Payment> payment = paymentRepository.findByBookingId(NON_EXISTENT_BOOKING_ID);
        assertThat(payment).isNotPresent();
    }

    @Test
    @DisplayName("Should check if user has a payment with given status")
    void shouldCheckIfUserHasPaymentWithStatus() {
        boolean exists = paymentRepository.existsByBooking_User_IdAndStatus(EXISTING_USER_ID,
                PaymentStatus.PENDING);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false if no payment exists for user with given status")
    void shouldReturnFalseIfNoPaymentExistsForUserWithGivenStatus() {
        boolean exists = paymentRepository.existsByBooking_User_IdAndStatus(EXISTING_USER_ID,
                PaymentStatus.EXPIRED);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find payments by user ID with pagination")
    void shouldFindPaymentsByUserIdWithPagination() {
        Page<Payment> payments = paymentRepository.findByBooking_User_Id(EXISTING_USER_ID,
                DEFAULT_PAGE);
        assertThat(payments).isNotEmpty();
        assertThat(payments.getContent()).allMatch(
                payment -> payment.getBooking().getUser().getId().equals(EXISTING_USER_ID));
    }

    @Test
    @DisplayName("Should return empty page if no payments exist for user")
    void shouldReturnEmptyPageIfNoPaymentsExistForUser() {
        Page<Payment> payments = paymentRepository.findByBooking_User_Id(NON_EXISTENT_USER_ID,
                DEFAULT_PAGE);
        assertThat(payments).isEmpty();
    }
}
