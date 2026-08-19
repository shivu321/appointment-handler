package com.appointment.handler.branch.controller;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.branch.dto.BranchRequest;
import com.appointment.handler.branch.dto.BranchResponse;
import com.appointment.handler.branch.service.BranchService;
import com.appointment.handler.common.response.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping("/api/businesses/{businessId}/branches")
    public ResponseDto<BranchResponse> createBranch(
            @PathVariable Long businessId,
            @Valid @RequestBody BranchRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        BranchResponse response = branchService.createBranch(businessId, request, currentUser);
        return ResponseDto.success("Branch created successfully", response);
    }

    @GetMapping("/api/businesses/{businessId}/branches")
    public ResponseDto<List<BranchResponse>> getBranchesByBusiness(@PathVariable Long businessId) {
        List<BranchResponse> response = branchService.getBranchesByBusiness(businessId);
        return ResponseDto.success("Branches retrieved successfully", response);
    }

    @GetMapping("/api/branches/{id}")
    public ResponseDto<BranchResponse> getBranchById(@PathVariable Long id) {
        BranchResponse response = branchService.getBranchById(id);
        return ResponseDto.success("Branch retrieved successfully", response);
    }

    @PutMapping("/api/branches/{id}")
    public ResponseDto<BranchResponse> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        BranchResponse response = branchService.updateBranch(id, request, currentUser);
        return ResponseDto.success("Branch updated successfully", response);
    }

    @DeleteMapping("/api/branches/{id}")
    public ResponseDto<Void> deleteBranch(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        branchService.deleteBranch(id, currentUser);
        return ResponseDto.success("Branch deleted successfully");
    }
}
