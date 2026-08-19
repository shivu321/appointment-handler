package com.appointment.handler.availability.repository;

import com.appointment.handler.availability.entity.WorkingHours;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.time.DayOfWeek;
import java.util.List;

@NoRepositoryBean
public interface WorkingHoursRepository extends CrudRepository<WorkingHours, Long> {
    List<WorkingHours> findByStaffId(Long staffId);
    boolean existsByStaffIdAndDayOfWeek(Long staffId, DayOfWeek dayOfWeek);
}
