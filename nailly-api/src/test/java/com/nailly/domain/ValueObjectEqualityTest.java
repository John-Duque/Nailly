package com.nailly.domain;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
    void money() {
        assertValueEquality(Money.of("10"), Money.of("10.00"), Money.of("10.01"));
        assertThat(Money.of("10.5")).hasToString("R$ 10.50");
    }

    @Test
    void geoPoint() {
        assertValueEquality(new GeoPoint(1, 2), new GeoPoint(1, 2), new GeoPoint(1, 3), new GeoPoint(0, 2));
        assertThat(new GeoPoint(1, 2)).hasToString("(1.0, 2.0)");
    }

    @Test
    void approvalPolicy() {
        assertValueEquality(
                new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 30),
                new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 30),
                new ApprovalPolicy(ApprovalMode.AUTOMATIC, ApprovalMode.MANUAL, 30),
                new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.AUTOMATIC, 30),
                new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, null));
    }

    @Test
    void homeVisitFee() {
        HomeVisitFee fee = new HomeVisitFee(new BigDecimal("5"), Money.of("10"));

        assertValueEquality(fee, new HomeVisitFee(new BigDecimal("5.0"), Money.of("10")),
                new HomeVisitFee(new BigDecimal("6"), Money.of("10")),
                new HomeVisitFee(new BigDecimal("5"), Money.of("11")));
    }

    @Test
    void homeVisitPolicy() {
        GeoPoint origin = new GeoPoint(0, 0);
        List<HomeVisitFee> fees = List.of(new HomeVisitFee(BigDecimal.ONE, Money.of("5")));

        assertValueEquality(new HomeVisitPolicy(true, origin, fees), new HomeVisitPolicy(true, origin, fees),
                new HomeVisitPolicy(false, origin, fees),
                new HomeVisitPolicy(true, new GeoPoint(1, 1), fees),
                new HomeVisitPolicy(true, origin, List.of()));
    }
}
