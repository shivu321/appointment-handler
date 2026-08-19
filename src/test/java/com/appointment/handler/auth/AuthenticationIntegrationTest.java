package com.appointment.handler.auth;

import com.appointment.handler.auth.dto.LoginRequest;
import com.appointment.handler.auth.dto.RegisterRequest;
import com.appointment.handler.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.appointment.handler.business.repository.BusinessRepository businessRepository;

    @Autowired
    private com.appointment.handler.branch.repository.BranchRepository branchRepository;

    @Autowired
    private com.appointment.handler.staff.repository.StaffRepository staffRepository;

    @Autowired
    private com.appointment.handler.service.repository.ServiceRepository serviceRepository;

    @Autowired
    private com.appointment.handler.availability.repository.WorkingHoursRepository workingHoursRepository;

    @Autowired
    private com.appointment.handler.availability.repository.HolidayRepository holidayRepository;

    @Autowired
    private com.appointment.handler.payment.repository.PaymentRepository paymentRepository;

    @Autowired
    private com.appointment.handler.appointment.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        appointmentRepository.deleteAll();
        holidayRepository.deleteAll();
        workingHoursRepository.deleteAll();
        staffRepository.deleteAll();
        serviceRepository.deleteAll();
        branchRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .password("securePassword123")
                .roles(Set.of("CUSTOMER"))
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User registered successfully")))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.email", is("john.doe@example.com")));
    }

    @Test
    void shouldFailRegisterWithDuplicateEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("duplicate@example.com")
                .phone("1234567890")
                .password("securePassword123")
                .roles(Set.of("CUSTOMER"))
                .build();

        // Register first time
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Register second time
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.code", is("EMAIL_ALREADY_IN_USE")));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // First register
        RegisterRequest register = RegisterRequest.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .phone("0987654321")
                .password("janeSecure123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        // Now login
        LoginRequest login = LoginRequest.builder()
                .email("jane.doe@example.com")
                .password("janeSecure123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User logged in successfully")))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        LoginRequest login = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")));
    }
}
