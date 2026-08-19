package com.appointment.handler.availability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponse {
    private Long id;
    private Long staffId;
    private LocalDate date;
    private String reason;
}
