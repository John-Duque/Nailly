package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Atendimento a domicílio da profissional. */
public final class HomeVisitPolicy {

    private final boolean offered;
    private final GeoPoint origin;
    private final List<HomeVisitFee> fees;

    /**
     * @param offered se ela atende a domicílio
     * @param origin  ponto de saída, obrigatório quando atende
     * @param fees    faixas da taxa de deslocamento (guardadas da menor para a maior distância)
     */
    public HomeVisitPolicy(boolean offered, GeoPoint origin, List<HomeVisitFee> fees) {
        if (offered && origin == null) {
            throw new DomainException("Informe o ponto de saída para atender a domicílio.");
        }
        List<HomeVisitFee> sorted = fees == null ? List.of() : fees.stream()
                .sorted(Comparator.comparing(HomeVisitFee::getMaxDistanceKm))
                .toList();
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).getMaxDistanceKm().equals(sorted.get(i - 1).getMaxDistanceKm())) {
                throw new DomainException("Há duas faixas de taxa com a mesma distância.");
            }
        }
        this.offered = offered;
        this.origin = origin;
        this.fees = sorted;
    }

    public static HomeVisitPolicy notOffered() {
        return new HomeVisitPolicy(false, null, List.of());
    }

    /** Distância em linha reta do ponto de saída até o destino, com uma casa decimal. */
    public BigDecimal distanceKmTo(GeoPoint destination) {
        if (!offered) {
            throw new DomainException("Esta profissional não atende a domicílio.");
        }
        return BigDecimal.valueOf(origin.distanceKmTo(destination)).setScale(1, RoundingMode.HALF_UP);
    }

    /** Taxa da menor faixa que comporta a distância. Vazio quando passa da última faixa (não atende). */
    public Optional<Money> feeFor(BigDecimal distanceKm) {
        if (!offered) {
            return Optional.empty();
        }
        return fees.stream()
                .filter(fee -> fee.covers(distanceKm))
                .findFirst()
                .map(HomeVisitFee::getFee);
    }

    public boolean isOffered() {
        return offered;
    }

    public GeoPoint getOrigin() {
        return origin;
    }

    public List<HomeVisitFee> getFees() {
        return fees;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof HomeVisitPolicy policy
                && offered == policy.offered
                && Objects.equals(origin, policy.origin)
                && fees.equals(policy.fees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offered, origin, fees);
    }
}
