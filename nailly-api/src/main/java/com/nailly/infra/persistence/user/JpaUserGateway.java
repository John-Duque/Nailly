package com.nailly.infra.persistence.user;

import com.nailly.domain.shared.NotFoundException;
import com.nailly.domain.user.User;
import com.nailly.domain.user.UserGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public class JpaUserGateway implements UserGateway {

    private final UserJpaRepository repository;

    public JpaUserGateway(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(JpaUserGateway::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    /** E-mail, senha e papel não mudam depois do cadastro; numa conta existente só o "ativo" é atualizado. */
    @Override
    public User save(User user) {
        UserEntity entity = user.getId() == null
                ? new UserEntity(user.getEmail(), user.getPasswordHash(), user.getRole(), user.isActive())
                : repository.findById(user.getId()).orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
        entity.setActive(user.isActive());
        return toDomain(repository.save(entity));
    }

    private static User toDomain(UserEntity entity) {
        User user = new User(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getRole());
        if (!entity.isActive()) {
            user.deactivate();
        }
        return user;
    }
}
