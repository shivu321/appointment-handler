package com.appointment.handler.branch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private Long id;
    private Long businessId;
    private String name;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String status;
}
