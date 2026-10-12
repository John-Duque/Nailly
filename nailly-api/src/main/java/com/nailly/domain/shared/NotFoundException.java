package com.nailly.domain.shared;

/** O registro pedido não existe. A camada web traduz para HTTP 404. */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }
}
