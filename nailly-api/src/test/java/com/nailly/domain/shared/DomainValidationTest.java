package com.nailly.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainValidationTest {

    @Test
    @DisplayName("Texto obrigatório tira os espaços das pontas")
    void requireTextStripsSpaces() {
        assertThat(DomainValidation.requireText("  Ana  ", "erro")).isEqualTo("Ana");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @DisplayName("Texto obrigatório recusa nulo, vazio ou só espaços")
    void requireTextRejectsBlank(String value) {
        assertThatThrownBy(() -> DomainValidation.requireText(value, "Informe o nome."))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o nome.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @DisplayName("Texto opcional em branco vira nulo")
    void optionalTextTurnsBlankIntoNull(String value) {
        assertThat(DomainValidation.optionalText(value)).isNull();
    }

    @Test
    @DisplayName("Texto opcional tira os espaços das pontas")
    void optionalTextStripsSpaces() {
        assertThat(DomainValidation.optionalText(" obs ")).isEqualTo("obs");
    }
}
