package com.appointment.handler.staff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotBlank(message = "Staff name is required")
    private String name;

    private String designation;

    private Set<Long> serviceIds;
}
