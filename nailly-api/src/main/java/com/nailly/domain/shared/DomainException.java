package com.nailly.domain.shared;

/**
 * Violação de uma regra de negócio. A mensagem é exibida ao usuário final;
 * a camada web traduz esta exceção para uma resposta HTTP.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
