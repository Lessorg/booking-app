package test.project.bookingapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import test.project.bookingapp.dto.accommodationdtos.AccommodationRequestDto;
import test.project.bookingapp.dto.accommodationdtos.AccommodationResponseDto;
import test.project.bookingapp.exception.EntityNotFoundException;
import test.project.bookingapp.mapper.AccommodationMapper;
import test.project.bookingapp.model.accommodation.Accommodation;
import test.project.bookingapp.repository.AccommodationRepository;

@RequiredArgsConstructor
@Service
public class AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;

    public AccommodationResponseDto createAccommodation(AccommodationRequestDto request) {
        Accommodation accommodation = accommodationMapper.toEntity(request);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        return accommodationMapper.toDto(savedAccommodation);
    }

    public Page<AccommodationResponseDto> getAllAccommodations(Pageable pageable) {
        return accommodationRepository.findAll(pageable)
                .map(accommodationMapper::toDto);
    }

    public AccommodationResponseDto getAccommodationById(Long id) {
        Accommodation accommodation = findAccommodationById(id);
        return accommodationMapper.toDto(accommodation);
    }

    public AccommodationResponseDto updateAccommodation(Long id, AccommodationRequestDto request) {
        Accommodation accommodation = findAccommodationById(id);
        accommodationMapper.updateEntity(accommodation, request);
        Accommodation updatedAccommodation = accommodationRepository.save(accommodation);
        return accommodationMapper.toDto(updatedAccommodation);
    }

    public void deleteAccommodation(Long id) {
        Accommodation accommodation = findAccommodationById(id);
        accommodationRepository.delete(accommodation);
    }

    public Accommodation findAccommodationById(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with id: " + id));
    }
}
