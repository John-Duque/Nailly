package com.nailly.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração provisória, até a autenticação JWT existir.
 * Com {@code nailly.security.open-endpoints=true} (só no perfil local) a API fica aberta;
 * em qualquer outro ambiente todas as rotas são negadas.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Value("${nailly.security.open-endpoints:false}")
                                                   boolean openEndpoints) {
        return http
                // API REST sem sessão nem cookie de login: não há o que proteger contra CSRF.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> {
                    if (openEndpoints) {
                        requests.anyRequest().permitAll();
                    } else {
                        requests.anyRequest().denyAll();
                    }
                })
                .build();
    }
}
