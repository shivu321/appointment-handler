package com.appointment.handler.staff.controller;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.common.response.ResponseDto;
import com.appointment.handler.staff.dto.StaffRequest;
import com.appointment.handler.staff.dto.StaffResponse;
import com.appointment.handler.staff.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/api/businesses/{businessId}/staff")
    public ResponseDto<StaffResponse> createStaff(
            @PathVariable Long businessId,
            @Valid @RequestBody StaffRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        StaffResponse response = staffService.createStaff(businessId, request, currentUser);
        return ResponseDto.success("Staff profile created successfully", response);
    }

    @GetMapping("/api/businesses/{businessId}/staff")
    public ResponseDto<List<StaffResponse>> getStaffByBusiness(@PathVariable Long businessId) {
        List<StaffResponse> response = staffService.getStaffByBusiness(businessId);
        return ResponseDto.success("Staff profiles retrieved successfully", response);
    }

    @GetMapping("/api/staff/{id}")
    public ResponseDto<StaffResponse> getStaffById(@PathVariable Long id) {
        StaffResponse response = staffService.getStaffById(id);
        return ResponseDto.success("Staff profile retrieved successfully", response);
    }

    @GetMapping("/api/staff/service/{serviceId}")
    public ResponseDto<List<StaffResponse>> getStaffByService(@PathVariable Long serviceId) {
        List<StaffResponse> response = staffService.getStaffByService(serviceId);
        return ResponseDto.success("Staff profiles retrieved successfully", response);
    }

    @PutMapping("/api/staff/{id}")
    public ResponseDto<StaffResponse> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        StaffResponse response = staffService.updateStaff(id, request, currentUser);
        return ResponseDto.success("Staff profile updated successfully", response);
    }

    @DeleteMapping("/api/staff/{id}")
    public ResponseDto<Void> deleteStaff(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        staffService.deleteStaff(id, currentUser);
        return ResponseDto.success("Staff profile deleted successfully");
    }
}
