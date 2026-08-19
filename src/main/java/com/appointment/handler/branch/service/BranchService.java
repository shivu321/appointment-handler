package com.appointment.handler.branch.service;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.branch.dto.BranchRequest;
import com.appointment.handler.branch.dto.BranchResponse;
import com.appointment.handler.branch.entity.Branch;
import com.appointment.handler.branch.repository.BranchRepository;
import com.appointment.handler.business.entity.Business;
import com.appointment.handler.business.repository.BusinessRepository;
import com.appointment.handler.common.enums.BranchStatus;
import com.appointment.handler.common.exception.AppException;
import com.appointment.handler.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final BusinessRepository businessRepository;

    @Transactional
    public BranchResponse createBranch(Long businessId, BranchRequest request, User currentUser) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + businessId));

        verifyBusinessOwnershipOrAdmin(business, currentUser);

        Branch branch = Branch.builder()
                .business(business)
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .status(BranchStatus.ACTIVE)
                .build();

        Branch saved = branchRepository.save(branch);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getBranchesByBusiness(Long businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business not found with id: " + businessId);
        }
        return branchRepository.findByBusinessId(businessId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        return mapToResponse(branch);
    }

    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request, User currentUser) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));

        verifyBusinessOwnershipOrAdmin(branch.getBusiness(), currentUser);

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setCity(request.getCity());
        branch.setLatitude(request.getLatitude());
        branch.setLongitude(request.getLongitude());
        branch.setPhone(request.getPhone());

        Branch updated = branchRepository.save(branch);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteBranch(Long id, User currentUser) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));

        verifyBusinessOwnershipOrAdmin(branch.getBusiness(), currentUser);

        branchRepository.delete(branch);
    }

    private void verifyBusinessOwnershipOrAdmin(Business business, User currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isOwner = business.getOwner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AppException("You do not have permission to modify branches for this business", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }
    }

    public BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .businessId(branch.getBusiness().getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .city(branch.getCity())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .phone(branch.getPhone())
                .status(branch.getStatus().name())
                .build();
    }
}
