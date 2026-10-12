package com.nailly.infra.persistence.salonservice;

import com.nailly.domain.salonservice.BodyArea;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/** Linha de salon_services. created_at fica com o valor padrão do banco. */
@Entity
@Table(name = "salon_services")
public class SalonServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professional_id", nullable = false, updatable = false)
    private long professionalId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "procedure_type_id", nullable = false)
    private ProcedureTypeEntity procedureType;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "body_area", nullable = false, length = 10)
    private BodyArea bodyArea;

    @Column(name = "duration_minutes", nullable = false)
    private short durationMinutes;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available_at_home", nullable = false)
    private boolean availableAtHome;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected SalonServiceEntity() {
    }

    public SalonServiceEntity(long professionalId) {
        this.professionalId = professionalId;
    }

    public Long getId() {
        return id;
    }

    public long getProfessionalId() {
        return professionalId;
    }

    public ProcedureTypeEntity getProcedureType() {
        return procedureType;
    }

    public void setProcedureType(ProcedureTypeEntity procedureType) {
        this.procedureType = procedureType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BodyArea getBodyArea() {
        return bodyArea;
    }

    public void setBodyArea(BodyArea bodyArea) {
        this.bodyArea = bodyArea;
    }

    public short getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(short durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAvailableAtHome() {
        return availableAtHome;
    }

    public void setAvailableAtHome(boolean availableAtHome) {
        this.availableAtHome = availableAtHome;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
