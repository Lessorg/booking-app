package test.project.bookingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import test.project.bookingapp.dto.accommodationdtos.AccommodationRequestDto;
import test.project.bookingapp.dto.accommodationdtos.AccommodationResponseDto;
import test.project.bookingapp.service.AccommodationService;

@RequiredArgsConstructor
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/accommodations")
@Tag(name = "Accommodation", description = "Endpoints for managing accommodations")
public class AccommodationController {
    private final AccommodationService accommodationService;

    @Operation(summary = "Add new accommodation",
            description = "Allows an admin to add a new accommodation")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public AccommodationResponseDto createAccommodation(
            @Valid @RequestBody AccommodationRequestDto request) {
        return accommodationService.createAccommodation(request);
    }

    @Operation(summary = "Get all accommodations",
            description = "Retrieves a list of all available accommodations")
    @GetMapping
    public Page<AccommodationResponseDto> getAllAccommodations(
            @ParameterObject @PageableDefault Pageable pageable) {
        return accommodationService.getAllAccommodations(pageable);
    }

    @Operation(summary = "Get accommodation by ID",
            description = "Retrieves details of a specific accommodation by ID")
    @GetMapping("/{id}")
    public AccommodationResponseDto getAccommodationById(@PathVariable Long id) {
        return accommodationService.getAccommodationById(id);
    }

    @Operation(summary = "Update accommodation details",
            description = "Allows an admin to update an accommodation's details")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public AccommodationResponseDto putUpdateAccommodation(
            @PathVariable Long id,
            @Valid @RequestBody AccommodationRequestDto request) {
        return accommodationService.updateAccommodation(id, request);
    }

    @Operation(summary = "Update accommodation details",
            description = "Allows an admin to update an accommodation's details")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public AccommodationResponseDto patchUpdateAccommodation(
            @PathVariable Long id,
            @Valid @RequestBody AccommodationRequestDto request) {
        return accommodationService.updateAccommodation(id, request);
    }

    @Operation(summary = "Delete accommodation",
            description = "Allows an admin to remove an accommodation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteAccommodation(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
    }
}
