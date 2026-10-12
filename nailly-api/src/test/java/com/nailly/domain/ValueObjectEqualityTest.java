package com.nailly.domain;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.WorkingPeriod;
import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Value objects são iguais quando todos os valores são iguais. */
class ValueObjectEqualityTest {

    private static void assertValueEquality(Object value, Object sameValue, Object... differentValues) {
        assertThat(value)
                .isEqualTo(value)
                .isEqualTo(sameValue)
                .hasSameHashCodeAs(sameValue)
                .isNotEqualTo(null)
                .isNotEqualTo("outro tipo");
        for (Object different : differentValues) {
            assertThat(value).isNotEqualTo(different);
        }
    }

    @Test
    @DisplayName("Dinheiro é igual quando o valor é o mesmo, com ou sem zeros à direita")
    void money() {
        assertValueEquality(Money.of("10"), Money.of("10.00"), Money.of("10.01"));
        assertThat(Money.of("10.5")).hasToString("R$ 10.50");
    }

    @Test
    @DisplayName("Coordenada é igual quando latitude e longitude são as mesmas")
    void geoPoint() {
        assertValueEquality(new GeoPoint(1, 2), new GeoPoint(1, 2), new GeoPoint(1, 3), new GeoPoint(0, 2));
        assertThat(new GeoPoint(1, 2)).hasToString("(1.0, 2.0)");
    }

    @Test
    @DisplayName("Regras de aprovação são iguais quando os modos e o prazo são os mesmos")
    void approvalPolicy() {
        assertValueEquality(
                new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 30),
                new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 30),
                new ApprovalPolicy(ApprovalMode.AUTOMATIC, ApprovalMode.MANUAL, 30),
                new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.AUTOMATIC, 30),
                new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, null));
    }

    @Test
    @DisplayName("Faixa de taxa é igual quando distância e valor são os mesmos")
    void homeVisitFee() {
        HomeVisitFee fee = new HomeVisitFee(new BigDecimal("5"), Money.of("10"));

        assertValueEquality(fee, new HomeVisitFee(new BigDecimal("5.0"), Money.of("10")),
                new HomeVisitFee(new BigDecimal("6"), Money.of("10")),
                new HomeVisitFee(new BigDecimal("5"), Money.of("11")));
    }

    @Test
    @DisplayName("Endereço é igual depois de normalizar complemento, UF e CEP")
    void address() {
        assertValueEquality(
                new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", "01000-000"),
                new Address("Rua A", "10", " ", "Centro", "São Paulo", "sp", "01000000"),
                new Address("Rua A", "11", null, "Centro", "São Paulo", "SP", "01000-000"));
    }

    @Test
    @DisplayName("Período de atendimento é igual quando dia, início e fim são os mesmos")
    void workingPeriod() {
        WorkingPeriod morning = new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));

        assertValueEquality(morning, new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new WorkingPeriod(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));
    }

    @Test
    @DisplayName("Regras de domicílio são iguais quando oferta, origem e faixas são as mesmas")
    void homeVisitPolicy() {
        GeoPoint origin = new GeoPoint(0, 0);
        List<HomeVisitFee> fees = List.of(new HomeVisitFee(BigDecimal.ONE, Money.of("5")));

        assertValueEquality(new HomeVisitPolicy(true, origin, fees), new HomeVisitPolicy(true, origin, fees),
                new HomeVisitPolicy(false, origin, fees),
                new HomeVisitPolicy(true, new GeoPoint(1, 1), fees),
                new HomeVisitPolicy(true, origin, List.of()));
    }
}
