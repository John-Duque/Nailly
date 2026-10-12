package com.nailly.infra.persistence;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dados usados nos testes de integração da persistência.
 * No H2 não há chaves estrangeiras para users, customers e professional_locations;
 * num MySQL com a V1, esses ids precisam existir antes de rodar os testes.
 */
public final class PersistenceTestData {

    public static final long USER_ID = 1L;
    public static final long OTHER_USER_ID = 2L;
    public static final long CUSTOMER_ID = 1L;
    public static final long CUSTOMER_ADDRESS_ID = 1L;
    public static final long LOCATION_ID = 1L;
    public static final GeoPoint ORIGIN = new GeoPoint(-23.550520, -46.633308);

    private PersistenceTestData() {
    }

    public static HomeVisitFee fee(String maxDistanceKm, String amount) {
        return new HomeVisitFee(new BigDecimal(maxDistanceKm), Money.of(amount));
    }

    public static Professional newProfessional(long userId, ApprovalMode approval) {
        return new Professional(null, userId, "Ana", "11999990000", "Nail designer",
                new ApprovalPolicy(approval, approval, 30),
                new HomeVisitPolicy(true, ORIGIN, List.of(fee("5", "10"), fee("10", "20"))));
    }
}
