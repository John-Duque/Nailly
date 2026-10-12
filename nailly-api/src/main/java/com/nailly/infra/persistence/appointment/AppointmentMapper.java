package com.nailly.infra.persistence.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentItem;
import com.nailly.domain.appointment.ServiceLocation;
import com.nailly.domain.shared.Money;

/** Converte entre {@link Appointment} (domínio) e {@link AppointmentEntity} (banco). */
final class AppointmentMapper {

    private AppointmentMapper() {
    }

    static Appointment toDomain(AppointmentEntity entity) {
        return Appointment.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .professionalId(entity.getProfessionalId())
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .location(new ServiceLocation(entity.getServiceMode(), entity.getLocationId(),
                        entity.getCustomerAddressId(), entity.getServiceAddress(), entity.getDistanceKm()))
                .items(entity.getItems().stream().map(AppointmentMapper::toDomain).toList())
                .homeVisitFee(new Money(entity.getHomeVisitFee()))
                .itemsTotal(new Money(entity.getItemsTotal()))
                .totalPrice(new Money(entity.getTotalPrice()))
                .notes(entity.getNotes())
                .statusReason(entity.getStatusReason())
                .decidedAt(entity.getDecidedAt())
                .canceledAt(entity.getCanceledAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private static AppointmentItem toDomain(AppointmentItemEntity entity) {
        return new AppointmentItem(entity.getServiceId(), entity.getServiceTitle(), entity.getProcedureTypeName(),
                entity.getBodyArea(), entity.getDurationMinutes(), new Money(entity.getPrice()));
    }

    /** Entidade completa de um agendamento novo, com os itens. */
    static AppointmentEntity toNewEntity(Appointment appointment) {
        AppointmentEntity entity = new AppointmentEntity(appointment.getCustomerId(),
                appointment.getProfessionalId(), appointment.getStartAt(), appointment.getEndAt());
        ServiceLocation location = appointment.getLocation();
        entity.setExpiresAt(appointment.getExpiresAt());
        entity.setServiceMode(location.getMode());
        entity.setLocationId(location.getLocationId());
        entity.setCustomerAddressId(location.getCustomerAddressId());
        entity.setServiceAddress(location.getAddress());
        entity.setDistanceKm(location.getDistanceKm());
        entity.setHomeVisitFee(appointment.getHomeVisitFee().getAmount());
        entity.setItemsTotal(appointment.getItemsTotal().getAmount());
        entity.setTotalPrice(appointment.getTotalPrice().getAmount());
        entity.setNotes(appointment.getNotes());
        entity.setCreatedAt(appointment.getCreatedAt());
        appointment.getItems().forEach(item -> entity.getItems().add(toEntity(item)));
        copyChangeableFields(appointment, entity);
        return entity;
    }

    /** Campos que mudam com o status: os únicos copiados num agendamento já gravado. */
    static void copyChangeableFields(Appointment appointment, AppointmentEntity entity) {
        entity.setStatus(appointment.getStatus());
        entity.setStatusReason(appointment.getStatusReason());
        entity.setDecidedAt(appointment.getDecidedAt());
        entity.setCanceledAt(appointment.getCanceledAt());
    }

    private static AppointmentItemEntity toEntity(AppointmentItem item) {
        // Cabe no SMALLINT: o domínio limita a duração de cada serviço a 24 horas.
        return new AppointmentItemEntity(item.getServiceId(), item.getServiceTitle(), item.getProcedureTypeName(),
                item.getBodyArea(), (short) item.getDurationMinutes(), item.getPrice().getAmount());
    }
}
