package com.appointment.handler.availability;

import com.appointment.handler.auth.dto.AuthResponse;
import com.appointment.handler.auth.dto.RegisterRequest;
import com.appointment.handler.auth.entity.User;
import com.appointment.handler.auth.repository.UserRepository;
import com.appointment.handler.auth.service.AuthService;
import com.appointment.handler.availability.dto.HolidayRequest;
import com.appointment.handler.availability.dto.WorkingHoursRequest;
import com.appointment.handler.availability.repository.HolidayRepository;
import com.appointment.handler.availability.repository.WorkingHoursRepository;
import com.appointment.handler.branch.dto.BranchRequest;
import com.appointment.handler.branch.dto.BranchResponse;
import com.appointment.handler.branch.repository.BranchRepository;
import com.appointment.handler.branch.service.BranchService;
import com.appointment.handler.business.dto.BusinessRequest;
import com.appointment.handler.business.dto.BusinessResponse;
import com.appointment.handler.business.repository.BusinessRepository;
import com.appointment.handler.business.service.BusinessService;
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

import java.time.LocalDate;
import java.time.LocalTime;
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
class AvailabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private com.appointment.handler.payment.repository.PaymentRepository paymentRepository;

    @Autowired
    private com.appointment.handler.appointment.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;
    private String customerToken;
    private String staffToken;
    private Long staffProfileId;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        appointmentRepository.deleteAll();
        holidayRepository.deleteAll();
        workingHoursRepository.deleteAll();
        staffRepository.deleteAll();
        branchRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Register Owner
        RegisterRequest ownerReq = RegisterRequest.builder()
                .name("Business Owner")
                .email("owner@clinic.com")
                .phone("1234567890")
                .password("ownerPass")
                .roles(Set.of("BUSINESS_OWNER"))
                .build();
        AuthResponse ownerAuth = authService.register(ownerReq);
        ownerToken = "Bearer " + ownerAuth.getAccessToken();

        User ownerUser = userRepository.findByEmail("owner@clinic.com").orElseThrow();

        // 2. Register Customer
        RegisterRequest customerReq = RegisterRequest.builder()
                .name("Customer")
                .email("customer@clinic.com")
                .phone("0987654321")
                .password("customerPass")
                .build();
        AuthResponse customerAuth = authService.register(customerReq);
        customerToken = "Bearer " + customerAuth.getAccessToken();

        // 3. Register Staff User
        RegisterRequest staffUserReq = RegisterRequest.builder()
                .name("Doctor Smith")
                .email("smith@clinic.com")
                .phone("5555555555")
                .password("smithPass")
                .roles(Set.of("STAFF"))
                .build();
        AuthResponse staffAuth = authService.register(staffUserReq);
        staffToken = "Bearer " + staffAuth.getAccessToken();

        // 4. Create Business and Branch
        BusinessResponse bus = businessService.createBusiness(
                BusinessRequest.builder()
                        .name("Smith Clinic")
                        .description("General clinic")
                        .businessType("DOCTOR")
                        .email("smith@clinic.com")
                        .phone("5555555556")
                        .build(),
                ownerUser
        );

        BranchResponse br = branchService.createBranch(
                bus.getId(),
                BranchRequest.builder()
                        .name("Smith Branch")
                        .address("101 Smith St")
                        .city("Pune")
                        .phone("5555555557")
                        .build(),
                ownerUser
        );

        // 5. Create Staff Profile
        StaffResponse st = staffService.createStaff(
                bus.getId(),
                StaffRequest.builder()
                        .userId(staffAuth.getUserId())
                        .branchId(br.getId())
                        .name("Doctor Smith")
                        .designation("General Physician")
                        .build(),
                ownerUser
        );
        staffProfileId = st.getId();
    }

    @Test
    void shouldManageAvailabilityAndHolidays() throws Exception {
        // 1. Add Working Hours (should succeed for Owner)
        WorkingHoursRequest whReq = WorkingHoursRequest.builder()
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        mockMvc.perform(post("/api/staff/" + staffProfileId + "/availability")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(whReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.dayOfWeek", is("MONDAY")))
                .andExpect(jsonPath("$.data.startTime", is("09:00:00")));

        // 2. Add Holiday (should succeed for Staff member themselves)
        HolidayRequest hReq = HolidayRequest.builder()
                .date(LocalDate.of(2026, 9, 1))
                .reason("Vacation")
                .build();

        mockMvc.perform(post("/api/staff/" + staffProfileId + "/holidays")
                        .header("Authorization", staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.date", is("2026-09-01")))
                .andExpect(jsonPath("$.data.reason", is("Vacation")));

        // 3. Get Working Hours (accessible by Customer)
        mockMvc.perform(get("/api/staff/" + staffProfileId + "/availability")
                        .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));

        // 4. Get Holidays (accessible by Customer)
        mockMvc.perform(get("/api/staff/" + staffProfileId + "/holidays")
                        .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void shouldPreventCustomerFromModifyingAvailability() throws Exception {
        WorkingHoursRequest whReq = WorkingHoursRequest.builder()
                .dayOfWeek("TUESDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        mockMvc.perform(post("/api/staff/" + staffProfileId + "/availability")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(whReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }
}
