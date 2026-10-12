package com.nailly.infra.persistence.appointment;

import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.appointment.ServiceMode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Linha de appointments. Os campos marcados com {@code updatable = false} só são gravados
 * na criação; depois disso só mudam o status e as datas de decisão e de cancelamento.
 */
@Entity
@Table(name = "appointments")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private long customerId;

    @Column(name = "professional_id", nullable = false, updatable = false)
    private long professionalId;

    @Column(name = "start_at", nullable = false, updatable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false, updatable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "expires_at", updatable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "service_mode", nullable = false, updatable = false, length = 15)
    private ServiceMode serviceMode;

    @Column(name = "location_id", updatable = false)
    private Long locationId;

    @Column(name = "customer_address_id", updatable = false)
    private Long customerAddressId;

    @Column(name = "service_address", nullable = false, updatable = false, length = 400)
    private String serviceAddress;

    @Column(name = "distance_km", updatable = false, precision = 5, scale = 1)
    private BigDecimal distanceKm;

    @Column(name = "home_visit_fee", nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal homeVisitFee;

    @Column(name = "items_total", nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal itemsTotal;

    @Column(name = "total_price", nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "notes", updatable = false, length = 500)
    private String notes;

    @Column(name = "status_reason")
    private String statusReason;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "appointment_id", nullable = false, updatable = false)
    @OrderBy("id")
    private List<AppointmentItemEntity> items = new ArrayList<>();

    protected AppointmentEntity() {
    }

    /** Dados que não mudam depois da criação. */
    public AppointmentEntity(long customerId, long professionalId, Instant startAt, Instant endAt) {
        this.customerId = customerId;
        this.professionalId = professionalId;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
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

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public ServiceMode getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(ServiceMode serviceMode) {
        this.serviceMode = serviceMode;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(Long customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getHomeVisitFee() {
        return homeVisitFee;
    }

    public void setHomeVisitFee(BigDecimal homeVisitFee) {
        this.homeVisitFee = homeVisitFee;
    }

    public BigDecimal getItemsTotal() {
        return itemsTotal;
    }

    public void setItemsTotal(BigDecimal itemsTotal) {
        this.itemsTotal = itemsTotal;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(Instant canceledAt) {
        this.canceledAt = canceledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<AppointmentItemEntity> getItems() {
        return items;
    }
}
