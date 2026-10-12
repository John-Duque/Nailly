package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.DomainValidation;
import com.nailly.domain.shared.Money;

import java.math.BigDecimal;
import java.util.Objects;

/** Faixa da taxa de deslocamento: até {@code maxDistanceKm} km, cobra {@code fee}. */
public final class HomeVisitFee {

    private final BigDecimal maxDistanceKm;
    private final Money fee;

    public HomeVisitFee(BigDecimal maxDistanceKm, Money fee) {
        if (maxDistanceKm == null || maxDistanceKm.signum() <= 0) {
            throw new DomainException("A distância máxima da faixa deve ser maior que zero.");
        }
        if (fee == null) {
            throw new DomainException("Informe o valor da taxa de deslocamento.");
        }
        this.maxDistanceKm = DomainValidation.requireScale(maxDistanceKm, 1,
                "A distância da faixa deve ter no máximo uma casa decimal.");
        this.fee = fee;
    }

    public boolean covers(BigDecimal distanceKm) {
        return distanceKm.compareTo(maxDistanceKm) <= 0;
    }

    public BigDecimal getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public Money getFee() {
        return fee;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof HomeVisitFee band
                && maxDistanceKm.equals(band.maxDistanceKm)
                && fee.equals(band.fee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxDistanceKm, fee);
    }
}
