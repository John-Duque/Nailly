package com.nailly.domain.professional;

import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.Place;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Horários em hora de São Paulo (UTC-3); os agendamentos chegam em UTC. */
class WorkingHoursTest {

    private static WorkingPeriod period(DayOfWeek day, int startHour, int endHour) {
        return new WorkingPeriod(day, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0));
    }

    /** Terça: 09-12 e 13-18. */
    private static final WorkingHours TUESDAY_WITH_LUNCH = new WorkingHours(10L, 3L,
            List.of(period(DayOfWeek.TUESDAY, 13, 18), period(DayOfWeek.TUESDAY, 9, 12)));

    /** 13/10/2026 é terça; 12:00 UTC = 09:00 em São Paulo. */
    private static Instant tuesdayLocal(int hour, int minute) {
        return Instant.parse("2026-10-13T00:00:00Z").plusSeconds((hour + 3) * 3600L + minute * 60L);
    }

    @Test
    void keepsPeriodsInOrder() {
        assertThat(TUESDAY_WITH_LUNCH.getPeriods()).extracting(WorkingPeriod::getStart)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(13, 0));
        assertThat(TUESDAY_WITH_LUNCH.getProfessionalId()).isEqualTo(10L);
        assertThat(TUESDAY_WITH_LUNCH.getLocationId()).isEqualTo(3L);
        assertThat(TUESDAY_WITH_LUNCH.isHomeVisit()).isFalse();
        assertThat(new WorkingHours(10L, null, null).isHomeVisit()).isTrue();
    }

    @Test
    void coversAppointmentInsideOnePeriod() {
        assertThat(TUESDAY_WITH_LUNCH.covers(tuesdayLocal(9, 0), tuesdayLocal(12, 0))).isTrue();
        assertThat(TUESDAY_WITH_LUNCH.covers(tuesdayLocal(14, 30), tuesdayLocal(16, 0))).isTrue();
    }

    @Test
    void doesNotCoverAppointmentCrossingTheBreak() {
        assertThat(TUESDAY_WITH_LUNCH.covers(tuesdayLocal(11, 30), tuesdayLocal(13, 30))).isFalse();
    }

    @Test
    void doesNotCoverOutsideHoursOrOtherDays() {
        assertThat(TUESDAY_WITH_LUNCH.covers(tuesdayLocal(8, 0), tuesdayLocal(9, 30))).isFalse();
        assertThat(TUESDAY_WITH_LUNCH.covers(tuesdayLocal(17, 30), tuesdayLocal(18, 30))).isFalse();
        Instant wednesdayMorning = tuesdayLocal(10, 0).plusSeconds(86_400);
        assertThat(TUESDAY_WITH_LUNCH.covers(wednesdayMorning, wednesdayMorning.plusSeconds(3600))).isFalse();
    }

    @Test
    void doesNotCoverAppointmentEndingOnTheNextDay() {
        WorkingHours lateNight = new WorkingHours(10L, null, List.of(period(DayOfWeek.TUESDAY, 20, 23)));

        assertThat(lateNight.covers(tuesdayLocal(22, 0), tuesdayLocal(22, 0).plusSeconds(3 * 3600L))).isFalse();
    }

    @Test
    void noPeriodsMeansClosed() {
        assertThat(new WorkingHours(10L, 3L, List.of()).covers(tuesdayLocal(10, 0), tuesdayLocal(11, 0)))
                .isFalse();
    }

    @Test
    void rejectsOverlappingPeriodsOnSameDay() {
        List<WorkingPeriod> overlapping = List.of(period(DayOfWeek.MONDAY, 9, 12), period(DayOfWeek.MONDAY, 11, 14));

        assertThatThrownBy(() -> new WorkingHours(10L, 3L, overlapping))
                .isInstanceOf(DomainException.class)
                .hasMessage("Há horários sobrepostos no mesmo dia.");
    }

    @Test
    void samePeriodOnDifferentDaysIsFine() {
        List<WorkingPeriod> week = List.of(period(DayOfWeek.MONDAY, 9, 18), period(DayOfWeek.FRIDAY, 9, 18));

        assertThat(new WorkingHours(10L, 3L, week).getPeriods()).hasSize(2);
    }

    @Test
    void periodRequiresEndAfterStart() {
        LocalTime nine = LocalTime.of(9, 0);

        assertThatThrownBy(() -> new WorkingPeriod(DayOfWeek.MONDAY, nine, nine))
                .isInstanceOf(DomainException.class)
                .hasMessage("O fim do horário deve ser depois do início.");
        assertThatThrownBy(() -> new WorkingPeriod(null, nine, nine.plusHours(1)))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o dia da semana, o início e o fim do horário.");
    }

    @Test
    void locationBelongsToProfessionalAndCanChange() {
        Address address = new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", "01000-000");
        ProfessionalLocation location = new ProfessionalLocation(3L, 10L, new Place("Estúdio", address, null));
        Place renamed = new Place("Salão", address, null);

        location.update(renamed);
        location.deactivate();

        assertThat(location.getId()).isEqualTo(3L);
        assertThat(location.getProfessionalId()).isEqualTo(10L);
        assertThat(location.belongsTo(10L)).isTrue();
        assertThat(location.belongsTo(11L)).isFalse();
        assertThat(location.getPlace()).isSameAs(renamed);
        assertThat(location.isActive()).isFalse();
        assertThatThrownBy(() -> location.update(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe os dados do local.");
    }
}
