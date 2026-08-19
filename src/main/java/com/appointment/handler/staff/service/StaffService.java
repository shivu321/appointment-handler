package com.appointment.handler.staff.service;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.auth.repository.UserRepository;
import com.appointment.handler.branch.entity.Branch;
import com.appointment.handler.branch.repository.BranchRepository;
import com.appointment.handler.business.entity.Business;
import com.appointment.handler.business.repository.BusinessRepository;
import com.appointment.handler.common.enums.StaffStatus;
import com.appointment.handler.common.exception.AppException;
import com.appointment.handler.common.exception.ResourceNotFoundException;
import com.appointment.handler.service.entity.ServiceEntity;
import com.appointment.handler.service.repository.ServiceRepository;
import com.appointment.handler.staff.dto.StaffRequest;
import com.appointment.handler.staff.dto.StaffResponse;
import com.appointment.handler.staff.entity.Staff;
import com.appointment.handler.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public StaffResponse createStaff(Long businessId, StaffRequest request, User currentUser) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + businessId));

        verifyBusinessOwnershipOrAdmin(business, currentUser);

        User staffUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + request.getBranchId()));

        if (!branch.getBusiness().getId().equals(businessId)) {
            throw new AppException("Branch does not belong to this business", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        Set<ServiceEntity> services = new HashSet<>();
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            services.addAll(serviceRepository.findAllById(request.getServiceIds()));
        }

        Staff staff = Staff.builder()
                .business(business)
                .branch(branch)
                .user(staffUser)
                .name(request.getName())
                .designation(request.getDesignation())
                .status(StaffStatus.ACTIVE)
                .services(services)
                .build();

        Staff saved = staffRepository.save(staff);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByBusiness(Long businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business not found with id: " + businessId);
        }
        return staffRepository.findByBusinessId(businessId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByBranch(Long branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch not found with id: " + branchId);
        }
        return staffRepository.findByBranchId(branchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            throw new ResourceNotFoundException("Service not found with id: " + serviceId);
        }
        return staffRepository.findByServiceId(serviceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with id: " + id));
        return mapToResponse(staff);
    }

    @Transactional
    public StaffResponse updateStaff(Long id, StaffRequest request, User currentUser) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with id: " + id));

        verifyBusinessOwnershipOrAdmin(staff.getBusiness(), currentUser);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + request.getBranchId()));

        if (!branch.getBusiness().getId().equals(staff.getBusiness().getId())) {
            throw new AppException("Branch does not belong to this business", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        Set<ServiceEntity> services = new HashSet<>();
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            services.addAll(serviceRepository.findAllById(request.getServiceIds()));
        }

        staff.setName(request.getName());
        staff.setDesignation(request.getDesignation());
        staff.setBranch(branch);
        staff.setServices(services);

        Staff updated = staffRepository.save(staff);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteStaff(Long id, User currentUser) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with id: " + id));

        verifyBusinessOwnershipOrAdmin(staff.getBusiness(), currentUser);

        staffRepository.delete(staff);
    }

    private void verifyBusinessOwnershipOrAdmin(Business business, User currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isOwner = business.getOwner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AppException("You do not have permission to modify staff for this business", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }
    }

    public StaffResponse mapToResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .userId(staff.getUser().getId())
                .businessId(staff.getBusiness().getId())
                .branchId(staff.getBranch().getId())
                .branchName(staff.getBranch().getName())
                .name(staff.getName())
                .designation(staff.getDesignation())
                .status(staff.getStatus().name())
                .serviceIds(staff.getServices().stream().map(ServiceEntity::getId).collect(Collectors.toSet()))
                .build();
    }
}
