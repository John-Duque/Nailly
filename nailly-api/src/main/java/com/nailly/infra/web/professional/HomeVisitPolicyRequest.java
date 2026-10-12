package com.nailly.infra.web.professional;

import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.shared.GeoPoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Atendimento a domicílio: se atende, de onde sai e as faixas de taxa. */
public class HomeVisitPolicyRequest {

    @NotNull(message = "Informe se atende a domicílio.")
    private Boolean offered;

    @DecimalMin(value = "-90", message = "Latitude inválida.")
    @DecimalMax(value = "90", message = "Latitude inválida.")
    private Double originLatitude;

    @DecimalMin(value = "-180", message = "Longitude inválida.")
    @DecimalMax(value = "180", message = "Longitude inválida.")
    private Double originLongitude;

    @Valid
    private List<@NotNull(message = "Faixa de taxa vazia.") HomeVisitFeeRequest> fees;

    HomeVisitPolicy toDomain() {
        GeoPoint origin = originLatitude == null || originLongitude == null
                ? null
                : new GeoPoint(originLatitude, originLongitude);
        List<HomeVisitFee> bands = fees == null ? List.of() : fees.stream().map(HomeVisitFeeRequest::toDomain).toList();
        return new HomeVisitPolicy(offered, origin, bands);
    }

    public void setOffered(Boolean offered) {
        this.offered = offered;
    }

    public void setOriginLatitude(Double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public void setOriginLongitude(Double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public void setFees(List<HomeVisitFeeRequest> fees) {
        this.fees = fees;
    }
}
