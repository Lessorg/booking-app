package test.project.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import test.project.bookingapp.dto.bookingdtos.BookingRequestDto;
import test.project.bookingapp.dto.bookingdtos.BookingResponseDto;
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

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private JwtAuthenticationService jwtAuthenticationService;
    @Mock
    private AccommodationService accommodationService;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingSpecificationBuilder bookingSpecificationBuilder;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;
    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
    private User user;
    private Accommodation accommodation;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setRoles(Set.of(new Role(2L, RoleName.ROLE_CUSTOMER)));

        accommodation = new Accommodation();
        accommodation.setId(1L);

        booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setAccommodation(accommodation);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(5));
        booking.setStatus(BookingStatus.PENDING);

        bookingRequestDto = new BookingRequestDto(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                1L
        );

        bookingResponseDto = new BookingResponseDto(
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                1L,
                1L,
                BookingStatus.PENDING.name()
        );
    }

    @Test
    @DisplayName("Create Booking")
    void createBooking() {
        when(accommodationService.findAccommodationById(any(Long.class))).thenReturn(accommodation);
        when(jwtAuthenticationService.findUserById(any(Long.class))).thenReturn(user);
        when(bookingMapper.toBookingEntity(any(), any(), any(), any())).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDto);

        BookingResponseDto response = bookingService.createBooking(1L, bookingRequestDto);

        assertNotNull(response);
        assertEquals(1L, response.id());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Cancel Booking")
    void cancelBooking() {
        when(jwtAuthenticationService.findUserById(1L)).thenReturn(user);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L, 1L);

        assertEquals(BookingStatus.CANCELED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    @DisplayName("Find Booking By ID - Not Found")
    void findBookingById_NotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBookingById(1L, 1L));
        assertEquals("Booking not found with id: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Update Booking - Canceled Booking")
    void updateBooking_CanceledBooking() {
        booking.setStatus(BookingStatus.CANCELED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        InvalidBookingStatusException exception = assertThrows(InvalidBookingStatusException.class,
                () -> bookingService.updateBooking(1L, bookingRequestDto));
        assertEquals("Cannot update a canceled booking with id: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Mark Bookings As Expired")
    void markBookingsAsExpired() {
        when(bookingRepository.findByCheckOutDateBeforeAndStatusNot(any(), any()))
                .thenReturn(List.of(booking));

        List<Booking> expiredBookings = bookingService.markBookingsAsExpired();

        assertFalse(expiredBookings.isEmpty());
        assertEquals(BookingStatus.EXPIRED, expiredBookings.get(0).getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }
}
