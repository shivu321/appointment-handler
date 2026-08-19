package com.appointment.handler.branch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchRequest {

    @NotBlank(message = "Branch name is required")
    private String name;

    @NotBlank(message = "Branch address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private Double latitude;

    private Double longitude;

    @NotBlank(message = "Branch phone is required")
    private String phone;
}
