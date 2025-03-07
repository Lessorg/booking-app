package test.project.bookingapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import test.project.bookingapp.dto.accommodationdtos.AccommodationRequestDto;
import test.project.bookingapp.dto.accommodationdtos.AccommodationResponseDto;
import test.project.bookingapp.model.accommodation.AccommodationType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/db/add-test-accommodations.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AccommodationControllerTests {
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
    @DisplayName("Should create a new accommodation")
    @Sql(scripts = "/db/clean-data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithMockUser(roles = "ADMIN")
    void createAccommodation_ShouldReturnAccommodationResponse() throws Exception {
        AccommodationRequestDto request = new AccommodationRequestDto(
                AccommodationType.valueOf("HOUSE"),
                "123 Main St",
                "3 Bedroom",
                Arrays.asList("WiFi", "Pool"),
                new BigDecimal("150.00"),
                5
        );
        AccommodationResponseDto expectedResponse = new AccommodationResponseDto(
                1L,
                AccommodationType.valueOf("HOUSE"),
                "123 Main St",
                "3 Bedroom",
                Arrays.asList("WiFi", "Pool"),
                new BigDecimal("150.0"),
                5
        );

        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(String.class)))
                .thenReturn("{\"ok\":true}");

        mockMvc.perform(post("/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.location").value(expectedResponse.location()))
                .andExpect(jsonPath("$.type").value(expectedResponse.type().toString()))
                .andExpect(jsonPath("$.size").value(expectedResponse.size()))
                .andExpect(jsonPath("$.dailyRate").value(expectedResponse.dailyRate().toString()))
                .andExpect(jsonPath("$.availability").value(expectedResponse.availability()))
                .andExpect(jsonPath("$.amenities").isArray())
                .andExpect(jsonPath("$.amenities[0]").value(expectedResponse.amenities().get(0)))
                .andExpect(jsonPath("$.amenities[1]").value(expectedResponse.amenities().get(1)));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get all accommodations successfully")
    void getAllAccommodations_Success() throws Exception {
        List<AccommodationResponseDto> accommodations = createAccommodationResponseDtoList();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<AccommodationResponseDto> page = new PageImpl<>(accommodations,
                pageRequest, accommodations.size());

        mockMvc.perform(get("/accommodations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(page)));
    }

    @Test
    @DisplayName("Should get accommodation by ID")
    void getAccommodationById_ShouldReturnAccommodation() throws Exception {
        mockMvc.perform(get("/accommodations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.location").value("123 Main St"));
    }

    @Test
    @DisplayName("Should update accommodation")
    @WithMockUser(roles = "ADMIN")
    void updateAccommodation_ShouldReturnUpdatedAccommodation() throws Exception {
        AccommodationRequestDto updateRequest = new AccommodationRequestDto(
                AccommodationType.valueOf("APARTMENT"),
                "456 Elm St",
                "2 Bedroom",
                Arrays.asList("WiFi", "Parking"),
                new BigDecimal("120.0"),
                3
        );

        mockMvc.perform(put("/accommodations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value(updateRequest.type().toString()))
                .andExpect(jsonPath("$.location").value(updateRequest.location()))
                .andExpect(jsonPath("$.size").value(updateRequest.size()))
                .andExpect(jsonPath("$.dailyRate").value(updateRequest.dailyRate().toString()))
                .andExpect(jsonPath("$.availability")
                        .value(updateRequest.availability().toString()))
                .andExpect(jsonPath("$.amenities").isArray())
                .andExpect(jsonPath("$.amenities[0]").value("WiFi"))
                .andExpect(jsonPath("$.amenities[1]").value("Parking"));
    }

    @Test
    @DisplayName("Should delete accommodation")
    @WithMockUser(roles = "ADMIN")
    void deleteAccommodation_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/accommodations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private List<AccommodationResponseDto> createAccommodationResponseDtoList() {
        AccommodationResponseDto accommodation1 = new AccommodationResponseDto(
                1L, AccommodationType.HOUSE, "123 Main St", "3 Bedroom",
                Arrays.asList("WiFi", "Pool", "Air Conditioning"), new BigDecimal("150.00"), 5);
        AccommodationResponseDto accommodation2 = new AccommodationResponseDto(
                2L, AccommodationType.APARTMENT, "456 Elm St", "2 Bedroom",
                Arrays.asList("WiFi", "Gym", "Elevator"), new BigDecimal("100.00"), 3);

        return List.of(accommodation1, accommodation2);
    }
}
