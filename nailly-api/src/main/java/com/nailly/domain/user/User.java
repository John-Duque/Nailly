package com.nailly.domain.user;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.DomainValidation;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/** Conta de acesso ao app. A senha só existe aqui como hash. */
public class User {

    public static final int MIN_PASSWORD_LENGTH = 8;
    /** O BCrypt só considera os primeiros 72 bytes da senha. */
    public static final int MAX_PASSWORD_BYTES = 72;

    private static final Pattern EMAIL_FORMAT = Pattern.compile("[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+");

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private boolean active = true;

    public User(Long id, String email, String passwordHash, Role role) {
        if (role == null) {
            throw new DomainException("Informe o tipo de conta.");
        }
        this.id = id;
        this.email = normalizeEmail(email);
        this.passwordHash = DomainValidation.requireText(passwordHash, "Senha não informada.");
        this.role = role;
    }

    /** Nova conta: valida a senha e guarda só o hash. */
    public static User register(String email, String rawPassword, Role role, PasswordHasher hasher) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new DomainException("A senha deve ter pelo menos " + MIN_PASSWORD_LENGTH + " caracteres.");
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new DomainException("A senha é longa demais.");
        }
        return new User(null, email, hasher.hash(rawPassword), role);
    }

    /** E-mail sem espaços e em minúsculas, para o login não depender de maiúsculas. */
    public static String normalizeEmail(String email) {
        String value = DomainValidation.requireText(email, "Informe o e-mail.").toLowerCase(Locale.ROOT);
        if (!EMAIL_FORMAT.matcher(value).matches()) {
            throw new DomainException("E-mail inválido.");
        }
        return value;
    }

    public boolean passwordMatches(String rawPassword, PasswordHasher hasher) {
        return rawPassword != null && hasher.matches(rawPassword, passwordHash);
    }

    public void deactivate() {
        active = false;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
