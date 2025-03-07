package test.project.bookingapp.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import test.project.bookingapp.model.payment.Payment;
import test.project.bookingapp.model.payment.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByBooking_User_Id(Long bookingUserId, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);

    boolean existsByBooking_User_IdAndStatus(Long userId, PaymentStatus status);

    Optional<Payment> findByBookingId(Long id);
}
