package com.nailly.infra.config;

import com.nailly.application.appointment.CancelAppointmentUseCase;
import com.nailly.application.appointment.CompleteAppointmentUseCase;
import com.nailly.application.appointment.ConfirmAppointmentUseCase;
import com.nailly.application.appointment.ExpirePendingAppointmentsUseCase;
import com.nailly.application.appointment.GetAppointmentUseCase;
import com.nailly.application.appointment.MarkAppointmentNoShowUseCase;
import com.nailly.application.appointment.RejectAppointmentUseCase;
import com.nailly.application.appointment.RequestAppointmentUseCase;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.salonservice.SalonServiceGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Registra os casos de uso de agendamento como beans (a camada application não usa anotações). */
@Configuration(proxyBeanMethods = false)
public class AppointmentUseCaseConfig {

    @Bean
    public RequestAppointmentUseCase requestAppointmentUseCase(AppointmentGateway appointmentGateway,
                                                               ProfessionalGateway professionalGateway,
                                                               SalonServiceGateway salonServiceGateway,
                                                               Clock clock) {
        return new RequestAppointmentUseCase(appointmentGateway, professionalGateway, salonServiceGateway, clock);
    }

    @Bean
    public GetAppointmentUseCase getAppointmentUseCase(AppointmentGateway appointmentGateway) {
        return new GetAppointmentUseCase(appointmentGateway);
    }

    @Bean
    public ConfirmAppointmentUseCase confirmAppointmentUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        return new ConfirmAppointmentUseCase(appointmentGateway, clock);
    }

    @Bean
    public RejectAppointmentUseCase rejectAppointmentUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        return new RejectAppointmentUseCase(appointmentGateway, clock);
    }

    @Bean
    public CancelAppointmentUseCase cancelAppointmentUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        return new CancelAppointmentUseCase(appointmentGateway, clock);
    }

    @Bean
    public CompleteAppointmentUseCase completeAppointmentUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        return new CompleteAppointmentUseCase(appointmentGateway, clock);
    }

    @Bean
    public MarkAppointmentNoShowUseCase markAppointmentNoShowUseCase(AppointmentGateway appointmentGateway,
                                                                     Clock clock) {
        return new MarkAppointmentNoShowUseCase(appointmentGateway, clock);
    }

    @Bean
    public ExpirePendingAppointmentsUseCase expirePendingAppointmentsUseCase(AppointmentGateway appointmentGateway,
                                                                             Clock clock) {
        return new ExpirePendingAppointmentsUseCase(appointmentGateway, clock);
    }
}
