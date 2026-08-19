package com.appointment.handler.appointment.service;

import com.appointment.handler.appointment.dto.AppointmentRequest;
import com.appointment.handler.appointment.dto.AppointmentResponse;
import com.appointment.handler.appointment.entity.Appointment;
import com.appointment.handler.appointment.enums.AppointmentStatus;
import com.appointment.handler.appointment.repository.AppointmentRepository;
import com.appointment.handler.auth.entity.User;
import com.appointment.handler.availability.entity.WorkingHours;
import com.appointment.handler.availability.repository.HolidayRepository;
import com.appointment.handler.availability.repository.WorkingHoursRepository;
import com.appointment.handler.common.exception.AppException;
import com.appointment.handler.common.exception.ResourceNotFoundException;
import com.appointment.handler.service.entity.ServiceEntity;
import com.appointment.handler.service.repository.ServiceRepository;
import com.appointment.handler.staff.entity.Staff;
import com.appointment.handler.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final HolidayRepository holidayRepository;

    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request, User currentUser) {
        // 1. Lock staff profile to serialize slot check & booking
        Staff staff = staffRepository.findByIdForUpdate(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with id: " + request.getStaffId()));

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + request.getServiceId()));

        // 2. Verify staff performs the service
        if (!staff.getServices().contains(service)) {
            throw new AppException("This staff member does not offer the requested service", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // 3. Check for Holiday
        if (holidayRepository.existsByStaffIdAndDate(request.getStaffId(), request.getDate())) {
            throw new AppException("Requested date falls on a staff holiday", "HOLIDAY", HttpStatus.BAD_REQUEST);
        }

        // 4. Check Working Hours
        DayOfWeek dayOfWeek = request.getDate().getDayOfWeek();
        WorkingHours wh = workingHoursRepository.findByStaffId(request.getStaffId()).stream()
                .filter(w -> w.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElseThrow(() -> new AppException("Staff does not work on " + dayOfWeek, "BAD_REQUEST", HttpStatus.BAD_REQUEST));

        LocalTime startTime = request.getStartTime();
        LocalTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        if (startTime.isBefore(wh.getStartTime()) || endTime.isAfter(wh.getEndTime())) {
            throw new AppException("Requested time is outside working hours", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // 5. Check for overlapping appointments
        List<Appointment> overlaps = appointmentRepository.findOverlappingAppointments(
                request.getStaffId(), request.getDate(), startTime, endTime
        );

        if (!overlaps.isEmpty()) {
            throw new AppException("The requested slot is already booked", "SLOT_CONFLICT", HttpStatus.CONFLICT);
        }

        // 6. Save appointment
        Appointment appointment = Appointment.builder()
                .customer(currentUser)
                .staff(staff)
                .service(service)
                .branch(staff.getBranch())
                .date(request.getDate())
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.PENDING)
                .price(service.getPrice())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id, User currentUser) {
        Appointment app = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        verifyAccess(app, currentUser);
        return mapToResponse(app);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, String statusStr, User currentUser) {
        Appointment app = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        verifyAccess(app, currentUser);

        AppointmentStatus status;
        try {
            status = AppointmentStatus.valueOf(statusStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid appointment status: " + statusStr, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // Prevent unauthorized customer action (customers can only cancel)
        boolean isCustomer = app.getCustomer().getId().equals(currentUser.getId());
        boolean isStaff = app.getStaff().getUser().getId().equals(currentUser.getId());
        boolean isOwner = app.getStaff().getBusiness().getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isCustomer && !isOwner && !isStaff && !isAdmin) {
            if (status != AppointmentStatus.CANCELLED) {
                throw new AppException("Customers are only authorized to cancel their appointments", "FORBIDDEN", HttpStatus.FORBIDDEN);
            }
        }

        app.setStatus(status);
        Appointment saved = appointmentRepository.save(app);
        return mapToResponse(saved);
    }

    private void verifyAccess(Appointment app, User currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isOwner = app.getStaff().getBusiness().getOwner().getId().equals(currentUser.getId());
        boolean isStaff = app.getStaff().getUser().getId().equals(currentUser.getId());
        boolean isCustomer = app.getCustomer().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner && !isStaff && !isCustomer) {
            throw new AppException("You do not have permission to view or edit this appointment", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }
    }

    private AppointmentResponse mapToResponse(Appointment app) {
        return AppointmentResponse.builder()
                .id(app.getId())
                .customerId(app.getCustomer().getId())
                .customerName(app.getCustomer().getName())
                .staffId(app.getStaff().getId())
                .staffName(app.getStaff().getName())
                .serviceId(app.getService().getId())
                .serviceName(app.getService().getName())
                .branchId(app.getBranch().getId())
                .branchName(app.getBranch().getName())
                .date(app.getDate())
                .startTime(app.getStartTime())
                .endTime(app.getEndTime())
                .status(app.getStatus().name())
                .price(app.getPrice())
                .build();
    }
}
