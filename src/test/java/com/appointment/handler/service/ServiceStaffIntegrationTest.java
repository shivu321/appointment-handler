package com.appointment.handler.service;

import com.appointment.handler.auth.dto.AuthResponse;
import com.appointment.handler.auth.dto.RegisterRequest;
import com.appointment.handler.auth.entity.User;
import com.appointment.handler.auth.repository.UserRepository;
import com.appointment.handler.auth.service.AuthService;
import com.appointment.handler.branch.dto.BranchRequest;
import com.appointment.handler.branch.dto.BranchResponse;
import com.appointment.handler.branch.repository.BranchRepository;
import com.appointment.handler.branch.service.BranchService;
import com.appointment.handler.business.dto.BusinessRequest;
import com.appointment.handler.business.dto.BusinessResponse;
import com.appointment.handler.business.repository.BusinessRepository;
import com.appointment.handler.business.service.BusinessService;
import com.appointment.handler.service.dto.ServiceRequest;
import com.appointment.handler.service.dto.ServiceResponse;
import com.appointment.handler.service.repository.ServiceRepository;
import com.appointment.handler.service.service.ServiceEntityService;
import com.appointment.handler.staff.dto.StaffRequest;
import com.appointment.handler.staff.repository.StaffRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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
class ServiceStaffIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private com.appointment.handler.payment.repository.PaymentRepository paymentRepository;

    @Autowired
    private com.appointment.handler.appointment.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.appointment.handler.availability.repository.WorkingHoursRepository workingHoursRepository;

    @Autowired
    private com.appointment.handler.availability.repository.HolidayRepository holidayRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private ServiceEntityService serviceService;

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;
    private String customerToken;
    private Long businessId;
    private Long branchId;
    private Long staffUserId;

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

        // 1. Register Owner
        RegisterRequest ownerReq = RegisterRequest.builder()
                .name("Salon Owner")
                .email("owner@salon.com")
                .phone("1112223333")
                .password("ownerPassword")
                .roles(Set.of("BUSINESS_OWNER"))
                .build();
        AuthResponse ownerAuth = authService.register(ownerReq);
        ownerToken = "Bearer " + ownerAuth.getAccessToken();

        // Get owner user entity
        User ownerUser = userRepository.findByEmail("owner@salon.com").orElseThrow();

        // 2. Register Customer
        RegisterRequest customerReq = RegisterRequest.builder()
                .name("Normal Customer")
                .email("customer@normal.com")
                .phone("4445556666")
                .password("customerPassword")
                .build();
        AuthResponse customerAuth = authService.register(customerReq);
        customerToken = "Bearer " + customerAuth.getAccessToken();

        // 3. Register a user who will be the staff member
        RegisterRequest staffUserReq = RegisterRequest.builder()
                .name("Stylist John")
                .email("john@salon.com")
                .phone("7778889999")
                .password("stylistPassword")
                .roles(Set.of("STAFF"))
                .build();
        AuthResponse staffAuth = authService.register(staffUserReq);
        staffUserId = staffAuth.getUserId();

        // 4. Set up business
        BusinessResponse busResp = businessService.createBusiness(
                BusinessRequest.builder()
                        .name("Owner Styling")
                        .description("Hair styling")
                        .businessType("SALON")
                        .email("styling@owner.com")
                        .phone("2223334444")
                        .build(),
                ownerUser
        );
        businessId = busResp.getId();

        // 5. Set up branch
        BranchResponse brResp = branchService.createBranch(
                businessId,
                BranchRequest.builder()
                        .name("West Branch")
                        .address("456 West Road")
                        .city("Pune")
                        .phone("2223334445")
                        .build(),
                ownerUser
        );
        branchId = brResp.getId();
    }

    @Test
    void shouldCreateServicesAndStaffAndMapThem() throws Exception {
        // 1. Create service (should succeed for Owner)
        ServiceRequest sReq = ServiceRequest.builder()
                .name("Haircut")
                .description("Standard haircut")
                .durationMinutes(30)
                .price(BigDecimal.valueOf(250.00))
                .build();

        String sRespJson = mockMvc.perform(post("/api/businesses/" + businessId + "/services")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Haircut")))
                .andReturn().getResponse().getContentAsString();

        Long serviceId = objectMapper.readTree(sRespJson).get("data").get("id").asLong();

        // 2. Create Staff profile mapping to user and branch, containing haircut service
        StaffRequest stReq = StaffRequest.builder()
                .userId(staffUserId)
                .branchId(branchId)
                .name("Stylist John")
                .designation("Senior Stylist")
                .serviceIds(Set.of(serviceId))
                .build();

        mockMvc.perform(post("/api/businesses/" + businessId + "/staff")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Stylist John")))
                .andExpect(jsonPath("$.data.serviceIds[0]", is(serviceId.intValue())));

        // 3. Get Staff by Service ID
        mockMvc.perform(get("/api/staff/service/" + serviceId)
                        .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Stylist John")));
    }
}
