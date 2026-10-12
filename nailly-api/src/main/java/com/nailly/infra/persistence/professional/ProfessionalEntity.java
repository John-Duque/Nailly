package com.nailly.infra.persistence.professional;

import com.nailly.domain.professional.ApprovalMode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Linha de professionals. created_at fica com o valor padrão do banco. */
@Entity
@Table(name = "professionals")
public class ProfessionalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private long userId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "bio", length = 500)
    private String bio;

    // VARCHAR no banco: sem isto, o Hibernate espera o tipo ENUM nativo do MySQL.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "booking_approval", nullable = false, length = 10)
    private ApprovalMode bookingApproval;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "home_visit_approval", nullable = false, length = 10)
    private ApprovalMode homeVisitApproval;

    @Column(name = "request_expiration_minutes")
    private Integer requestExpirationMinutes;

    @Column(name = "offers_home_visit", nullable = false)
    private boolean offersHomeVisit;

    @Column(name = "home_visit_origin_latitude", precision = 9, scale = 6)
    private BigDecimal homeVisitOriginLatitude;

    @Column(name = "home_visit_origin_longitude", precision = 9, scale = 6)
    private BigDecimal homeVisitOriginLongitude;

    @Column(name = "active", nullable = false)
    private boolean active;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "professional_id", nullable = false)
    @OrderBy("maxDistanceKm")
    private List<HomeVisitFeeEntity> homeVisitFees = new ArrayList<>();

    protected ProfessionalEntity() {
    }

    public ProfessionalEntity(long userId) {
        this.userId = userId;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public ApprovalMode getBookingApproval() {
        return bookingApproval;
    }

    public void setBookingApproval(ApprovalMode bookingApproval) {
        this.bookingApproval = bookingApproval;
    }

    public ApprovalMode getHomeVisitApproval() {
        return homeVisitApproval;
    }

    public void setHomeVisitApproval(ApprovalMode homeVisitApproval) {
        this.homeVisitApproval = homeVisitApproval;
    }

    public Integer getRequestExpirationMinutes() {
        return requestExpirationMinutes;
    }

    public void setRequestExpirationMinutes(Integer requestExpirationMinutes) {
        this.requestExpirationMinutes = requestExpirationMinutes;
    }

    public boolean isOffersHomeVisit() {
        return offersHomeVisit;
    }

    public void setOffersHomeVisit(boolean offersHomeVisit) {
        this.offersHomeVisit = offersHomeVisit;
    }

    public BigDecimal getHomeVisitOriginLatitude() {
        return homeVisitOriginLatitude;
    }

    public void setHomeVisitOriginLatitude(BigDecimal homeVisitOriginLatitude) {
        this.homeVisitOriginLatitude = homeVisitOriginLatitude;
    }

    public BigDecimal getHomeVisitOriginLongitude() {
        return homeVisitOriginLongitude;
    }

    public void setHomeVisitOriginLongitude(BigDecimal homeVisitOriginLongitude) {
        this.homeVisitOriginLongitude = homeVisitOriginLongitude;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<HomeVisitFeeEntity> getHomeVisitFees() {
        return homeVisitFees;
    }
}
