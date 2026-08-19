package com.appointment.handler.service.controller;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.common.response.ResponseDto;
import com.appointment.handler.service.dto.ServiceRequest;
import com.appointment.handler.service.dto.ServiceResponse;
import com.appointment.handler.service.service.ServiceEntityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ServiceEntityController {

    private final ServiceEntityService serviceService;

    @PostMapping("/api/businesses/{businessId}/services")
    public ResponseDto<ServiceResponse> createService(
            @PathVariable Long businessId,
            @Valid @RequestBody ServiceRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ServiceResponse response = serviceService.createService(businessId, request, currentUser);
        return ResponseDto.success("Service created successfully", response);
    }

    @GetMapping("/api/businesses/{businessId}/services")
    public ResponseDto<List<ServiceResponse>> getServicesByBusiness(@PathVariable Long businessId) {
        List<ServiceResponse> response = serviceService.getServicesByBusiness(businessId);
        return ResponseDto.success("Services retrieved successfully", response);
    }

    @GetMapping("/api/services/{id}")
    public ResponseDto<ServiceResponse> getServiceById(@PathVariable Long id) {
        ServiceResponse response = serviceService.getServiceById(id);
        return ResponseDto.success("Service retrieved successfully", response);
    }

    @PutMapping("/api/services/{id}")
    public ResponseDto<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ServiceResponse response = serviceService.updateService(id, request, currentUser);
        return ResponseDto.success("Service updated successfully", response);
    }

    @DeleteMapping("/api/services/{id}")
    public ResponseDto<Void> deleteService(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        serviceService.deleteService(id, currentUser);
        return ResponseDto.success("Service deleted successfully");
    }
}
