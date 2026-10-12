package com.nailly.domain.user;

import java.util.Optional;

/** Acesso às contas de usuário. Implementado na camada infra. */
public interface UserGateway {

    /** @param email já normalizado ({@link User#normalizeEmail}) */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}
