package test.project.bookingapp.dto.accommodationdtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import test.project.bookingapp.model.accommodation.AccommodationType;

public record AccommodationRequestDto(
        @NotNull AccommodationType type,
        @NotBlank String location,
        @NotBlank String size,
        @NotNull List<@NotBlank String> amenities,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal dailyRate,
        @NotNull @Min(value = 0) Integer availability
) {
}
