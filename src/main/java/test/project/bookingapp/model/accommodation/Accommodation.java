package test.project.bookingapp.model.accommodation;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "accommodations")
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accommodation_seq")
    @SequenceGenerator(name = "accommodation_seq",
            sequenceName = "accommodations_id_seq", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccommodationType type;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String size;

    @ElementCollection
    @CollectionTable(name = "accommodation_amenities",
            joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "amenity")
    private List<String> amenities;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(nullable = false)
    private Integer availability;
}
