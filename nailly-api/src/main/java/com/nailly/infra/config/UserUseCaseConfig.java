package com.nailly.infra.config;

import com.nailly.application.user.AccountRegistration;
import com.nailly.application.user.AuthenticateUseCase;
import com.nailly.domain.customer.CustomerGateway;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.user.PasswordHasher;
import com.nailly.domain.user.UserGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra a criação de conta e o login como beans. */
@Configuration(proxyBeanMethods = false)
public class UserUseCaseConfig {

    @Bean
    public AccountRegistration accountRegistration(UserGateway userGateway, PasswordHasher passwordHasher) {
        return new AccountRegistration(userGateway, passwordHasher);
    }

    @Bean
    public AuthenticateUseCase authenticateUseCase(UserGateway userGateway, PasswordHasher passwordHasher,
                                                   ProfessionalGateway professionalGateway,
                                                   CustomerGateway customerGateway) {
        return new AuthenticateUseCase(userGateway, passwordHasher, professionalGateway, customerGateway);
    }
}
