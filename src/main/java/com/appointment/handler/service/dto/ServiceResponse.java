package com.appointment.handler.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {
    private Long id;
    private Long businessId;
    private String name;
    private String description;
    private Integer durationMinutes;
    private BigDecimal price;
    private String status;
}
