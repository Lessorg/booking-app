package test.project.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jmx.export.notification.UnableToSendNotificationException;
import test.project.bookingapp.dto.accommodationdtos.AccommodationRequestDto;
import test.project.bookingapp.dto.accommodationdtos.AccommodationResponseDto;
import test.project.bookingapp.exception.EntityNotFoundException;
import test.project.bookingapp.mapper.AccommodationMapper;
import test.project.bookingapp.model.accommodation.Accommodation;
import test.project.bookingapp.model.accommodation.AccommodationType;
import test.project.bookingapp.repository.AccommodationRepository;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTests {
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AccommodationMapper accommodationMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private AccommodationService accommodationService;

    private Accommodation accommodation;
    private AccommodationRequestDto accommodationRequestDto;
    private AccommodationResponseDto accommodationResponseDto;

    @BeforeEach
    void setUp() {
        accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(AccommodationType.HOUSE);
        accommodation.setLocation("123 Main St");
        accommodation.setSize("3 Bedroom");
        accommodation.setDailyRate(new BigDecimal("150.00"));
        accommodation.setAvailability(5);

        accommodationRequestDto = new AccommodationRequestDto(
                AccommodationType.HOUSE,
                "123 Main St",
                "3 Bedroom",
                Collections.singletonList("WiFi, Pool"),
                new BigDecimal("150.00"),
                5);
        accommodationResponseDto = new AccommodationResponseDto(
                1L,
                AccommodationType.HOUSE,
                "123 Main St",
                "3 Bedroom",
                Collections.singletonList("WiFi, Pool"),
                new BigDecimal("150.00"),
                5);
    }

    @Test
    @DisplayName("Create Accommodation")
    void createAccommodation() {
        when(accommodationMapper.toEntity(accommodationRequestDto)).thenReturn(accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(accommodationResponseDto);

        AccommodationResponseDto response =
                accommodationService.createAccommodation(accommodationRequestDto);

        assertNotNull(response);
        assertEquals(accommodationResponseDto.id(), response.id());
        assertEquals(accommodationResponseDto.type(), response.type());
        verify(accommodationRepository).save(any(Accommodation.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("Get All Accommodations")
    void getAllAccommodations() {
        Page<Accommodation> accommodations = new PageImpl<>(List.of(accommodation));
        when(accommodationRepository.findAll(any(Pageable.class))).thenReturn(accommodations);
        when(accommodationMapper.toDto(accommodation)).thenReturn(accommodationResponseDto);

        Page<AccommodationResponseDto> response =
                accommodationService.getAllAccommodations(Pageable.unpaged());

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(accommodationResponseDto.id(), response.getContent().get(0).id());
    }

    @Test
    @DisplayName("Get Accommodation By ID")
    void getAccommodationById() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(accommodationResponseDto);

        AccommodationResponseDto response = accommodationService.getAccommodationById(1L);

        assertNotNull(response);
        assertEquals(accommodationResponseDto.id(), response.id());
        verify(accommodationRepository).findById(1L);
    }

    @Test
    @DisplayName("Get Accommodation By ID - Entity Not Found")
    void getAccommodationById_NotFound() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class,
                        () -> accommodationService.getAccommodationById(1L));

        assertEquals("Accommodation not found with id: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Update Accommodation")
    void updateAccommodation() {
        AccommodationRequestDto updateRequest =
                new AccommodationRequestDto(
                        AccommodationType.valueOf("APARTMENT"),
                        "456 Elm St",
                        "2 Bedroom",
                        Collections.singletonList("WiFi, Parking"),
                        new BigDecimal("120.00"),
                        3);

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(accommodationResponseDto);

        AccommodationResponseDto response =
                accommodationService.updateAccommodation(1L, updateRequest);

        assertNotNull(response);
        assertEquals(accommodation.getId(), response.id());
        assertEquals(accommodation.getType(), response.type());
        assertEquals(accommodation.getLocation(), response.location());
        verify(accommodationRepository).save(accommodation);
    }

    @Test
    @DisplayName("Delete Accommodation")
    void deleteAccommodation() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));

        accommodationService.deleteAccommodation(1L);

        verify(accommodationRepository).delete(accommodation);
    }

    @Test
    @DisplayName("Delete Accommodation - Entity Not Found")
    void deleteAccommodation_NotFound() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> accommodationService.deleteAccommodation(1L));

        assertEquals("Accommodation not found with id: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Create Accommodation - Unable to Send Notification Exception")
    void createAccommodation_NotificationFailure() {
        when(accommodationMapper.toEntity(accommodationRequestDto))
                .thenReturn(accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);

        doThrow(new RuntimeException("Notification service down"))
                .when(eventPublisher).publishEvent(any());

        UnableToSendNotificationException exception =
                assertThrows(UnableToSendNotificationException.class,
                        () -> accommodationService.createAccommodation(accommodationRequestDto));

        assertEquals("Failed to send notification for accommodation ID: 1",
                exception.getMessage());
    }

    @Test
    @DisplayName("Update Accommodation - Entity Not Found")
    void updateAccommodation_NotFound() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> accommodationService.updateAccommodation(1L, accommodationRequestDto));

        assertEquals("Accommodation not found with id: 1", exception.getMessage());
    }
}

