package com.nailly.domain.user;

import com.nailly.domain.shared.DomainException;

/** Login recusado. A mensagem não diz se o erro foi no e-mail ou na senha. A camada web devolve 401. */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("E-mail ou senha inválidos.");
    }
}
