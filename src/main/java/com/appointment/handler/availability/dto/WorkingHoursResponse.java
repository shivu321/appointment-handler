package com.appointment.handler.availability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursResponse {
    private Long id;
    private Long staffId;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
