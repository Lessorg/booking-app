package test.project.bookingapp.service;

import test.project.bookingapp.dto.userdtos.UserLoginRequestDto;
import test.project.bookingapp.dto.userdtos.UserLoginResponseDto;
import test.project.bookingapp.dto.userdtos.UserRegistrationRequestDto;
import test.project.bookingapp.dto.userdtos.UserResponseDto;
import test.project.bookingapp.dto.userdtos.UserRoleUpdateRequestDto;
import test.project.bookingapp.dto.userdtos.UserRoleUpdateResponseDto;
import test.project.bookingapp.dto.userdtos.UserUpdateRequestDto;
import test.project.bookingapp.exception.RegistrationException;
import test.project.bookingapp.model.User;

public interface AuthenticationService {
    UserLoginResponseDto authenticate(UserLoginRequestDto request);

    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserRoleUpdateResponseDto updateUserRole(Long id, UserRoleUpdateRequestDto request);

    UserResponseDto getUserProfile(Long userId);

    UserResponseDto updateUserProfile(Long userId, UserUpdateRequestDto request);

    User findUserById(Long id);

    User findUserByUsername(String username);
}
