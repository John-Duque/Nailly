package com.nailly.infra.web.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentItem;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.appointment.ServiceLocation;
import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.salonservice.BodyArea;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Agendamento devolvido pela API. Datas em UTC (ISO-8601). */
public class AppointmentResponse {

    private final Long id;
    private final long customerId;
    private final long professionalId;
    private final Instant startAt;
    private final Instant endAt;
    private final AppointmentStatus status;
    private final Instant expiresAt;
    private final Location location;
    private final List<Item> items;
    private final BigDecimal itemsTotal;
    private final BigDecimal homeVisitFee;
    private final BigDecimal totalPrice;
    private final String notes;
    private final String statusReason;
    private final Instant decidedAt;
    private final Instant canceledAt;
    private final Instant createdAt;

    private AppointmentResponse(Appointment appointment) {
        this.id = appointment.getId();
        this.customerId = appointment.getCustomerId();
        this.professionalId = appointment.getProfessionalId();
        this.startAt = appointment.getStartAt();
        this.endAt = appointment.getEndAt();
        this.status = appointment.getStatus();
        this.expiresAt = appointment.getExpiresAt();
        this.location = new Location(appointment.getLocation());
        this.items = appointment.getItems().stream().map(Item::new).toList();
        this.itemsTotal = appointment.getItemsTotal().getAmount();
        this.homeVisitFee = appointment.getHomeVisitFee().getAmount();
        this.totalPrice = appointment.getTotalPrice().getAmount();
        this.notes = appointment.getNotes();
        this.statusReason = appointment.getStatusReason();
        this.decidedAt = appointment.getDecidedAt();
        this.canceledAt = appointment.getCanceledAt();
        this.createdAt = appointment.getCreatedAt();
    }

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(appointment);
    }

    public Long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public long getProfessionalId() {
        return professionalId;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Location getLocation() {
        return location;
    }

    public List<Item> getItems() {
        return items;
    }

    public BigDecimal getItemsTotal() {
        return itemsTotal;
    }

    public BigDecimal getHomeVisitFee() {
        return homeVisitFee;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getNotes() {
        return notes;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static class Location {

        private final ServiceMode mode;
        private final Long locationId;
        private final Long customerAddressId;
        private final String address;
        private final BigDecimal distanceKm;

        Location(ServiceLocation location) {
            this.mode = location.getMode();
            this.locationId = location.getLocationId();
            this.customerAddressId = location.getCustomerAddressId();
            this.address = location.getAddress();
            this.distanceKm = location.getDistanceKm();
        }

        public ServiceMode getMode() {
            return mode;
        }

        public Long getLocationId() {
            return locationId;
        }

        public Long getCustomerAddressId() {
            return customerAddressId;
        }

        public String getAddress() {
            return address;
        }

        public BigDecimal getDistanceKm() {
            return distanceKm;
        }
    }

    public static class Item {

        private final long serviceId;
        private final String serviceTitle;
        private final String procedureTypeName;
        private final BodyArea bodyArea;
        private final int durationMinutes;
        private final BigDecimal price;

        Item(AppointmentItem item) {
            this.serviceId = item.getServiceId();
            this.serviceTitle = item.getServiceTitle();
            this.procedureTypeName = item.getProcedureTypeName();
            this.bodyArea = item.getBodyArea();
            this.durationMinutes = item.getDurationMinutes();
            this.price = item.getPrice().getAmount();
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

        public BigDecimal getPrice() {
            return price;
        }
    }
}
