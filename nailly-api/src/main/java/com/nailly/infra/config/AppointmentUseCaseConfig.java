package com.nailly.infra.config;

import com.nailly.application.appointment.CancelAppointmentUseCase;
import com.nailly.application.appointment.CompleteAppointmentUseCase;
import com.nailly.application.appointment.ConfirmAppointmentUseCase;
import com.nailly.application.appointment.ExpirePendingAppointmentsUseCase;
import com.nailly.application.appointment.GetAppointmentUseCase;
import com.nailly.application.appointment.ListCustomerAppointmentsUseCase;
import com.nailly.application.appointment.ListProfessionalAgendaUseCase;
import com.nailly.application.appointment.MarkAppointmentNoShowUseCase;
import com.nailly.application.appointment.RejectAppointmentUseCase;
import com.nailly.application.appointment.RequestAppointmentUseCase;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.customer.CustomerAddressGateway;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.professional.WorkingHoursGateway;
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
                                                               ProfessionalLocationGateway locationGateway,
                                                               CustomerAddressGateway customerAddressGateway,
                                                               WorkingHoursGateway workingHoursGateway,
                                                               Clock clock) {
        return new RequestAppointmentUseCase(appointmentGateway, professionalGateway, salonServiceGateway,
                locationGateway, customerAddressGateway, workingHoursGateway, clock);
    }

    @Bean
    public GetAppointmentUseCase getAppointmentUseCase(AppointmentGateway appointmentGateway) {
        return new GetAppointmentUseCase(appointmentGateway);
    }

    @Bean
    public ListProfessionalAgendaUseCase listProfessionalAgendaUseCase(AppointmentGateway appointmentGateway) {
        return new ListProfessionalAgendaUseCase(appointmentGateway);
    }

    @Bean
    public ListCustomerAppointmentsUseCase listCustomerAppointmentsUseCase(AppointmentGateway appointmentGateway) {
        return new ListCustomerAppointmentsUseCase(appointmentGateway);
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
