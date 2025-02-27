package test.project.bookingapp.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.model.booking.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.accommodation.id = :accommodationId "
            + "AND (:checkIn BETWEEN b.checkInDate AND b.checkOutDate "
            + "OR :checkOut BETWEEN b.checkInDate AND b.checkOutDate "
            + "OR (b.checkInDate BETWEEN :checkIn AND :checkOut))")
    List<Booking> findOverlappingBookings(@Param("accommodationId") Long accommodationId,
                                          @Param("checkIn") LocalDate checkIn,
                                          @Param("checkOut") LocalDate checkOut);

    Page<Booking> findByStatus(BookingStatus bookingStatus, Pageable pageable);
}
