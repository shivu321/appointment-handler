package com.appointment.handler.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long staffId;
    private String staffName;
    private Long serviceId;
    private String serviceName;
    private Long branchId;
    private String branchName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private BigDecimal price;
}
