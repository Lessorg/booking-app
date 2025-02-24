package test.project.bookingapp.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import test.project.bookingapp.model.RoleName;

public record UserRoleUpdateRequestDto(
        @NotEmpty Set<RoleName> roles
) {}
