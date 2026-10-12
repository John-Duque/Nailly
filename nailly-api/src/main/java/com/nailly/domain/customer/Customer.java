package com.nailly.domain.customer;

import com.nailly.domain.shared.DomainValidation;

/** Cliente do salão. userId nulo = cliente cadastrada pela profissional, sem conta no app. */
public class Customer {

    private final Long id;
    private final Long userId;
    private String name;
    private String phone;

    public Customer(Long id, Long userId, String name, String phone) {
        this.id = id;
        this.userId = userId;
        updateProfile(name, phone);
    }

    public final void updateProfile(String name, String phone) {
        this.name = DomainValidation.requireText(name, "Informe o nome.");
        this.phone = DomainValidation.requireText(phone, "Informe o telefone.");
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
