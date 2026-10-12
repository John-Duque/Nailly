package com.nailly.infra.persistence.appointment;

import com.nailly.domain.salonservice.BodyArea;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * Linha de appointment_items. Só é inserida: a cópia do serviço não muda depois do agendamento.
 * A chave estrangeira appointment_id é mantida por {@link AppointmentEntity}.
 */
@Entity
@Table(name = "appointment_items")
public class AppointmentItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false, updatable = false)
    private long serviceId;

    @Column(name = "service_title", nullable = false, updatable = false, length = 120)
    private String serviceTitle;

    @Column(name = "procedure_type_name", nullable = false, updatable = false, length = 80)
    private String procedureTypeName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "body_area", nullable = false, updatable = false, length = 10)
    private BodyArea bodyArea;

    @Column(name = "duration_minutes", nullable = false, updatable = false)
    private short durationMinutes;

    @Column(name = "price", nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected AppointmentItemEntity() {
    }

    public AppointmentItemEntity(long serviceId, String serviceTitle, String procedureTypeName, BodyArea bodyArea,
                                 short durationMinutes, BigDecimal price) {
        this.serviceId = serviceId;
        this.serviceTitle = serviceTitle;
        this.procedureTypeName = procedureTypeName;
        this.bodyArea = bodyArea;
        this.durationMinutes = durationMinutes;
        this.price = price;
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

    public short getDurationMinutes() {
        return durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
