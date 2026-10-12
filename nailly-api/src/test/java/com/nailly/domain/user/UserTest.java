package com.nailly.domain.user;

import com.nailly.domain.shared.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    /** Hash de mentira, só para testar o domínio sem BCrypt. */
    private static final PasswordHasher FAKE_HASHER = new PasswordHasher() {
        @Override
        public String hash(String rawPassword) {
            return "hash:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return passwordHash.equals("hash:" + rawPassword);
        }
    };

    @Test
    @DisplayName("Cadastro guarda só o hash da senha e o e-mail em minúsculas")
    void registerStoresOnlyTheHash() {
        User user = User.register(" Ana@Nailly.COM ", "senha-forte", Role.CUSTOMER, FAKE_HASHER);

        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo("ana@nailly.com");
        assertThat(user.getPasswordHash()).isEqualTo("hash:senha-forte");
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("Confere a senha")
    void checksPassword() {
        User user = User.register("ana@nailly.com", "senha-forte", Role.CUSTOMER, FAKE_HASHER);

        assertThat(user.passwordMatches("senha-forte", FAKE_HASHER)).isTrue();
        assertThat(user.passwordMatches("outra-senha", FAKE_HASHER)).isFalse();
        assertThat(user.passwordMatches(null, FAKE_HASHER)).isFalse();
    }

    @Test
    @DisplayName("Senha precisa de pelo menos 8 caracteres")
    void passwordNeedsMinimumLength() {
        assertThatThrownBy(() -> User.register("ana@nailly.com", "1234567", Role.CUSTOMER, FAKE_HASHER))
                .isInstanceOf(DomainException.class)
                .hasMessage("A senha deve ter pelo menos 8 caracteres.");
        assertThatThrownBy(() -> User.register("ana@nailly.com", null, Role.CUSTOMER, FAKE_HASHER))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Senha não pode passar do limite do BCrypt")
    void passwordCannotExceedBcryptLimit() {
        String accented = "ç".repeat(37);

        assertThatThrownBy(() -> User.register("ana@nailly.com", accented, Role.CUSTOMER, FAKE_HASHER))
                .isInstanceOf(DomainException.class)
                .hasMessage("A senha é longa demais.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sem-arroba", "ana@", "@nailly.com", "ana@nailly", "a na@nailly.com"})
    @DisplayName("Recusa e-mail inválido")
    void rejectsInvalidEmail(String email) {
        assertThatThrownBy(() -> User.normalizeEmail(email))
                .isInstanceOf(DomainException.class)
                .hasMessage("E-mail inválido.");
    }

    @Test
    @DisplayName("Exige o tipo de conta e o hash da senha")
    void requiresRoleAndHash() {
        assertThatThrownBy(() -> new User(1L, "ana@nailly.com", "hash", null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o tipo de conta.");
        assertThatThrownBy(() -> new User(1L, "ana@nailly.com", " ", Role.ADMIN))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Pode ser desativada")
    void canBeDeactivated() {
        User user = new User(1L, "ana@nailly.com", "hash", Role.PROFESSIONAL);

        user.deactivate();

        assertThat(user.isActive()).isFalse();
    }
}
