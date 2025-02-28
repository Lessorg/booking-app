package test.project.bookingapp.repository.booking;

import org.springframework.data.jpa.domain.Specification;
import test.project.bookingapp.dto.bookingdtos.BookingSearchParametersDto;

public interface SpecificationBuilder<T> {
    Specification<T> build(BookingSearchParametersDto searchParameters);
}
