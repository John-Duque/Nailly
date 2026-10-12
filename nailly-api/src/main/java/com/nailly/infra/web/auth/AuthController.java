package com.nailly.infra.web.auth;

import com.nailly.application.customer.RegisterCustomerUseCase;
import com.nailly.application.professional.RegisterProfessionalUseCase;
import com.nailly.application.user.AuthenticateUseCase;
import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.infra.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Cadastro e login. Rotas públicas; todas devolvem o token de acesso. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterCustomerUseCase registerCustomer;
    private final RegisterProfessionalUseCase registerProfessional;
    private final AuthenticateUseCase authenticate;
    private final TokenService tokenService;

    public AuthController(RegisterCustomerUseCase registerCustomer, RegisterProfessionalUseCase registerProfessional,
                          AuthenticateUseCase authenticate, TokenService tokenService) {
        this.registerCustomer = registerCustomer;
        this.registerProfessional = registerProfessional;
        this.authenticate = authenticate;
        this.tokenService = tokenService;
    }

    @PostMapping("/register/customer")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        return withToken(registerCustomer.execute(request.getEmail(), request.getPassword(), request.getName(),
                request.getPhone()));
    }

    @PostMapping("/register/professional")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerProfessional(@Valid @RequestBody RegisterProfessionalRequest request) {
        return withToken(registerProfessional.execute(request.getEmail(), request.getPassword(), request.getName(),
                request.getPhone(), request.getBio()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return withToken(authenticate.execute(request.getEmail(), request.getPassword()));
    }

    private AuthResponse withToken(AuthenticatedAccount account) {
        return AuthResponse.of(tokenService.issue(account), account);
    }
}
