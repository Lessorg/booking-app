package test.project.bookingapp.repository.booking.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import test.project.bookingapp.model.booking.Booking;
import test.project.bookingapp.repository.booking.SpecificationProvider;

@Component
public class UserIdSpecificationProvider implements SpecificationProvider<Booking> {
    private static final String USER_COLUMN_NAME = "user";

    @Override
    public String getKey() {
        return USER_COLUMN_NAME;
    }

    @Override
    public Specification<Booking> getSpecification(String[] params) {
        return (root, query, cb) -> root.get(USER_COLUMN_NAME).get("id")
                .in((Object[]) params);
    }
}
