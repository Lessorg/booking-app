package test.project.bookingapp.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import test.project.bookingapp.dto.bookingdtos.BookingRequestDto;
import test.project.bookingapp.dto.bookingdtos.BookingResponseDto;
import test.project.bookingapp.dto.bookingdtos.BookingSearchParametersDto;
import test.project.bookingapp.exception.EntityNotFoundException;
import test.project.bookingapp.exception.InvalidStatusException;
import test.project.bookingapp.mapper.BookingMapper;
import test.project.bookingapp.model.User;
import test.project.bookingapp.model.accommodation.Accommodation;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.model.booking.BookingStatus;
import test.project.bookingapp.model.payment.PaymentStatus;
import test.project.bookingapp.repository.PaymentRepository;
import test.project.bookingapp.repository.booking.BookingRepository;
import test.project.bookingapp.repository.booking.specification.BookingSpecificationBuilder;
import test.project.bookingapp.service.impl.JwtAuthenticationService;

class BookingServiceTests {
    private static final Long USER_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    private static final Long ACCOMMODATION_ID = 1L;
    private static final LocalDate CHECK_IN_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate CHECK_OUT_DATE = LocalDate.now().plusDays(5);
    private static final BookingStatus STATUS_PENDING = BookingStatus.PENDING;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;
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
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingService = new BookingService(bookingRepository,
                jwtAuthenticationService, accommodationService,
                bookingMapper, bookingSpecificationBuilder,
                eventPublisher, paymentRepository);
    }

    @Test
    @DisplayName("Should return BookingResponseDto when a booking is successfully created")
    void testCreateBooking_ShouldReturnBookingResponseDto_WhenBookingIsCreated() {
        Accommodation mockAccommodation = new Accommodation();
        mockAccommodation.setId(ACCOMMODATION_ID);
        BookingRequestDto request = new BookingRequestDto(CHECK_IN_DATE,
                CHECK_OUT_DATE, ACCOMMODATION_ID);

        User mockUser = new User();
        mockUser.setId(USER_ID);

        Booking mockBooking = new Booking();
        mockBooking.setId(BOOKING_ID);
        mockBooking.setCheckInDate(request.checkInDate());
        mockBooking.setCheckOutDate(request.checkOutDate());
        mockBooking.setAccommodation(mockAccommodation);
        mockBooking.setUser(mockUser);
        mockBooking.setStatus(STATUS_PENDING);

        when(paymentRepository.existsByBooking_User_IdAndStatus(USER_ID,
                PaymentStatus.PENDING)).thenReturn(false);
        when(accommodationService.findAccommodationById(request.accommodationId()))
                .thenReturn(mockAccommodation);
        when(jwtAuthenticationService.findUserById(USER_ID)).thenReturn(mockUser);
        when(bookingMapper.toBookingEntity(request, mockUser,
                mockAccommodation, BookingStatus.PENDING))
                .thenReturn(mockBooking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
        when(bookingMapper.toBookingResponseDto(mockBooking)).thenReturn(
                new BookingResponseDto(
                        BOOKING_ID,
                        mockBooking.getCheckInDate(),
                        mockBooking.getCheckOutDate(),
                        mockAccommodation.getId(),
                        mockUser.getId(),
                        PaymentStatus.PENDING.name()));

        BookingResponseDto result = bookingService.createBooking(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(BOOKING_ID);
        assertThat(result.checkInDate()).isEqualTo(mockBooking.getCheckInDate());
        assertThat(result.checkOutDate()).isEqualTo(mockBooking.getCheckOutDate());
        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING.name());
        assertThat(result.accommodationId()).isEqualTo(ACCOMMODATION_ID);
        assertThat(result.userId()).isEqualTo(USER_ID);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when a payment is pending")
    void testCreateBooking_ShouldThrowException_WhenPaymentPending() {
        BookingRequestDto request = new BookingRequestDto(CHECK_IN_DATE,
                CHECK_OUT_DATE, ACCOMMODATION_ID);

        when(paymentRepository.existsByBooking_User_IdAndStatus(USER_ID,
                PaymentStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You cannot create a new booking until your"
                        + " previous payment is not completed.");
    }

    @Test
    @DisplayName("Should return BookingResponseDto when booking exists")
    void testGetBookingById_ShouldReturnBookingResponseDto_WhenBookingExists() {
        Booking mockBooking = new Booking();
        mockBooking.setId(BOOKING_ID);

        User mockUser = new User();
        mockUser.setId(USER_ID);
        mockBooking.setUser(mockUser);
        mockBooking.setStatus(STATUS_PENDING);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
        when(jwtAuthenticationService.findUserById(USER_ID)).thenReturn(mockUser);
        when(bookingMapper.toBookingResponseDto(mockBooking)).thenReturn(
                new BookingResponseDto(BOOKING_ID,
                        mockBooking.getCheckInDate(),
                        mockBooking.getCheckOutDate(),
                        1L,
                        1L,
                        "PENDING"));

        BookingResponseDto result = bookingService.getBookingById(USER_ID, BOOKING_ID);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(BOOKING_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when booking does not exist")
    void testGetBookingById_ShouldThrowEntityNotFoundException_WhenBookingDoesNotExist() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(USER_ID, 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Booking not found with id: 999");
    }

    @Test
    @DisplayName("Should return a page of bookings filtered by user and status")
    void testGetBookingsByUserAndStatus_ShouldReturnBookingPage_WhenBookingsExist() {
        BookingSearchParametersDto searchParams = new BookingSearchParametersDto(
                USER_ID, STATUS_PENDING);
        Booking mockBooking = new Booking();
        mockBooking.setId(BOOKING_ID);
        mockBooking.setStatus(STATUS_PENDING);
        Pageable pageable = PageRequest.of(0, 10);

        when(bookingSpecificationBuilder.build(searchParams)).thenReturn(Specification.where(
                null));
        Page<Booking> page = new PageImpl<>(List.of(mockBooking), pageable, 1);
        when(bookingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        BookingResponseDto mockResponse = new BookingResponseDto(BOOKING_ID, null,
                null, null, null, STATUS_PENDING.name());
        when(bookingMapper.toBookingResponseDto(mockBooking)).thenReturn(mockResponse);

        Page<BookingResponseDto> result = bookingService.getBookingsByUserAndStatus(searchParams,
                pageable);

        assertThat(result).isNotNull();
        Assertions.assertFalse(result.getContent().isEmpty());
        assertThat(result.getContent().get(0)).isNotNull();
        assertThat(result.getContent().get(0).id()).isEqualTo(mockBooking.getId());
    }

    @Test
    @DisplayName("Should throw InvalidStatusException when booking is already canceled")
    void testCancelBooking_ShouldThrowInvalidStatusException_WhenBookingAlreadyCanceled() {
        Booking mockBooking = new Booking();
        mockBooking.setId(BOOKING_ID);
        mockBooking.setStatus(BookingStatus.CANCELED);

        User mockUser = new User();
        mockUser.setId(USER_ID);
        mockBooking.setUser(mockUser);

        when(jwtAuthenticationService.findUserById(USER_ID)).thenReturn(mockUser);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(USER_ID, BOOKING_ID))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessage("Booking with id: 1 has already been canceled.");
    }

    @Test
    @DisplayName("Should throw InvalidStatusException when booking "
            + "is canceled and update is attempted")
    void testUpdateBooking_ShouldThrowInvalidStatusException_WhenBookingIsCanceled() {
        Booking mockBooking = new Booking();
        mockBooking.setId(BOOKING_ID);
        mockBooking.setStatus(BookingStatus.CANCELED);
        BookingRequestDto request = new BookingRequestDto(CHECK_IN_DATE, CHECK_OUT_DATE,
                ACCOMMODATION_ID);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

        assertThatThrownBy(() -> bookingService.updateBooking(BOOKING_ID, request))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessage("Cannot update a canceled booking with id: 1");
    }
}
