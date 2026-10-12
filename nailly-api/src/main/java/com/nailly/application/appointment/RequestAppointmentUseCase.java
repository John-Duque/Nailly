package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.appointment.ServiceLocation;
import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.customer.CustomerAddressGateway;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.professional.WorkingHoursGateway;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.NotFoundException;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cliente pede um agendamento. Roda numa transação: a profissional fica travada do início
 * ao fim, para que dois pedidos simultâneos não ocupem o mesmo horário.
 */
public class RequestAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final ProfessionalGateway professionalGateway;
    private final SalonServiceGateway salonServiceGateway;
    private final ProfessionalLocationGateway locationGateway;
    private final CustomerAddressGateway customerAddressGateway;
    private final WorkingHoursGateway workingHoursGateway;
    private final Clock clock;

    public RequestAppointmentUseCase(AppointmentGateway appointmentGateway,
                                     ProfessionalGateway professionalGateway,
                                     SalonServiceGateway salonServiceGateway,
                                     ProfessionalLocationGateway locationGateway,
                                     CustomerAddressGateway customerAddressGateway,
                                     WorkingHoursGateway workingHoursGateway,
                                     Clock clock) {
        this.appointmentGateway = appointmentGateway;
        this.professionalGateway = professionalGateway;
        this.salonServiceGateway = salonServiceGateway;
        this.locationGateway = locationGateway;
        this.customerAddressGateway = customerAddressGateway;
        this.workingHoursGateway = workingHoursGateway;
        this.clock = clock;
    }

    public Appointment execute(RequestAppointmentCommand command) {
        Instant now = clock.instant();
        Professional professional = professionalGateway.findByIdForUpdate(command.getProfessionalId())
                .orElseThrow(() -> new NotFoundException("Profissional não encontrada."));

        Appointment appointment = Appointment.request(command.getCustomerId(), professional,
                loadServices(command.getServiceIds()), command.getStartAt(),
                toLocation(command, professional), command.getNotes(), now);

        if (!workingHoursGateway.find(professional.getId(), appointment.getLocation().getLocationId())
                .covers(appointment.getStartAt(), appointment.getEndAt())) {
            throw new DomainException("O horário escolhido está fora do horário de atendimento.");
        }
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

    private ServiceLocation toLocation(RequestAppointmentCommand command, Professional professional) {
        if (command.getMode() == null) {
            throw new DomainException("Informe se o atendimento é no local ou a domicílio.");
        }
        return command.getMode() == ServiceMode.AT_LOCATION
                ? atProfessionalLocation(command.getLocationId(), professional)
                : atCustomerAddress(command.getCustomerAddressId(), command.getCustomerId(), professional);
    }

    private ServiceLocation atProfessionalLocation(Long locationId, Professional professional) {
        if (locationId == null) {
            throw new DomainException("Informe o local do atendimento.");
        }
        ProfessionalLocation location = locationGateway.getByIdAndProfessional(locationId, professional.getId());
        if (!location.isActive()) {
            throw new DomainException("Este local não está mais disponível.");
        }
        return ServiceLocation.atLocation(location.getId(), location.getPlace().getAddress().format());
    }

    private ServiceLocation atCustomerAddress(Long addressId, long customerId, Professional professional) {
        if (addressId == null) {
            throw new DomainException("Informe o endereço da cliente.");
        }
        CustomerAddress address = customerAddressGateway.getByIdAndCustomer(addressId, customerId);
        if (!address.isActive()) {
            throw new DomainException("Este endereço foi removido.");
        }
        return ServiceLocation.homeVisit(address.getId(), address.getPlace().getAddress().format(),
                professional.getHomeVisitPolicy().distanceKmTo(address.requireLocation()));
    }
}
