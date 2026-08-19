package com.appointment.handler.availability.service;

import com.appointment.handler.availability.dto.SlotResponse;
import com.appointment.handler.availability.entity.WorkingHours;
import com.appointment.handler.availability.repository.HolidayRepository;
import com.appointment.handler.availability.repository.WorkingHoursRepository;
import com.appointment.handler.common.exception.ResourceNotFoundException;
import com.appointment.handler.service.entity.ServiceEntity;
import com.appointment.handler.service.repository.ServiceRepository;
import com.appointment.handler.staff.entity.Staff;
import com.appointment.handler.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotGenerationService {

    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final HolidayRepository holidayRepository;
    private final com.appointment.handler.appointment.repository.AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<SlotResponse> generateSlots(Long staffId, LocalDate date, Long serviceId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with id: " + staffId));

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        // 1. Check if the staff performs this service
        if (!staff.getServices().contains(service)) {
            return Collections.emptyList();
        }

        // 2. Check if the date is a holiday
        if (holidayRepository.existsByStaffIdAndDate(staffId, date)) {
            return Collections.emptyList();
        }

        // 3. Get Working Hours for that day of week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<WorkingHours> whList = workingHoursRepository.findByStaffId(staffId).stream()
                .filter(wh -> wh.getDayOfWeek() == dayOfWeek)
                .toList();

        if (whList.isEmpty()) {
            return Collections.emptyList();
        }

        List<SlotResponse> slots = new ArrayList<>();
        int duration = service.getDurationMinutes();

        for (WorkingHours wh : whList) {
            LocalTime start = wh.getStartTime();
            LocalTime end = wh.getEndTime();

            // We step by 30 minutes to find all potential slot start times
            while (start.plusMinutes(duration).isBefore(end) || start.plusMinutes(duration).equals(end)) {
                LocalTime slotEnd = start.plusMinutes(duration);

                // Check overlap with active appointments
                boolean hasOverlap = !appointmentRepository.findOverlappingAppointments(
                        staffId, date, start, slotEnd
                ).isEmpty();

                slots.add(SlotResponse.builder()
                        .startTime(start)
                        .endTime(slotEnd)
                        .available(!hasOverlap)
                        .build());

                start = start.plusMinutes(30); // 30-minute steps
            }
        }

        return slots;
    }
}
