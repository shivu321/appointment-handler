package com.appointment.handler.business.service;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.business.dto.BusinessRequest;
import com.appointment.handler.business.dto.BusinessResponse;
import com.appointment.handler.business.entity.Business;
import com.appointment.handler.business.entity.BusinessType;
import com.appointment.handler.business.repository.BusinessRepository;
import com.appointment.handler.business.repository.BusinessTypeRepository;
import com.appointment.handler.common.enums.BusinessStatus;
import com.appointment.handler.common.exception.AppException;
import com.appointment.handler.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessTypeRepository businessTypeRepository;

    @Transactional
    public BusinessResponse createBusiness(BusinessRequest request, User currentUser) {
        // Only SUPER_ADMIN and BUSINESS_OWNER can create businesses
        boolean isOwnerOrAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_BUSINESS_OWNER"));

        if (!isOwnerOrAdmin) {
            throw new AppException("Only SUPER_ADMIN or BUSINESS_OWNER can create businesses", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        String typeName = request.getBusinessType().toUpperCase().trim();
        BusinessType businessType = businessTypeRepository.findByName(typeName)
                .orElseGet(() -> businessTypeRepository.save(BusinessType.builder().name(typeName).build()));

        Business business = Business.builder()
                .name(request.getName())
                .description(request.getDescription())
                .email(request.getEmail())
                .phone(request.getPhone())
                .businessType(businessType)
                .owner(currentUser)
                .status(BusinessStatus.ACTIVE)
                .build();

        Business saved = businessRepository.save(business);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<BusinessResponse> getBusinesses(String type, String city, String status, String search, Pageable pageable) {
        BusinessStatus businessStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                businessStatus = BusinessStatus.valueOf(status.toUpperCase().trim());
            } catch (IllegalArgumentException ex) {
                throw new AppException("Invalid business status value: " + status, "INVALID_FILTER", HttpStatus.BAD_REQUEST);
            }
        }

        Page<Business> businesses = businessRepository.findAllFiltered(
                type != null && !type.isBlank() ? type.trim() : null,
                city != null && !city.isBlank() ? city.trim() : null,
                businessStatus,
                search != null && !search.isBlank() ? search.trim() : null,
                pageable
        );

        return businesses.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(Long id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + id));
        return mapToResponse(business);
    }

    @Transactional
    public BusinessResponse updateBusiness(Long id, BusinessRequest request, User currentUser) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + id));

        verifyOwnershipOrAdmin(business, currentUser);

        String typeName = request.getBusinessType().toUpperCase().trim();
        BusinessType businessType = businessTypeRepository.findByName(typeName)
                .orElseGet(() -> businessTypeRepository.save(BusinessType.builder().name(typeName).build()));

        business.setName(request.getName());
        business.setDescription(request.getDescription());
        business.setEmail(request.getEmail());
        business.setPhone(request.getPhone());
        business.setBusinessType(businessType);

        Business updated = businessRepository.save(business);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteBusiness(Long id, User currentUser) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + id));

        verifyOwnershipOrAdmin(business, currentUser);

        businessRepository.delete(business);
    }

    private void verifyOwnershipOrAdmin(Business business, User currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isOwner = business.getOwner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AppException("You do not have permission to modify this business", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }
    }

    public BusinessResponse mapToResponse(Business business) {
        return BusinessResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .description(business.getDescription())
                .businessType(business.getBusinessType().getName())
                .email(business.getEmail())
                .phone(business.getPhone())
                .ownerId(business.getOwner().getId())
                .ownerName(business.getOwner().getName())
                .status(business.getStatus().name())
                .createdAt(business.getCreatedAt())
                .updatedAt(business.getUpdatedAt())
                .build();
    }
}
