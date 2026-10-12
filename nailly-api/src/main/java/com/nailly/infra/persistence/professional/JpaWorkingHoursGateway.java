package com.nailly.infra.persistence.professional;

import com.nailly.domain.professional.WorkingHours;
import com.nailly.domain.professional.WorkingHoursGateway;
import com.nailly.domain.professional.WorkingPeriod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Transactional
public class JpaWorkingHoursGateway implements WorkingHoursGateway {

    private final WorkingHoursJpaRepository repository;

    public JpaWorkingHoursGateway(WorkingHoursJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkingHours find(long professionalId, Long locationId) {
        return new WorkingHours(professionalId, locationId, toPeriods(rowsOf(professionalId, locationId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkingHours> findAllByProfessional(long professionalId) {
        // HashMap aceita a chave nula (domicílio); o Collectors.groupingBy não aceitaria.
        Map<Long, List<WorkingHoursEntity>> byLocation = new LinkedHashMap<>();
        for (WorkingHoursEntity row :
                repository.findByProfessionalIdOrderByLocationIdAscDayOfWeekAscStartTimeAsc(professionalId)) {
            byLocation.computeIfAbsent(row.getLocationId(), key -> new ArrayList<>()).add(row);
        }
        return byLocation.entrySet().stream()
                .map(entry -> new WorkingHours(professionalId, entry.getKey(), toPeriods(entry.getValue())))
                .toList();
    }

    /** Apaga os períodos daquele local (ou do domicílio) e grava os novos. */
    @Override
    public WorkingHours save(WorkingHours workingHours) {
        long professionalId = workingHours.getProfessionalId();
        Long locationId = workingHours.getLocationId();
        repository.deleteAll(rowsOf(professionalId, locationId));
        repository.saveAll(workingHours.getPeriods().stream()
                .map(period -> new WorkingHoursEntity(professionalId, locationId,
                        (byte) period.getDayOfWeek().getValue(), period.getStart(), period.getEnd()))
                .toList());
        return find(professionalId, locationId);
    }

    private List<WorkingHoursEntity> rowsOf(long professionalId, Long locationId) {
        return locationId == null
                ? repository.findByProfessionalIdAndLocationIdIsNull(professionalId)
                : repository.findByProfessionalIdAndLocationId(professionalId, locationId);
    }

    private static List<WorkingPeriod> toPeriods(List<WorkingHoursEntity> rows) {
        return rows.stream()
                .map(row -> new WorkingPeriod(DayOfWeek.of(row.getDayOfWeek()), row.getStartTime(), row.getEndTime()))
                .toList();
    }
}
