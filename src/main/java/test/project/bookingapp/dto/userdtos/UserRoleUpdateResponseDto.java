package test.project.bookingapp.dto.userdtos;

import java.util.Set;
import test.project.bookingapp.model.role.RoleName;

public record UserRoleUpdateResponseDto(Long userId, Set<RoleName> roles) {}
