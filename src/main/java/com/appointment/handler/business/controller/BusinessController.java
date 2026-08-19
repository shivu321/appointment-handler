package com.appointment.handler.business.controller;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.business.dto.BusinessRequest;
import com.appointment.handler.business.dto.BusinessResponse;
import com.appointment.handler.business.service.BusinessService;
import com.appointment.handler.common.response.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    public ResponseDto<BusinessResponse> createBusiness(
            @Valid @RequestBody BusinessRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        BusinessResponse response = businessService.createBusiness(request, currentUser);
        return ResponseDto.success("Business created successfully", response);
    }

    @GetMapping
    public ResponseDto<Page<BusinessResponse>> getBusinesses(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<BusinessResponse> response = businessService.getBusinesses(type, city, status, search, pageable);
        return ResponseDto.success("Businesses retrieved successfully", response);
    }

    @GetMapping("/{id}")
    public ResponseDto<BusinessResponse> getBusinessById(@PathVariable Long id) {
        BusinessResponse response = businessService.getBusinessById(id);
        return ResponseDto.success("Business retrieved successfully", response);
    }

    @PutMapping("/{id}")
    public ResponseDto<BusinessResponse> updateBusiness(
            @PathVariable Long id,
            @Valid @RequestBody BusinessRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        BusinessResponse response = businessService.updateBusiness(id, request, currentUser);
        return ResponseDto.success("Business updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ResponseDto<Void> deleteBusiness(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        businessService.deleteBusiness(id, currentUser);
        return ResponseDto.success("Business deleted successfully");
    }
}
