package com.nailly.infra.persistence.professional;

import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Converte entre {@link Professional} (domínio) e {@link ProfessionalEntity} (banco). */
final class ProfessionalMapper {

    private ProfessionalMapper() {
    }

    static Professional toDomain(ProfessionalEntity entity) {
        ApprovalPolicy approvalPolicy = new ApprovalPolicy(entity.getBookingApproval(),
                entity.getHomeVisitApproval(), entity.getRequestExpirationMinutes());
        List<HomeVisitFee> fees = entity.getHomeVisitFees().stream()
                .map(fee -> new HomeVisitFee(fee.getMaxDistanceKm(), new Money(fee.getFee())))
                .toList();
        HomeVisitPolicy homeVisitPolicy = new HomeVisitPolicy(entity.isOffersHomeVisit(), origin(entity), fees);

        Professional professional = new Professional(entity.getId(), entity.getUserId(), entity.getName(),
                entity.getPhone(), entity.getBio(), approvalPolicy, homeVisitPolicy);
        if (!entity.isActive()) {
            professional.deactivate();
        }
        return professional;
    }

    /** Copia os dados do domínio para a entidade (nova ou já gravada). */
    static void copyToEntity(Professional professional, ProfessionalEntity entity) {
        entity.setName(professional.getName());
        entity.setPhone(professional.getPhone());
        entity.setBio(professional.getBio());
        entity.setActive(professional.isActive());

        ApprovalPolicy approval = professional.getApprovalPolicy();
        entity.setBookingApproval(approval.getBookingApproval());
        entity.setHomeVisitApproval(approval.getHomeVisitApproval());
        entity.setRequestExpirationMinutes(approval.getRequestExpirationMinutes());

        HomeVisitPolicy homeVisit = professional.getHomeVisitPolicy();
        GeoPoint origin = homeVisit.getOrigin();
        entity.setOffersHomeVisit(homeVisit.isOffered());
        entity.setHomeVisitOriginLatitude(origin == null ? null : coordinate(origin.getLatitude()));
        entity.setHomeVisitOriginLongitude(origin == null ? null : coordinate(origin.getLongitude()));
        copyFees(homeVisit.getFees(), entity.getHomeVisitFees());
    }

    private static GeoPoint origin(ProfessionalEntity entity) {
        if (entity.getHomeVisitOriginLatitude() == null || entity.getHomeVisitOriginLongitude() == null) {
            return null;
        }
        return new GeoPoint(entity.getHomeVisitOriginLatitude().doubleValue(),
                entity.getHomeVisitOriginLongitude().doubleValue());
    }

    /** DECIMAL(9,6) no banco. */
    private static BigDecimal coordinate(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * Atualiza as faixas pela distância, em vez de apagar todas e inserir de novo: o Hibernate
     * faz os INSERTs antes dos DELETEs, e a chave única (professional_id, max_distance_km) quebraria.
     */
    private static void copyFees(List<HomeVisitFee> fees, List<HomeVisitFeeEntity> current) {
        Map<BigDecimal, BigDecimal> wanted = new TreeMap<>();
        fees.forEach(fee -> wanted.put(fee.getMaxDistanceKm(), fee.getFee().getAmount()));

        current.removeIf(entity -> !wanted.containsKey(entity.getMaxDistanceKm()));
        for (HomeVisitFeeEntity entity : current) {
            entity.setFee(wanted.remove(entity.getMaxDistanceKm()));
        }
        wanted.forEach((maxDistanceKm, fee) -> current.add(new HomeVisitFeeEntity(maxDistanceKm, fee)));
    }
}
