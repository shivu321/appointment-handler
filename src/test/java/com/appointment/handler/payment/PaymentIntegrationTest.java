package com.appointment.handler.payment;

import com.appointment.handler.appointment.dto.AppointmentRequest;
import com.appointment.handler.appointment.dto.AppointmentResponse;
import com.appointment.handler.appointment.repository.AppointmentRepository;
import com.appointment.handler.appointment.service.AppointmentService;
import com.appointment.handler.auth.dto.AuthResponse;
import com.appointment.handler.auth.dto.RegisterRequest;
import com.appointment.handler.auth.entity.User;
import com.appointment.handler.auth.repository.UserRepository;
import com.appointment.handler.auth.service.AuthService;
import com.appointment.handler.availability.dto.WorkingHoursRequest;
import com.appointment.handler.availability.repository.HolidayRepository;
import com.appointment.handler.availability.repository.WorkingHoursRepository;
import com.appointment.handler.availability.service.AvailabilityService;
import com.appointment.handler.branch.dto.BranchRequest;
import com.appointment.handler.branch.dto.BranchResponse;
import com.appointment.handler.branch.repository.BranchRepository;
import com.appointment.handler.branch.service.BranchService;
import com.appointment.handler.business.dto.BusinessRequest;
import com.appointment.handler.business.dto.BusinessResponse;
import com.appointment.handler.business.repository.BusinessRepository;
import com.appointment.handler.business.service.BusinessService;
import com.appointment.handler.payment.dto.PaymentRequest;
import com.appointment.handler.payment.repository.PaymentRepository;
import com.appointment.handler.service.dto.ServiceRequest;
import com.appointment.handler.service.dto.ServiceResponse;
import com.appointment.handler.service.repository.ServiceRepository;
import com.appointment.handler.service.service.ServiceEntityService;
import com.appointment.handler.staff.dto.StaffRequest;
import com.appointment.handler.staff.dto.StaffResponse;
import com.appointment.handler.staff.repository.StaffRepository;
import com.appointment.handler.staff.service.StaffService;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentIntegrationTest {

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
    private WorkingHoursRepository workingHoursRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private ServiceEntityService serviceService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private ObjectMapper objectMapper;

    private String customerToken;
    private String ownerToken;
    private Long staffProfileId;
    private Long serviceId;

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
                .name("Clinic Owner")
                .email("owner@clinic.com")
                .phone("1112223333")
                .password("ownerPassword")
                .roles(Set.of("BUSINESS_OWNER"))
                .build();
        AuthResponse ownerAuth = authService.register(ownerReq);
        ownerToken = "Bearer " + ownerAuth.getAccessToken();

        User ownerUser = userRepository.findByEmail("owner@clinic.com").orElseThrow();

        // 2. Register Customer
        RegisterRequest customerReq = RegisterRequest.builder()
                .name("Normal Customer")
                .email("customer@normal.com")
                .phone("4445556666")
                .password("customerPassword")
                .build();
        AuthResponse customerAuth = authService.register(customerReq);
        customerToken = "Bearer " + customerAuth.getAccessToken();

        // 3. Register Staff User
        RegisterRequest staffUserReq = RegisterRequest.builder()
                .name("Doctor Lisa")
                .email("lisa@clinic.com")
                .phone("5556667777")
                .password("lisaPass")
                .roles(Set.of("STAFF"))
                .build();
        AuthResponse staffAuth = authService.register(staffUserReq);

        // 4. Set up business, branch, and service
        BusinessResponse bus = businessService.createBusiness(
                BusinessRequest.builder()
                        .name("Dental Care")
                        .description("Dental Clinic")
                        .businessType("DOCTOR")
                        .email("dental@care.com")
                        .phone("2223334444")
                        .build(),
                ownerUser
        );

        BranchResponse br = branchService.createBranch(
                bus.getId(),
                BranchRequest.builder()
                        .name("East Branch")
                        .address("202 Dental St")
                        .city("Pune")
                        .phone("2223334445")
                        .build(),
                ownerUser
        );

        ServiceResponse ser = serviceService.createService(
                bus.getId(),
                ServiceRequest.builder()
                        .name("Root Canal")
                        .description("Teeth treatment")
                        .durationMinutes(60)
                        .price(BigDecimal.valueOf(1500.00))
                        .build(),
                ownerUser
        );
        serviceId = ser.getId();

        // 5. Create Staff Profile
        StaffResponse st = staffService.createStaff(
                bus.getId(),
                StaffRequest.builder()
                        .userId(staffAuth.getUserId())
                        .branchId(br.getId())
                        .name("Doctor Lisa")
                        .designation("Dentist")
                        .serviceIds(Set.of(serviceId))
                        .build(),
                ownerUser
        );
        staffProfileId = st.getId();

        // 6. Set up Monday working hours (09:00 - 17:00)
        availabilityService.addWorkingHours(
                staffProfileId,
                WorkingHoursRequest.builder()
                        .dayOfWeek("MONDAY")
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build(),
                ownerUser
        );
    }

    @Test
    void shouldProcessPaymentAndConfirmOrFailAppointment() throws Exception {
        LocalDate mondayDate = LocalDate.of(2026, 9, 7);

        AppointmentRequest bookingReq = AppointmentRequest.builder()
                .staffId(staffProfileId)
                .serviceId(serviceId)
                .date(mondayDate)
                .startTime(LocalTime.of(10, 0))
                .build();

        // 1. Book appointment (status = PENDING)
        String respJson = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andReturn().getResponse().getContentAsString();

        Long appointmentId = objectMapper.readTree(respJson).get("data").get("id").asLong();

        // 2. Pay with declining card (ends with 4444)
        PaymentRequest declinePay = PaymentRequest.builder()
                .appointmentId(appointmentId)
                .paymentMethod("CREDIT_CARD")
                .cardNumber("1111-2222-3333-4444")
                .build();

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(declinePay)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("FAILED")));

        // Check appointment status: should still be PENDING
        mockMvc.perform(get("/api/appointments/" + appointmentId)
                        .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PENDING")));

        // 3. Pay with successful card
        PaymentRequest successPay = PaymentRequest.builder()
                .appointmentId(appointmentId)
                .paymentMethod("CREDIT_CARD")
                .cardNumber("1111-2222-3333-1111")
                .build();

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successPay)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("SUCCESSFUL")));

        // Check appointment status: should now be CONFIRMED
        mockMvc.perform(get("/api/appointments/" + appointmentId)
                        .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CONFIRMED")));
    }
}
