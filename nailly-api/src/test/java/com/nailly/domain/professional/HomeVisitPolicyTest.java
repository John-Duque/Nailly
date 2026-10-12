package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HomeVisitPolicyTest {

    private static final GeoPoint ORIGIN = new GeoPoint(0, 0);

    private static HomeVisitFee fee(String maxDistanceKm, String amount) {
        return new HomeVisitFee(new BigDecimal(maxDistanceKm), Money.of(amount));
    }

    private static final HomeVisitPolicy POLICY = new HomeVisitPolicy(true, ORIGIN,
            List.of(fee("10", "20"), fee("5", "10")));

    @ParameterizedTest
    @CsvSource({"0.0, 10.00", "4.9, 10.00", "5.0, 10.00", "5.1, 20.00", "10.0, 20.00"})
    @DisplayName("Cobra a menor faixa que cobre a distância")
    void chargesSmallestBandThatCoversDistance(String distanceKm, String expectedFee) {
        assertThat(POLICY.feeFor(new BigDecimal(distanceKm))).contains(Money.of(expectedFee));
    }

    @Test
    @DisplayName("Não atende além da última faixa")
    void doesNotServeBeyondLastBand() {
        assertThat(POLICY.feeFor(new BigDecimal("10.1"))).isEmpty();
    }

    @Test
    @DisplayName("Não atende quando não há faixas")
    void doesNotServeWithoutBands() {
        HomeVisitPolicy policy = new HomeVisitPolicy(true, ORIGIN, null);

        assertThat(policy.getFees()).isEmpty();
        assertThat(policy.feeFor(BigDecimal.ONE)).isEmpty();
    }

    @Test
    @DisplayName("Mantém as faixas em ordem de distância")
    void keepsBandsSortedByDistance() {
        assertThat(POLICY.getFees()).extracting(HomeVisitFee::getMaxDistanceKm)
                .containsExactly(new BigDecimal("5.0"), new BigDecimal("10.0"));
    }

    @Test
    @DisplayName("Recusa duas faixas com a mesma distância")
    void rejectsDuplicatedBandDistance() {
        List<HomeVisitFee> fees = List.of(fee("5", "10"), fee("5.0", "15"));

        assertThatThrownBy(() -> new HomeVisitPolicy(true, ORIGIN, fees))
                .isInstanceOf(DomainException.class)
                .hasMessage("Há duas faixas de taxa com a mesma distância.");
    }

    @Test
    @DisplayName("Exige ponto de saída quando atende a domicílio")
    void requiresOriginWhenOffered() {
        assertThatThrownBy(() -> new HomeVisitPolicy(true, null, List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o ponto de saída para atender a domicílio.");
    }

    @Test
    @DisplayName("Sem domicílio não há taxa nem cálculo de distância")
    void notOfferedHasNoFeeAndNoDistance() {
        HomeVisitPolicy policy = HomeVisitPolicy.notOffered();

        assertThat(policy.feeFor(BigDecimal.ONE)).isEmpty();
        assertThatThrownBy(() -> policy.distanceKmTo(ORIGIN))
                .isInstanceOf(DomainException.class)
                .hasMessage("Esta profissional não atende a domicílio.");
    }

    @Test
    @DisplayName("Distância é em linha reta, arredondada para uma casa decimal")
    void distanceIsStraightLineRoundedToOneDecimal() {
        assertThat(POLICY.distanceKmTo(new GeoPoint(0.05, 0))).isEqualTo(new BigDecimal("5.6"));
    }

    @Test
    @DisplayName("Faixa recusa distância e valor inválidos")
    void feeBandRejectsInvalidValues() {
        assertThatThrownBy(() -> new HomeVisitFee(null, Money.ZERO))
                .isInstanceOf(DomainException.class)
                .hasMessage("A distância máxima da faixa deve ser maior que zero.");
        assertThatThrownBy(() -> fee("0", "10"))
                .isInstanceOf(DomainException.class)
                .hasMessage("A distância máxima da faixa deve ser maior que zero.");
        assertThatThrownBy(() -> fee("5.25", "10"))
                .isInstanceOf(DomainException.class)
                .hasMessage("A distância da faixa deve ter no máximo uma casa decimal.");
        assertThatThrownBy(() -> new HomeVisitFee(BigDecimal.ONE, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o valor da taxa de deslocamento.");
    }
}
