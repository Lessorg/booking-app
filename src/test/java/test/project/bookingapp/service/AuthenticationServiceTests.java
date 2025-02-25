package test.project.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
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

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtAuthenticationService jwtAuthenticationService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private Role role;
    private UserRegistrationRequestDto registrationRequest;
    private UserLoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        role = new Role();
        role.setId(1L);
        role.setName(RoleName.ROLE_CUSTOMER);

        registrationRequest = new UserRegistrationRequestDto(
                "test@example.com",
                "password123",
                "password123",
                "John",
                "Doe");
        loginRequest = new UserLoginRequestDto("test@example.com", "password123");
    }

    @Test
    @DisplayName("Authenticate user and return JWT token")
    void authenticate_UserExists_ReturnsToken() {
        when(authenticationManager.authenticate(
                argThat(token -> token.getPrincipal().equals(loginRequest.email())
                        && token.getCredentials().equals(loginRequest.password())))
        ).thenReturn(null);
        when(jwtAuthenticationService.generateToken(loginRequest.email())).thenReturn("mock-token");

        UserLoginResponseDto response = authenticationService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("mock-token", response.token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Register user when email doesn't exist")
    void register_UserEmailDoesNotExist_ReturnsUserDto() throws RegistrationException {
        when(userRepository.existsByEmail(registrationRequest.email())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(role));
        when(userMapper.toUser(registrationRequest)).thenReturn(user);
        when(passwordEncoder.encode(registrationRequest.password())).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(
                new UserResponseDto(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName()));
        UserResponseDto response = authenticationService.register(registrationRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Register user throws exception when email already exists")
    void register_UserEmailExists_ThrowsRegistrationException() throws RegistrationException {
        when(userRepository.existsByEmail(registrationRequest.email())).thenReturn(true);

        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> authenticationService.register(registrationRequest));

        assertEquals("Can't register user with email test@example.com, email already exists",
                exception.getMessage());
    }

    @Test
    @DisplayName("Update user role")
    void updateUserRole_ValidRequest_ReturnsUpdatedUserRoleResponse() {
        Set<RoleName> roleNames = Set.of(RoleName.ROLE_CUSTOMER);
        Set<RoleName> roleNamesSet =
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto(roleNames);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserRoleUpdateResponseDto(user)).thenReturn(
                new UserRoleUpdateResponseDto(user.getId(), roleNamesSet));

        UserRoleUpdateResponseDto response = authenticationService.updateUserRole(1L, request);

        assertNotNull(response);
        assertEquals(user.getId(), response.userId());
        assertEquals(roleNamesSet, response.roles());
    }

    @Test
    @DisplayName("Update user role throws exception when user not found")
    void updateUserRole_UserNotFound_ThrowsEntityNotFoundException() {
        Set<RoleName> roleNames = Set.of(RoleName.ROLE_CUSTOMER);
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto(roleNames);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authenticationService.updateUserRole(1L, request));

        assertEquals("Can't find user by id: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Get user profile by user ID")
    void getUserProfile_ValidUser_ReturnsUserProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(
                new UserResponseDto(user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName()));

        UserResponseDto response = authenticationService.getUserProfile(1L);

        assertNotNull(response);
        assertEquals(user.getId(), response.id());
        assertEquals(user.getEmail(), response.email());
    }

    @Test
    @DisplayName("Update user profile with new email")
    void updateUserProfile_ValidRequest_ReturnsUpdatedUserProfile() {
        UserUpdateRequestDto updateRequest =
                new UserUpdateRequestDto("newemail@example.com", "John", "Doe");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(updateRequest.email())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(
                new UserResponseDto(user.getId(),
                        updateRequest.email(),
                        user.getFirstName(),
                        user.getLastName()));

        UserResponseDto response = authenticationService.updateUserProfile(1L, updateRequest);

        assertNotNull(response);
        assertEquals(updateRequest.email(), response.email());
    }

    @Test
    @DisplayName("Update user profile throws exception when email already exists")
    void updateUserProfile_EmailExists_ThrowsEmailAlreadyExistsException() {
        UserUpdateRequestDto updateRequest =
                new UserUpdateRequestDto("existingemail@example.com", "John", "Doe");
        User existedUser = new User();
        existedUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(updateRequest.email()))
                .thenReturn(Optional.of(existedUser));

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authenticationService.updateUserProfile(1L, updateRequest));
        assertEquals("Email existingemail@example.com already exists", exception.getMessage());
    }
}
