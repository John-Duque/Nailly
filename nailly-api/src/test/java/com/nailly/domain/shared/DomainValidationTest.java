package com.nailly.domain.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainValidationTest {

    @Test
    void requireTextStripsSpaces() {
        assertThat(DomainValidation.requireText("  Ana  ", "erro")).isEqualTo("Ana");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void requireTextRejectsBlank(String value) {
        assertThatThrownBy(() -> DomainValidation.requireText(value, "Informe o nome."))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o nome.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void optionalTextTurnsBlankIntoNull(String value) {
        assertThat(DomainValidation.optionalText(value)).isNull();
    }

    @Test
    void optionalTextStripsSpaces() {
        assertThat(DomainValidation.optionalText(" obs ")).isEqualTo("obs");
    }
}
