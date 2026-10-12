package com.nailly.infra.web.professional;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.shared.GeoPoint;

import java.math.BigDecimal;
import java.util.List;

/** Profissional devolvida pela API. */
public class ProfessionalResponse {

    private final Long id;
    private final long userId;
    private final String name;
    private final String phone;
    private final String bio;
    private final boolean active;
    private final Approval approvalPolicy;
    private final HomeVisit homeVisitPolicy;

    private ProfessionalResponse(Professional professional) {
        this.id = professional.getId();
        this.userId = professional.getUserId();
        this.name = professional.getName();
        this.phone = professional.getPhone();
        this.bio = professional.getBio();
        this.active = professional.isActive();
        this.approvalPolicy = new Approval(professional.getApprovalPolicy());
        this.homeVisitPolicy = new HomeVisit(professional.getHomeVisitPolicy());
    }

    public static ProfessionalResponse from(Professional professional) {
        return new ProfessionalResponse(professional);
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getBio() {
        return bio;
    }

    public boolean isActive() {
        return active;
    }

    public Approval getApprovalPolicy() {
        return approvalPolicy;
    }

    public HomeVisit getHomeVisitPolicy() {
        return homeVisitPolicy;
    }

    public static class Approval {

        private final ApprovalMode bookingApproval;
        private final ApprovalMode homeVisitApproval;
        private final Integer requestExpirationMinutes;

        Approval(ApprovalPolicy policy) {
            this.bookingApproval = policy.getBookingApproval();
            this.homeVisitApproval = policy.getHomeVisitApproval();
            this.requestExpirationMinutes = policy.getRequestExpirationMinutes();
        }

        public ApprovalMode getBookingApproval() {
            return bookingApproval;
        }

        public ApprovalMode getHomeVisitApproval() {
            return homeVisitApproval;
        }

        public Integer getRequestExpirationMinutes() {
            return requestExpirationMinutes;
        }
    }

    public static class HomeVisit {

        private final boolean offered;
        private final Double originLatitude;
        private final Double originLongitude;
        private final List<Fee> fees;

        HomeVisit(HomeVisitPolicy policy) {
            GeoPoint origin = policy.getOrigin();
            this.offered = policy.isOffered();
            this.originLatitude = origin == null ? null : origin.getLatitude();
            this.originLongitude = origin == null ? null : origin.getLongitude();
            this.fees = policy.getFees().stream().map(Fee::new).toList();
        }

        public boolean isOffered() {
            return offered;
        }

        public Double getOriginLatitude() {
            return originLatitude;
        }

        public Double getOriginLongitude() {
            return originLongitude;
        }

        public List<Fee> getFees() {
            return fees;
        }
    }

    public static class Fee {

        private final BigDecimal maxDistanceKm;
        private final BigDecimal fee;

        Fee(HomeVisitFee band) {
            this.maxDistanceKm = band.getMaxDistanceKm();
            this.fee = band.getFee().getAmount();
        }

        public BigDecimal getMaxDistanceKm() {
            return maxDistanceKm;
        }

        public BigDecimal getFee() {
            return fee;
        }
    }
}
