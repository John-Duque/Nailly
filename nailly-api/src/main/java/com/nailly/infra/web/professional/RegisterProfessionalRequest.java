package com.nailly.infra.web.professional;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Cadastro de profissional. Enquanto não há autenticação, o usuário vem no corpo;
 * com o JWT ele passa a ser o usuário logado.
 */
public class RegisterProfessionalRequest extends ProfessionalProfileRequest {

    @NotNull(message = "Informe o usuário.")
    @Positive(message = "Usuário inválido.")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
