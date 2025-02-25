package test.project.bookingapp.dto.userdtos;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import test.project.bookingapp.model.role.RoleName;

public record UserRoleUpdateRequestDto(
        @NotEmpty Set<RoleName> roles
) {}
