package com.appointment.handler.availability.repository;

import com.appointment.handler.availability.entity.Holiday;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.time.LocalDate;
import java.util.List;

@NoRepositoryBean
public interface HolidayRepository extends CrudRepository<Holiday, Long> {
    List<Holiday> findByStaffId(Long staffId);
    boolean existsByStaffIdAndDate(Long staffId, LocalDate date);
    List<Holiday> findByStaffIdAndDateGreaterThanEqual(Long staffId, LocalDate date);
}
