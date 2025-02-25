package test.project.bookingapp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import test.project.bookingapp.config.WithMockCustomUser;
import test.project.bookingapp.dto.UserResponseDto;
import test.project.bookingapp.dto.UserRoleUpdateRequestDto;
import test.project.bookingapp.dto.UserRoleUpdateResponseDto;
import test.project.bookingapp.dto.UserUpdateRequestDto;
import test.project.bookingapp.model.RoleName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/db/add-test-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should update user role successfully")
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_ShouldReturnUpdatedRole() throws Exception {
        UserRoleUpdateRequestDto request =
                new UserRoleUpdateRequestDto(Set.of(RoleName.ROLE_ADMIN));
        UserRoleUpdateResponseDto response = new UserRoleUpdateResponseDto(1L,
                Set.of(RoleName.ROLE_ADMIN));

        mockMvc.perform(put("/users/1/role")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(response.userId()))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should fail updating user role without ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void updateUserRole_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        UserRoleUpdateRequestDto request =
                new UserRoleUpdateRequestDto(Set.of(RoleName.ROLE_ADMIN));

        mockMvc.perform(put("/users/1/role")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get current user profile successfully")
    @WithMockCustomUser
    void getCurrentUser_ShouldReturnUserProfile() throws Exception {
        UserResponseDto response = new UserResponseDto(1L, "test@example.com", "John", "Doe");

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.email").value(response.email()))
                .andExpect(jsonPath("$.firstName").value(response.firstName()))
                .andExpect(jsonPath("$.lastName").value(response.lastName()));
    }

    @Test
    @DisplayName("Should update current user profile successfully")
    @WithMockCustomUser
    void updateProfile_ShouldReturnUpdatedProfile() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "newemail@example.com", "NewFirst", "NewLast");
        UserResponseDto response = new UserResponseDto(1L,
                "newemail@example.com", "NewFirst", "NewLast");

        mockMvc.perform(patch("/users/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.email").value(response.email()))
                .andExpect(jsonPath("$.firstName").value(response.firstName()))
                .andExpect(jsonPath("$.lastName").value(response.lastName()));
    }
}
