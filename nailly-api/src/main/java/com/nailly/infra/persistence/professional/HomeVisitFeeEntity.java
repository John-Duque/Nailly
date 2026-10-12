package com.nailly.infra.persistence.professional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Linha de home_visit_fees. A chave estrangeira professional_id é mantida por {@link ProfessionalEntity}. */
@Entity
@Table(name = "home_visit_fees")
public class HomeVisitFeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_distance_km", nullable = false, precision = 5, scale = 1)
    private BigDecimal maxDistanceKm;

    @Column(name = "fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    protected HomeVisitFeeEntity() {
    }

    public HomeVisitFeeEntity(BigDecimal maxDistanceKm, BigDecimal fee) {
        this.maxDistanceKm = maxDistanceKm;
        this.fee = fee;
    }

    public BigDecimal getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
}
