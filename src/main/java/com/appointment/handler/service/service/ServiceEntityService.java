package com.appointment.handler.service.service;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.business.entity.Business;
import com.appointment.handler.business.repository.BusinessRepository;
import com.appointment.handler.common.enums.ServiceStatus;
import com.appointment.handler.common.exception.AppException;
import com.appointment.handler.common.exception.ResourceNotFoundException;
import com.appointment.handler.service.dto.ServiceRequest;
import com.appointment.handler.service.dto.ServiceResponse;
import com.appointment.handler.service.entity.ServiceEntity;
import com.appointment.handler.service.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceEntityService {

    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;

    @Transactional
    public ServiceResponse createService(Long businessId, ServiceRequest request, User currentUser) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + businessId));

        verifyBusinessOwnershipOrAdmin(business, currentUser);

        ServiceEntity service = ServiceEntity.builder()
                .business(business)
                .name(request.getName())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .price(request.getPrice())
                .status(ServiceStatus.ACTIVE)
                .build();

        ServiceEntity saved = serviceRepository.save(service);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesByBusiness(Long businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business not found with id: " + businessId);
        }
        return serviceRepository.findByBusinessId(businessId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        return mapToResponse(service);
    }

    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request, User currentUser) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        verifyBusinessOwnershipOrAdmin(service.getBusiness(), currentUser);

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());

        ServiceEntity updated = serviceRepository.save(service);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteService(Long id, User currentUser) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        verifyBusinessOwnershipOrAdmin(service.getBusiness(), currentUser);

        serviceRepository.delete(service);
    }

    private void verifyBusinessOwnershipOrAdmin(Business business, User currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isOwner = business.getOwner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AppException("You do not have permission to modify services for this business", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }
    }

    public ServiceResponse mapToResponse(ServiceEntity service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .businessId(service.getBusiness().getId())
                .name(service.getName())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .status(service.getStatus().name())
                .build();
    }
}
