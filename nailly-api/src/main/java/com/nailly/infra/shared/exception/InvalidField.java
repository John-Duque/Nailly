package com.nailly.infra.shared.exception;

/** Campo recusado pela validação da requisição, com a mensagem para o usuário. */
public class InvalidField {

    private final String field;
    private final String message;

    public InvalidField(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
