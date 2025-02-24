package test.project.bookingapp.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import test.project.bookingapp.config.MapperConfig;
import test.project.bookingapp.dto.UserRegistrationRequestDto;
import test.project.bookingapp.dto.UserResponseDto;
import test.project.bookingapp.dto.UserRoleUpdateResponseDto;
import test.project.bookingapp.model.Role;
import test.project.bookingapp.model.RoleName;
import test.project.bookingapp.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toUser(UserRegistrationRequestDto userDto);

    UserResponseDto toUserResponseDto(User updatedUser);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "mapRolesToRoleNames")
    UserRoleUpdateResponseDto toUserRoleUpdateResponseDto(User updatedUser);

    @Named("mapRolesToRoleNames")
    default Set<RoleName> mapRolesToRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
