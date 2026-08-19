package com.appointment.handler.availability.service;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.availability.dto.HolidayRequest;
import com.appointment.handler.availability.dto.HolidayResponse;
import com.appointment.handler.availability.dto.WorkingHoursRequest;
import com.appointment.handler.availability.dto.WorkingHoursResponse;
import com.appointment.handler.availability.entity.Holiday;
import com.appointment.handler.availability.entity.WorkingHours;
import com.appointment.handler.availability.repository.HolidayRepository;
import com.appointment.handler.availability.repository.WorkingHoursRepository;
import com.appointment.handler.common.exception.AppException;
import com.appointment.handler.common.exception.ResourceNotFoundException;
import com.appointment.handler.staff.entity.Staff;
import com.appointment.handler.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final WorkingHoursRepository workingHoursRepository;
    private final HolidayRepository holidayRepository;
    private final StaffRepository staffRepository;

    @Transactional
    public WorkingHoursResponse addWorkingHours(Long staffId, WorkingHoursRequest request, User currentUser) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with id: " + staffId));

        verifyStaffOrBusinessOwner(staff, currentUser);

        DayOfWeek day;
        try {
            day = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase().trim());
        } catch (IllegalArgumentException ex) {
            throw new AppException("Invalid day of week: " + request.getDayOfWeek(), "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        if (workingHoursRepository.existsByStaffIdAndDayOfWeek(staffId, day)) {
            throw new AppException("Working hours already set for " + day, "DUPLICATE_ENTRY", HttpStatus.BAD_REQUEST);
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new AppException("Start time must be before end time", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        WorkingHours wh = WorkingHours.builder()
                .staff(staff)
                .dayOfWeek(day)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        WorkingHours saved = workingHoursRepository.save(wh);
        return mapToWorkingHoursResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkingHoursResponse> getWorkingHours(Long staffId) {
        if (!staffRepository.existsById(staffId)) {
            throw new ResourceNotFoundException("Staff profile not found with id: " + staffId);
        }
        return workingHoursRepository.findByStaffId(staffId).stream()
                .map(this::mapToWorkingHoursResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkingHoursResponse updateWorkingHours(Long id, WorkingHoursRequest request, User currentUser) {
        WorkingHours wh = workingHoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Working hours entry not found with id: " + id));

        verifyStaffOrBusinessOwner(wh.getStaff(), currentUser);

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new AppException("Start time must be before end time", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        wh.setStartTime(request.getStartTime());
        wh.setEndTime(request.getEndTime());

        WorkingHours updated = workingHoursRepository.save(wh);
        return mapToWorkingHoursResponse(updated);
    }

    @Transactional
    public void deleteWorkingHours(Long id, User currentUser) {
        WorkingHours wh = workingHoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Working hours entry not found with id: " + id));

        verifyStaffOrBusinessOwner(wh.getStaff(), currentUser);

        workingHoursRepository.delete(wh);
    }

    @Transactional
    public HolidayResponse addHoliday(Long staffId, HolidayRequest request, User currentUser) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with id: " + staffId));

        verifyStaffOrBusinessOwner(staff, currentUser);

        if (holidayRepository.existsByStaffIdAndDate(staffId, request.getDate())) {
            throw new AppException("Holiday already declared for date: " + request.getDate(), "DUPLICATE_ENTRY", HttpStatus.BAD_REQUEST);
        }

        Holiday holiday = Holiday.builder()
                .staff(staff)
                .date(request.getDate())
                .reason(request.getReason())
                .build();

        Holiday saved = holidayRepository.save(holiday);
        return mapToHolidayResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidays(Long staffId) {
        if (!staffRepository.existsById(staffId)) {
            throw new ResourceNotFoundException("Staff profile not found with id: " + staffId);
        }
        return holidayRepository.findByStaffId(staffId).stream()
                .map(this::mapToHolidayResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteHoliday(Long id, User currentUser) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday entry not found with id: " + id));

        verifyStaffOrBusinessOwner(holiday.getStaff(), currentUser);

        holidayRepository.delete(holiday);
    }

    private void verifyStaffOrBusinessOwner(Staff staff, User currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isOwner = staff.getBusiness().getOwner().getId().equals(currentUser.getId());
        boolean isStaffSelf = staff.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner && !isStaffSelf) {
            throw new AppException("You do not have permission to modify this staff schedule", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }
    }

    private WorkingHoursResponse mapToWorkingHoursResponse(WorkingHours wh) {
        return WorkingHoursResponse.builder()
                .id(wh.getId())
                .staffId(wh.getStaff().getId())
                .dayOfWeek(wh.getDayOfWeek().name())
                .startTime(wh.getStartTime())
                .endTime(wh.getEndTime())
                .build();
    }

    private HolidayResponse mapToHolidayResponse(Holiday h) {
        return HolidayResponse.builder()
                .id(h.getId())
                .staffId(h.getStaff().getId())
                .date(h.getDate())
                .reason(h.getReason())
                .build();
    }
}
