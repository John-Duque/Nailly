package com.nailly.domain.appointment;

import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.Money;

import java.util.Objects;

/**
 * Cópia do serviço no momento do agendamento: o histórico não muda
 * se a profissional alterar título, duração ou preço depois.
 */
public final class AppointmentItem {

    private final long serviceId;
    private final String serviceTitle;
    private final String procedureTypeName;
    private final BodyArea bodyArea;
    private final int durationMinutes;
    private final Money price;

    public AppointmentItem(long serviceId, String serviceTitle, String procedureTypeName, BodyArea bodyArea,
                           int durationMinutes, Money price) {
        this.serviceId = serviceId;
        this.serviceTitle = serviceTitle;
        this.procedureTypeName = procedureTypeName;
        this.bodyArea = bodyArea;
        this.durationMinutes = durationMinutes;
        this.price = price;
    }

    public static AppointmentItem of(SalonService service) {
        Long serviceId = Objects.requireNonNull(service.getId(), "service.id");
        ServiceDetails details = service.getDetails();
        return new AppointmentItem(serviceId, details.getTitle(), details.getProcedureType().getName(),
                details.getBodyArea(), details.getDurationMinutes(), details.getPrice());
    }

    public long getServiceId() {
        return serviceId;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public String getProcedureTypeName() {
        return procedureTypeName;
    }

    public BodyArea getBodyArea() {
        return bodyArea;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public Money getPrice() {
        return price;
    }
}
