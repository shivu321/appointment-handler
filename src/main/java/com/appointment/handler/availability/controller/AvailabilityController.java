package com.appointment.handler.availability.controller;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.availability.dto.HolidayRequest;
import com.appointment.handler.availability.dto.HolidayResponse;
import com.appointment.handler.availability.dto.WorkingHoursRequest;
import com.appointment.handler.availability.dto.WorkingHoursResponse;
import com.appointment.handler.availability.service.AvailabilityService;
import com.appointment.handler.common.response.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping("/api/staff/{staffId}/availability")
    public ResponseDto<WorkingHoursResponse> addWorkingHours(
            @PathVariable Long staffId,
            @Valid @RequestBody WorkingHoursRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        WorkingHoursResponse response = availabilityService.addWorkingHours(staffId, request, currentUser);
        return ResponseDto.success("Working hours added successfully", response);
    }

    @GetMapping("/api/staff/{staffId}/availability")
    public ResponseDto<List<WorkingHoursResponse>> getWorkingHours(@PathVariable Long staffId) {
        List<WorkingHoursResponse> response = availabilityService.getWorkingHours(staffId);
        return ResponseDto.success("Working hours retrieved successfully", response);
    }

    @PutMapping("/api/availability/{id}")
    public ResponseDto<WorkingHoursResponse> updateWorkingHours(
            @PathVariable Long id,
            @Valid @RequestBody WorkingHoursRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        WorkingHoursResponse response = availabilityService.updateWorkingHours(id, request, currentUser);
        return ResponseDto.success("Working hours updated successfully", response);
    }

    @DeleteMapping("/api/availability/{id}")
    public ResponseDto<Void> deleteWorkingHours(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        availabilityService.deleteWorkingHours(id, currentUser);
        return ResponseDto.success("Working hours entry deleted successfully");
    }

    @PostMapping("/api/staff/{staffId}/holidays")
    public ResponseDto<HolidayResponse> addHoliday(
            @PathVariable Long staffId,
            @Valid @RequestBody HolidayRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        HolidayResponse response = availabilityService.addHoliday(staffId, request, currentUser);
        return ResponseDto.success("Holiday added successfully", response);
    }

    @GetMapping("/api/staff/{staffId}/holidays")
    public ResponseDto<List<HolidayResponse>> getHolidays(@PathVariable Long staffId) {
        List<HolidayResponse> response = availabilityService.getHolidays(staffId);
        return ResponseDto.success("Holidays retrieved successfully", response);
    }

    @DeleteMapping("/api/holidays/{id}")
    public ResponseDto<Void> deleteHoliday(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        availabilityService.deleteHoliday(id, currentUser);
        return ResponseDto.success("Holiday entry deleted successfully");
    }
}
