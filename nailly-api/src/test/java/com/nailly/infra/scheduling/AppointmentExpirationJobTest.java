package com.nailly.infra.scheduling;

import com.nailly.application.appointment.ExpirePendingAppointmentsUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentExpirationJobTest {

    @Mock
    private ExpirePendingAppointmentsUseCase expirePendingAppointments;

    @Test
    void runsTheExpirationUseCase() {
        when(expirePendingAppointments.execute()).thenReturn(2, 0);
        AppointmentExpirationJob job = new AppointmentExpirationJob(expirePendingAppointments);

        job.expirePendingAppointments();
        job.expirePendingAppointments();

        verify(expirePendingAppointments, times(2)).execute();
    }
}
