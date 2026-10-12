package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.DomainValidation;

public class Professional {

    private final Long id;
    private final long userId;
    private String name;
    private String phone;
    private String bio;
    private ApprovalPolicy approvalPolicy;
    private HomeVisitPolicy homeVisitPolicy;
    private boolean active = true;

    public Professional(Long id, long userId, String name, String phone, String bio,
                        ApprovalPolicy approvalPolicy, HomeVisitPolicy homeVisitPolicy) {
        this.id = id;
        this.userId = userId;
        updateProfile(name, phone, bio);
        changeApprovalPolicy(approvalPolicy);
        changeHomeVisitPolicy(homeVisitPolicy);
    }

    /** Nova profissional, com as regras padrão: aprovação padrão e sem atendimento a domicílio. */
    public static Professional register(long userId, String name, String phone, String bio) {
        return new Professional(null, userId, name, phone, bio, ApprovalPolicy.defaults(),
                HomeVisitPolicy.notOffered());
    }

    public final void updateProfile(String name, String phone, String bio) {
        this.name = DomainValidation.requireText(name, "Informe o nome da profissional.");
        this.phone = DomainValidation.optionalText(phone);
        this.bio = DomainValidation.optionalText(bio);
    }

    public final void changeApprovalPolicy(ApprovalPolicy approvalPolicy) {
        if (approvalPolicy == null) {
            throw new DomainException("Informe as regras de aprovação.");
        }
        this.approvalPolicy = approvalPolicy;
    }

    public final void changeHomeVisitPolicy(HomeVisitPolicy homeVisitPolicy) {
        if (homeVisitPolicy == null) {
            throw new DomainException("Informe as regras de atendimento a domicílio.");
        }
        this.homeVisitPolicy = homeVisitPolicy;
    }

    public void deactivate() {
        active = false;
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

    public ApprovalPolicy getApprovalPolicy() {
        return approvalPolicy;
    }

    public HomeVisitPolicy getHomeVisitPolicy() {
        return homeVisitPolicy;
    }

    public boolean isActive() {
        return active;
    }
}
