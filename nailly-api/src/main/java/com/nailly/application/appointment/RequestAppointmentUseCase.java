package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.appointment.ServiceLocation;
import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.NotFoundException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cliente pede um agendamento. Deve rodar numa transação: a profissional fica travada
 * do início ao fim, para que dois pedidos simultâneos não ocupem o mesmo horário.
 */
public class RequestAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final ProfessionalGateway professionalGateway;
    private final SalonServiceGateway salonServiceGateway;
    private final Clock clock;

    public RequestAppointmentUseCase(AppointmentGateway appointmentGateway,
                                     ProfessionalGateway professionalGateway,
                                     SalonServiceGateway salonServiceGateway,
                                     Clock clock) {
        this.appointmentGateway = appointmentGateway;
        this.professionalGateway = professionalGateway;
        this.salonServiceGateway = salonServiceGateway;
        this.clock = clock;
    }

    public Appointment execute(RequestAppointmentCommand command) {
        Instant now = clock.instant();
        Professional professional = professionalGateway.findByIdForUpdate(command.getProfessionalId())
                .orElseThrow(() -> new NotFoundException("Profissional não encontrada."));

        Appointment appointment = Appointment.request(command.getCustomerId(), professional,
                loadServices(command.getServiceIds()), command.getStartAt(),
                toLocation(command, professional), command.getNotes(), now);

        if (appointmentGateway.existsOccupyingSchedule(appointment.getProfessionalId(),
                appointment.getStartAt(), appointment.getEndAt(), now)) {
            throw new DomainException("Este horário não está mais disponível.");
        }
        return appointmentGateway.save(appointment);
    }

    /** Mantém a ordem e as repetições pedidas: o domínio é quem recusa serviço repetido. */
    private List<SalonService> loadServices(List<Long> serviceIds) {
        Map<Long, SalonService> found = salonServiceGateway.findAllByIds(serviceIds).stream()
                .collect(Collectors.toMap(SalonService::getId, Function.identity(), (first, second) -> first));
        return serviceIds.stream()
                .map(id -> Optional.ofNullable(found.get(id))
                        .orElseThrow(() -> new NotFoundException("Serviço não encontrado.")))
                .toList();
    }

    private static ServiceLocation toLocation(RequestAppointmentCommand command, Professional professional) {
        if (command.getMode() == null) {
            throw new DomainException("Informe se o atendimento é no local ou a domicílio.");
        }
        if (command.getMode() == ServiceMode.AT_LOCATION) {
            return new ServiceLocation(ServiceMode.AT_LOCATION, command.getLocationId(), null,
                    command.getAddress(), null);
        }
        if (command.getDestination() == null) {
            throw new DomainException("Informe a localização do endereço da cliente.");
        }
        BigDecimal distanceKm = professional.getHomeVisitPolicy().distanceKmTo(command.getDestination());
        return new ServiceLocation(ServiceMode.HOME_VISIT, null, command.getCustomerAddressId(),
                command.getAddress(), distanceKm);
    }
}
