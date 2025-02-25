package test.project.bookingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.project.bookingapp.model.accommodation.Accommodation;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
}
