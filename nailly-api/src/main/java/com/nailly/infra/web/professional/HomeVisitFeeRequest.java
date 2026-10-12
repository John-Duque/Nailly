package com.nailly.infra.web.professional;

import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.shared.Money;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** Faixa da taxa de deslocamento: até {@code maxDistanceKm} km, cobra {@code fee}. */
public class HomeVisitFeeRequest {

    @NotNull(message = "Informe a distância máxima da faixa.")
    @Positive(message = "A distância máxima da faixa deve ser maior que zero.")
    @Digits(integer = 4, fraction = 1, message = "A distância deve ter até 4 dígitos e 1 casa decimal.")
    private BigDecimal maxDistanceKm;

    @NotNull(message = "Informe o valor da taxa.")
    @PositiveOrZero(message = "A taxa não pode ser negativa.")
    @Digits(integer = 8, fraction = 2, message = "A taxa deve ter até 8 dígitos e 2 casas decimais.")
    private BigDecimal fee;

    HomeVisitFee toDomain() {
        return new HomeVisitFee(maxDistanceKm, new Money(fee));
    }

    public void setMaxDistanceKm(BigDecimal maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
}
