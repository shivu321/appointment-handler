package com.appointment.handler.business;

import com.appointment.handler.auth.dto.AuthResponse;
import com.appointment.handler.auth.dto.RegisterRequest;
import com.appointment.handler.auth.repository.UserRepository;
import com.appointment.handler.auth.service.AuthService;
import com.appointment.handler.branch.dto.BranchRequest;
import com.appointment.handler.branch.repository.BranchRepository;
import com.appointment.handler.business.dto.BusinessRequest;
import com.appointment.handler.business.repository.BusinessRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BusinessBranchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private BranchRepository branchRepository;

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
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;
    private String customerToken;

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

        // Register Business Owner
        RegisterRequest ownerReq = RegisterRequest.builder()
                .name("Business Owner")
                .email("owner@example.com")
                .phone("1111111111")
                .password("password123")
                .roles(Set.of("BUSINESS_OWNER"))
                .build();
        AuthResponse ownerAuth = authService.register(ownerReq);
        ownerToken = "Bearer " + ownerAuth.getAccessToken();

        // Register Customer
        RegisterRequest customerReq = RegisterRequest.builder()
                .name("Customer")
                .email("customer@example.com")
                .phone("2222222222")
                .password("password123")
                .roles(Set.of("CUSTOMER"))
                .build();
        AuthResponse customerAuth = authService.register(customerReq);
        customerToken = "Bearer " + customerAuth.getAccessToken();
    }

    @Test
    void shouldCreateBusinessAndBranchAndFilterSuccessfully() throws Exception {
        // 1. Create Business (should succeed for Owner)
        BusinessRequest busReq = BusinessRequest.builder()
                .name("Perfect Salon")
                .description("Luxury Styling")
                .businessType("SALON")
                .email("salon@example.com")
                .phone("9876543210")
                .build();

        String busResponseJson = mockMvc.perform(post("/api/businesses")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(busReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Perfect Salon")))
                .andExpect(jsonPath("$.data.businessType", is("SALON")))
                .andReturn().getResponse().getContentAsString();

        Long businessId = objectMapper.readTree(busResponseJson).get("data").get("id").asLong();

        // 2. Create Branch (should succeed for Owner)
        BranchRequest brReq = BranchRequest.builder()
                .name("Main Branch")
                .address("123 Fashion St")
                .city("Pune")
                .latitude(18.5204)
                .longitude(73.8567)
                .phone("9876543211")
                .build();

        mockMvc.perform(post("/api/businesses/" + businessId + "/branches")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Main Branch")))
                .andExpect(jsonPath("$.data.city", is("Pune")));

        // 3. Get Branches of Business
        mockMvc.perform(get("/api/businesses/" + businessId + "/branches")
                        .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Main Branch")));

        // 4. Query / Filter Businesses by Type and City
        mockMvc.perform(get("/api/businesses?type=SALON&city=Pune")
                        .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Perfect Salon")));

        // 5. Query / Filter Businesses with Non-Matching City (should return empty)
        mockMvc.perform(get("/api/businesses?type=SALON&city=Mumbai")
                        .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    void shouldPreventCustomerFromCreatingBusiness() throws Exception {
        BusinessRequest busReq = BusinessRequest.builder()
                .name("Failed Cafe")
                .description("Unpermitted Cafe")
                .businessType("CAFE")
                .email("failed@example.com")
                .phone("0000000000")
                .build();

        mockMvc.perform(post("/api/businesses")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(busReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }
}
