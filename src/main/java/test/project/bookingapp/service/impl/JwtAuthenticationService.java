package test.project.bookingapp.service.impl;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import test.project.bookingapp.dto.userdtos.UserLoginRequestDto;
import test.project.bookingapp.dto.userdtos.UserLoginResponseDto;
import test.project.bookingapp.dto.userdtos.UserRegistrationRequestDto;
import test.project.bookingapp.dto.userdtos.UserResponseDto;
import test.project.bookingapp.dto.userdtos.UserRoleUpdateRequestDto;
import test.project.bookingapp.dto.userdtos.UserRoleUpdateResponseDto;
import test.project.bookingapp.dto.userdtos.UserUpdateRequestDto;
import test.project.bookingapp.exception.EmailAlreadyExistsException;
import test.project.bookingapp.exception.EntityNotFoundException;
import test.project.bookingapp.exception.RegistrationException;
import test.project.bookingapp.mapper.UserMapper;
import test.project.bookingapp.model.User;
import test.project.bookingapp.model.role.Role;
import test.project.bookingapp.model.role.RoleName;
import test.project.bookingapp.repository.RoleRepository;
import test.project.bookingapp.repository.UserRepository;
import test.project.bookingapp.service.AuthenticationService;
import test.project.bookingapp.utils.JwtUtils;

@RequiredArgsConstructor
@Service
public class JwtAuthenticationService implements AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final Set<RoleName> defaultUserRoles = Collections.singleton(RoleName.ROLE_CUSTOMER);

    @Override
    public UserLoginResponseDto authenticate(UserLoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(request.email());
        return new UserLoginResponseDto(token);
    }

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new RegistrationException("Can't register user with email "
                    + requestDto.email() + ", email already exists");
        }

        User user = userMapper.toUser(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        user.setRoles(getRoles(defaultUserRoles));
        userRepository.save(user);
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserRoleUpdateResponseDto updateUserRole(Long id, UserRoleUpdateRequestDto request) {
        User user = findUserById(id);
        user.setRoles(getRoles(request.roles()));
        User updatedUser = userRepository.save(user);
        return userMapper.toUserRoleUpdateResponseDto(updatedUser);
    }

    @Override
    public UserResponseDto getUserProfile(Long userId) {
        return userMapper.toUserResponseDto(findUserById(userId));
    }

    @Override
    public UserResponseDto updateUserProfile(Long userId, UserUpdateRequestDto request) {
        User user = findUserById(userId);

        Optional<User> userWithEmail = userRepository.findByEmail(request.email());
        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)) {
            throw new EmailAlreadyExistsException("Email " + request.email() + " already exists");
        }

        userMapper.updateEntity(user, request);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(updatedUser);
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + id));
    }

    @Override
    public User findUserByUsername(String username) {
        return null;
    }

    private Set<Role> getRoles(Set<RoleName> roles) {
        return roles.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new EntityNotFoundException("Role not found: "
                                + roleName)))
                .collect(Collectors.toSet());
    }
}
