package test.project.bookingapp.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jmx.export.notification.UnableToSendNotificationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.project.bookingapp.dto.bookingdtos.BookingRequestDto;
import test.project.bookingapp.dto.bookingdtos.BookingResponseDto;
import test.project.bookingapp.dto.bookingdtos.BookingSearchParametersDto;
import test.project.bookingapp.events.BookingNotificationEvent;
import test.project.bookingapp.exception.BookingConflictException;
import test.project.bookingapp.exception.EntityNotFoundException;
import test.project.bookingapp.exception.InvalidBookingStatusException;
import test.project.bookingapp.mapper.BookingMapper;
import test.project.bookingapp.model.User;
import test.project.bookingapp.model.accommodation.Accommodation;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.model.booking.BookingStatus;
import test.project.bookingapp.model.role.Role;
import test.project.bookingapp.model.role.RoleName;
import test.project.bookingapp.repository.booking.BookingRepository;
import test.project.bookingapp.repository.booking.specification.BookingSpecificationBuilder;
import test.project.bookingapp.service.impl.JwtAuthenticationService;

@RequiredArgsConstructor
@Transactional
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final AccommodationService accommodationService;
    private final BookingMapper bookingMapper;
    private final BookingSpecificationBuilder bookingSpecificationBuilder;
    private final ApplicationEventPublisher eventPublisher;

    public BookingResponseDto createBooking(Long userId, BookingRequestDto request) {
        Accommodation accommodation =
                accommodationService.findAccommodationById(request.accommodationId());
        validateAvailability(accommodation.getId(), request.checkInDate(), request.checkOutDate());

        User user = jwtAuthenticationService.findUserById(userId);
        Booking booking = bookingMapper.toBookingEntity(request, user, accommodation,
                BookingStatus.PENDING);
        booking = bookingRepository.save(booking);

        try {
            eventPublisher.publishEvent(
                    new BookingNotificationEvent(this, "New booking created: " + booking.getId()));
        } catch (Exception e) {
            throw new UnableToSendNotificationException(
                    "Failed to send notification for booking ID: " + booking.getId(), e);
        }

        return bookingMapper.toBookingResponseDto(booking);
    }

    public BookingResponseDto getBookingById(Long userId, Long id) {
        Booking booking = findBookingById(id);
        validateAccess(userId, booking);
        return bookingMapper.toBookingResponseDto(booking);
    }

    public Page<BookingResponseDto> getBookingsByUserAndStatus(
            BookingSearchParametersDto searchParams,
            Pageable pageable) {
        Specification<Booking> spec = bookingSpecificationBuilder.build(searchParams);
        return bookingRepository.findAll(spec, pageable)
                .map(bookingMapper::toBookingResponseDto);
    }

    public Page<BookingResponseDto> getMyBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable)
                .map(bookingMapper::toBookingResponseDto);
    }

    public BookingResponseDto updateBooking(Long id, BookingRequestDto request) {
        Booking booking = findBookingById(id);

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new InvalidBookingStatusException(
                    "Cannot update a canceled booking with id: " + id);
        }
        validateAvailability(request.accommodationId(), request.checkInDate(),
                request.checkOutDate());

        bookingMapper.updateBookingEntity(booking, request);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponseDto(updatedBooking);
    }

    public void cancelBooking(Long userId, Long id) {
        Booking booking = findBookingById(id);

        validateAccess(userId, booking);
        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new InvalidBookingStatusException("Booking with id: "
                    + booking.getId() + " has already been canceled.");
        }

        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);

        try {
            eventPublisher.publishEvent(
                    new BookingNotificationEvent(this, "Booking canceled: "
                            + booking.getId()));
        } catch (Exception e) {
            throw new UnableToSendNotificationException(
                    "Failed to send notification for booking ID: " + booking.getId(), e);
        }
    }

    public List<Booking> markBookingsAsExpired() {
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.minusDays(1);
        List<Booking> expiredBookings = findExpiredBookings(thresholdDate);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
        }
        return expiredBookings;
    }

    private List<Booking> findExpiredBookings(LocalDate thresholdDate) {
        return bookingRepository.findByCheckOutDateBeforeAndStatusNot(thresholdDate,
                BookingStatus.CANCELED);
    }

    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Booking not found with id: " + id));
    }

    private void validateAvailability(Long accommodationId, LocalDate checkIn,
                                      LocalDate checkOut) {
        List<Booking> existingBookings =
                bookingRepository.findOverlappingBookings(accommodationId, checkIn, checkOut);
        if (!existingBookings.isEmpty()) {

            throw new BookingConflictException(String.format(
                    "Accommodation %d is already booked from %s to %s.",
                    accommodationId, checkIn, checkOut
            ));
        }
    }

    private void validateAccess(Long userId, Booking booking) {
        User currentUser = jwtAuthenticationService.findUserById(userId);
        boolean isAdmin = currentUser.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName -> roleName == RoleName.ROLE_ADMIN);
        boolean isOwner = booking.getUser().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new SecurityException(
                    "Access denied: You can only interact with your own bookings.");
        }
    }
}
