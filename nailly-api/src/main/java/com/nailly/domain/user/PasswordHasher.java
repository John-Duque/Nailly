package com.nailly.domain.user;

/** Gera e confere o hash das senhas. Implementado na camada infra (BCrypt). */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
