package test.project.bookingapp.controller;

import static org.hamcrest.core.StringRegularExpression.matchesRegex;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import test.project.bookingapp.dto.UserLoginRequestDto;
import test.project.bookingapp.dto.UserRegistrationRequestDto;
import test.project.bookingapp.dto.UserResponseDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AuthenticationControllerTests {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Sql(scripts = "/db/add-test-roles.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Should register a new user successfully")
    void registerUser_ShouldReturnUserResponse() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "test@example.com",
                "password123",
                "password123",
                "Jon",
                "Smith");
        UserResponseDto response = new UserResponseDto(
                1L,
                "test@example.com",
                "Jon",
                "Smith");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.firstName").value(response.firstName()))
                .andExpect(jsonPath("$.lastName").value(response.lastName()))
                .andExpect(jsonPath("$.email").value(response.email()));
    }

    @Test
    @DisplayName("Should fail registration with missing fields")
    void registerUser_MissingFields_ShouldReturnBadRequest() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "", "", "", "", "");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/db/add-test-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Should authenticate user and return JWT token")
    void loginUser_ShouldReturnToken() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto("test@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token")
                        .value(matchesRegex(
                        "[A-Za-z0-9\\-\\.\\_\\~]+\\.[A-Za-z0-9\\-\\.\\_\\~]"
                                + "+\\.[A-Za-z0-9\\-\\.\\_\\~]+")));
    }

    @Test
    @DisplayName("Should fail authentication with incorrect credentials")
    void loginUser_IncorrectCredentials_ShouldReturnUnauthorized() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto("test@example.com", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
