package test.project.bookingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import test.project.bookingapp.config.MapperConfig;
import test.project.bookingapp.dto.accommodationdtos.AccommodationRequestDto;
import test.project.bookingapp.dto.accommodationdtos.AccommodationResponseDto;
import test.project.bookingapp.model.accommodation.Accommodation;

@Mapper(config = MapperConfig.class)
public interface AccommodationMapper {

    @Mapping(target = "id", ignore = true)
    Accommodation toEntity(AccommodationRequestDto request);

    AccommodationResponseDto toDto(Accommodation accommodation);

    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Accommodation accommodation, AccommodationRequestDto request);
}
