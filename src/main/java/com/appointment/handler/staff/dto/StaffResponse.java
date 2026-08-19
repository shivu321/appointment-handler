package com.appointment.handler.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private Long userId;
    private Long businessId;
    private Long branchId;
    private String branchName;
    private String name;
    private String designation;
    private String status;
    private Set<Long> serviceIds;
}
