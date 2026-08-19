package com.appointment.handler.appointment.controller;

import com.appointment.handler.appointment.dto.AppointmentRequest;
import com.appointment.handler.appointment.dto.AppointmentResponse;
import com.appointment.handler.appointment.service.AppointmentService;
import com.appointment.handler.auth.entity.User;
import com.appointment.handler.common.response.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseDto<AppointmentResponse> bookAppointment(
            @Valid @RequestBody AppointmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        AppointmentResponse response = appointmentService.bookAppointment(request, currentUser);
        return ResponseDto.success("Appointment booked successfully", response);
    }

    @GetMapping("/{id}")
    public ResponseDto<AppointmentResponse> getAppointmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        AppointmentResponse response = appointmentService.getAppointmentById(id, currentUser);
        return ResponseDto.success("Appointment details retrieved successfully", response);
    }

    @PutMapping("/{id}/status")
    public ResponseDto<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal User currentUser
    ) {
        AppointmentResponse response = appointmentService.updateStatus(id, status, currentUser);
        return ResponseDto.success("Appointment status updated successfully", response);
    }
}
