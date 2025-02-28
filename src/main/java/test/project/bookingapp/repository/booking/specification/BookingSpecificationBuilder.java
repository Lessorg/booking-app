package test.project.bookingapp.repository.booking.specification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import test.project.bookingapp.dto.bookingdtos.BookingSearchParametersDto;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.repository.booking.SpecificationBuilder;
import test.project.bookingapp.repository.booking.SpecificationProviderManager;

@RequiredArgsConstructor
@Component
public class BookingSpecificationBuilder implements SpecificationBuilder<Booking> {
    private static final String STATUS_COLUMN_NAME = "status";
    private static final String USER_COLUMN_NAME = "user";

    private final SpecificationProviderManager<Booking> bookingSpecificationProviderManager;

    @Override
    public Specification<Booking> build(BookingSearchParametersDto searchParameters) {
        Specification<Booking> spec = Specification.where(null);

        if (searchParameters.status() != null) {
            spec = spec.and(bookingSpecificationProviderManager
                    .getSpecificationProvider(STATUS_COLUMN_NAME)
                    .getSpecification(new String[]{searchParameters.status().name()}));
        }

        if (searchParameters.userId() != null) {
            spec = spec.and(bookingSpecificationProviderManager
                    .getSpecificationProvider(USER_COLUMN_NAME)
                    .getSpecification(new String[]{String.valueOf(searchParameters.userId())}));
        }
        return spec;
    }
}
