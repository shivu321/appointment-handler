package com.appointment.handler.availability.controller;

import com.appointment.handler.availability.dto.SlotResponse;
import com.appointment.handler.availability.service.SlotGenerationService;
import com.appointment.handler.common.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SlotController {

    private final SlotGenerationService slotGenerationService;

    @GetMapping("/api/staff/{staffId}/slots")
    public ResponseDto<List<SlotResponse>> getAvailableSlots(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long serviceId
    ) {
        List<SlotResponse> response = slotGenerationService.generateSlots(staffId, date, serviceId);
        return ResponseDto.success("Slots generated successfully", response);
    }
}
