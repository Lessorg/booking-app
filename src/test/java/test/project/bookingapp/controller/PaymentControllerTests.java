package test.project.bookingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
import test.project.bookingapp.dto.payment.PaymentRequestDto;
import test.project.bookingapp.dto.payment.PaymentResponseDto;
import test.project.bookingapp.model.payment.PaymentStatus;
import test.project.bookingapp.model.role.RoleName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/db/add-test-payments.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PaymentControllerTests {
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
    @DisplayName("Should create a new payment session")
    @WithMockCustomUser(id = 3L)
    void createPayment_ShouldReturnPaymentResponse() throws Exception {
        PaymentRequestDto request = new PaymentRequestDto(201L);
        PaymentResponseDto response = new PaymentResponseDto(6L, 201L,
                PaymentStatus.PENDING, "test-url", BigDecimal.valueOf(320.0));

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {

            Session mockSession = mock(Session.class);

            when(mockSession.getUrl()).thenReturn("test-url");
            when(mockSession.getId()).thenReturn("test-session");
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            mockMvc.perform(post("/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(response.id()))
                    .andExpect(jsonPath("$.bookingId").value(response.bookingId()))
                    .andExpect(jsonPath("$.status").value(response.status().toString()))
                    .andExpect(jsonPath("$.sessionUrl").value(response.sessionUrl()))
                    .andExpect(jsonPath("$.amount").value(response.amount()));
        }
    }

    @Test
    @DisplayName("Customer should get their payments")
    @WithMockCustomUser(email = "testuser1@example.com")
    void getMyPayments_ShouldReturnPagedPayments() throws Exception {
        String[] expectedIds = {"1", "2"};
        String[] expectedBookingIds = {"1", "2"};
        String[] expectedStatuses = {"PENDING", "PAID"};
        double[] expectedAmounts = {100.00, 150.00};
        String[] expectedSessionUrls = {"http://test-url.com/session1",
                "http://test-url.com/session2"};

        mockMvc.perform(get("/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(expectedIds.length));

        for (int i = 0; i < expectedIds.length; i++) {
            mockMvc.perform(get("/payments")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[" + i + "].id")
                            .value(expectedIds[i]))
                    .andExpect(jsonPath("$.content[" + i + "].bookingId")
                            .value(expectedBookingIds[i]))
                    .andExpect(jsonPath("$.content[" + i + "].status")
                            .value(expectedStatuses[i]))
                    .andExpect(jsonPath("$.content[" + i + "].amount")
                            .value(expectedAmounts[i]))
                    .andExpect(jsonPath("$.content[" + i + "].sessionUrl")
                            .value(expectedSessionUrls[i]));
        }
    }

    @Test
    @DisplayName("Admin should get all payments")
    @WithMockCustomUser(role = RoleName.ROLE_ADMIN)
    void getAllPayments_Admin_ShouldReturnPagedPayments() throws Exception {
        mockMvc.perform(get("/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    @DisplayName("Should handle successful payment")
    void handlePaymentSuccess_ShouldReturnPaymentResponse() throws Exception {
        String mockTelegramResponse = "{\"ok\":true}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockTelegramResponse);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);
            when(mockSession.getPaymentStatus()).thenReturn("paid");
            mockedSession.when(() -> Session.retrieve(anyString())).thenReturn(mockSession);

            mockMvc.perform(get("/payments/success")
                            .param("sessionId", "test-session-1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAID"));
        }
    }

    @Test
    @DisplayName("Should handle canceled payment")
    void handlePaymentCancel_ShouldReturnCanceledResponse() throws Exception {
        mockMvc.perform(get("/payments/cancel")
                        .param("sessionId", "test-session-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value(
                        "The payment session is still available."
                                + " You can complete the payment within 24 hours."));
    }

    @Test
    @DisplayName("Should renew payment session")
    @WithMockUser(roles = "CUSTOMER")
    void renewPaymentSession_ShouldReturnUpdatedPayment() throws Exception {
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);
            when(mockSession.getId()).thenReturn("new-session-id");
            when(mockSession.getUrl()).thenReturn("new-session-url");

            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            mockMvc.perform(put("/payments/renew/{paymentId}", 5L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.sessionUrl").value("new-session-url"));
        }
    }
}
