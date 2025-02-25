package test.project.bookingapp.service;

import java.util.HashSet;
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
import test.project.bookingapp.dto.UserLoginRequestDto;
import test.project.bookingapp.dto.UserLoginResponseDto;
import test.project.bookingapp.dto.UserRegistrationRequestDto;
import test.project.bookingapp.dto.UserResponseDto;
import test.project.bookingapp.dto.UserRoleUpdateRequestDto;
import test.project.bookingapp.dto.UserRoleUpdateResponseDto;
import test.project.bookingapp.dto.UserUpdateRequestDto;
import test.project.bookingapp.exception.EmailAlreadyExistsException;
import test.project.bookingapp.exception.EntityNotFoundException;
import test.project.bookingapp.exception.RegistrationException;
import test.project.bookingapp.mapper.UserMapper;
import test.project.bookingapp.model.Role;
import test.project.bookingapp.model.RoleName;
import test.project.bookingapp.model.User;
import test.project.bookingapp.repository.RoleRepository;
import test.project.bookingapp.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final AuthenticationManager authenticationManager;

    public UserLoginResponseDto authenticate(UserLoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtAuthenticationService.generateToken(request.email());
        return new UserLoginResponseDto(token);
    }

    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new RegistrationException("Can't register user with email "
                    + requestDto.email() + ", email already exists");
        }
        User user = userMapper.toUser(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: "
                        + RoleName.ROLE_CUSTOMER)));
        user.setRoles(roles);

        userRepository.save(user);
        return userMapper.toUserResponseDto(user);
    }

    public UserRoleUpdateResponseDto updateUserRole(Long id, UserRoleUpdateRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find user by id: " + id));

        Set<Role> newRoles = request.roles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new EntityNotFoundException("Role not found: "
                                + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);
        User updatedUser = userRepository.save(user);

        return userMapper.toUserRoleUpdateResponseDto(updatedUser);
    }

    public UserResponseDto getUserProfile(Long userId) {
        return userMapper.toUserResponseDto(findBookById(userId));
    }

    public UserResponseDto updateUserProfile(Long userId, UserUpdateRequestDto request) {
        User user = findBookById(userId);
        Optional<User> userWithEmail = userRepository.findByEmail(request.email());
        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)) {
            throw new EmailAlreadyExistsException("Email " + request.email() + " already exists");
        }
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(updatedUser);
    }

    private User findBookById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + id));
    }
}
