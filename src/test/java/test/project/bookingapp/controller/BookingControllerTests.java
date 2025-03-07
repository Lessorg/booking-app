package test.project.bookingapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import test.project.bookingapp.config.WithMockCustomUser;
import test.project.bookingapp.dto.bookingdtos.BookingRequestDto;
import test.project.bookingapp.model.role.RoleName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/db/add-test-bookings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookingControllerTests {
    protected static MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

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
    @DisplayName("Should create a new booking")
    @WithMockCustomUser(id = 20L)
    void createBooking_ShouldReturnBookingResponse() throws Exception {
        BookingRequestDto request = new BookingRequestDto(
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(5),
                10L
        );

        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(String.class)))
                .thenReturn("{\"ok\":true}");

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.checkInDate").value(request.checkInDate().toString()))
                .andExpect(jsonPath("$.checkOutDate").value(request.checkOutDate().toString()))
                .andExpect(jsonPath("$.accommodationId").value(request.accommodationId()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Admin should get all bookings")
    @WithMockCustomUser(id = 20L, role = RoleName.ROLE_ADMIN)
    void getBookingsByUserAndStatus_ShouldReturnPagedBookings() throws Exception {
        mockMvc.perform(get("/bookings")
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(100L))
                .andExpect(jsonPath("$.content[0].checkInDate").value("2025-06-01"))
                .andExpect(jsonPath("$.content[0].checkOutDate").value("2025-06-07"))
                .andExpect(jsonPath("$.content[0].accommodationId").value(10L))
                .andExpect(jsonPath("$.content[0].userId").value(20L))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Customer should get their bookings")
    @WithMockCustomUser(id = 20L)
    void getMyBookings_ShouldReturnPagedBookings() throws Exception {
        mockMvc.perform(get("/bookings/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should get booking by ID")
    @WithMockCustomUser(id = 20L)
    void getBookingById_ShouldReturnBooking() throws Exception {
        mockMvc.perform(get("/bookings/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    @DisplayName("Should update booking using PUT")
    @WithMockUser(roles = "CUSTOMER")
    void putUpdateBooking_ShouldReturnUpdatedBooking() throws Exception {
        BookingRequestDto updateRequest = new BookingRequestDto(
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(6),
                11L
        );

        mockMvc.perform(put("/bookings/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkInDate")
                        .value(updateRequest.checkInDate().toString()))
                .andExpect(jsonPath("$.checkOutDate")
                        .value(updateRequest.checkOutDate().toString()));
    }

    @Test
    @DisplayName("Should update booking using PATCH")
    @WithMockUser(roles = "CUSTOMER")
    void patchUpdateBooking_ShouldReturnUpdatedBooking() throws Exception {
        BookingRequestDto updateRequest = new BookingRequestDto(
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(7),
                11L
        );

        mockMvc.perform(patch("/bookings/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkInDate")
                        .value(updateRequest.checkInDate().toString()))
                .andExpect(jsonPath("$.checkOutDate")
                        .value(updateRequest.checkOutDate().toString()));
    }

    @Test
    @DisplayName("Should delete booking")
    @WithMockCustomUser(id = 20L)
    void cancelBooking_ShouldReturnNoContent() throws Exception {
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(String.class)))
                .thenReturn("{\"ok\":true}");

        mockMvc.perform(delete("/bookings/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Unauthorized customers should not access bookings")
    void getBookings_Unauthorized_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should get bookings by status")
    @WithMockUser(roles = "ADMIN")
    void getBookingsByStatus_Admin_ShouldReturnBookings() throws Exception {
        mockMvc.perform(get("/bookings")
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(100L))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Customer should not get another customer's bookings")
    @WithMockCustomUser(id = 21L)
    void getBookingById_NotOwnBooking_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/bookings/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
