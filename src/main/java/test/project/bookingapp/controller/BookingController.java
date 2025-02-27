package test.project.bookingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import test.project.bookingapp.dto.bookingdtos.BookingRequestDto;
import test.project.bookingapp.dto.bookingdtos.BookingResponseDto;
import test.project.bookingapp.model.User;
import test.project.bookingapp.service.BookingService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
@Tag(name = "Booking", description = "Endpoints for managing bookings")
public class BookingController {
    private final BookingService bookingService;

    @Operation(summary = "Create a new booking",
            description = "Allows users to create a new accommodation booking")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public BookingResponseDto createBooking(
            Authentication authentication,
            @Valid @RequestBody BookingRequestDto request) {
        return bookingService.createBooking(getUserId(authentication), request);
    }

    @Operation(summary = "Get bookings by user and status",
            description = "Retrieves bookings based on user ID and status (Available for managers)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<BookingResponseDto> getBookingsByUserAndStatus(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault Pageable pageable) {
        return bookingService.getBookingsByUserAndStatus(userId, status, pageable);
    }

    @Operation(summary = "Get user bookings",
            description = "Retrieves bookings for the currently authenticated user")
    @GetMapping("/my")
    public Page<BookingResponseDto> getMyBookings(
            Authentication authentication,
            @ParameterObject @PageableDefault Pageable pageable) {
        return bookingService.getMyBookings(getUserId(authentication), pageable);
    }

    @Operation(summary = "Get booking by ID",
            description = "Provides details of a specific booking by ID")
    @GetMapping("/{id}")
    public BookingResponseDto getBookingById(
            Authentication authentication,
            @PathVariable Long id) {
        return bookingService.getBookingById(getUserId(authentication),id);
    }

    @Operation(summary = "Update booking details",
            description = "Allows users to update their booking details")
    @PutMapping("/{id}")
    public BookingResponseDto putUpdateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequestDto request) {
        return bookingService.updateBooking(id, request);
    }

    @Operation(summary = "Update booking details",
            description = "Allows users to update their booking details")
    @PatchMapping("/{id}")
    public BookingResponseDto patchUpdateBooking(
            @PathVariable Long id,
            @RequestBody BookingRequestDto request) {
        return bookingService.updateBooking(id, request);
    }

    @Operation(summary = "Cancel booking",
            description = "Allows users to cancel their booking")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void cancelBooking(
            Authentication authentication,
            @PathVariable Long id) {
        bookingService.cancelBooking(getUserId(authentication), id);
    }

    private Long getUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}

