package test.project.bookingapp.dto;

import java.util.Set;
import test.project.bookingapp.model.RoleName;

public record UserRoleUpdateResponseDto(Long userId, Set<RoleName> roles) {}
