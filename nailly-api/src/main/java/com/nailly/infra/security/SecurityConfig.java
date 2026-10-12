package com.nailly.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API protegida por token JWT (Bearer), sem sessão nem cookie.
 * Público: login, cadastro e a consulta do catálogo das profissionais. O resto exige token;
 * quem é dono de cada recurso é conferido nos controllers ({@link CurrentUser}).
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    static final String[] PUBLIC_POSTS = {"/auth/login", "/auth/register/customer", "/auth/register/professional"};
    static final String[] PUBLIC_GETS = {"/procedure-types", "/professionals/*", "/professionals/*/services",
            "/professionals/*/locations", "/professionals/*/working-hours"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ProblemDetailsSecurityHandler errors) {
        return http
                // O CSRF continua ligado. Requisições com token Bearer são liberadas dele pelo próprio
                // resource server, porque o navegador não envia esse cabeçalho sozinho. Só login e
                // cadastro chegam sem token, e eles não usam credencial guardada pelo navegador
                // (cookie), que é o que um ataque CSRF precisaria para agir em nome da usuária.
                .csrf(csrf -> csrf.ignoringRequestMatchers(PUBLIC_POSTS)) // NOSONAR: análise acima
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, PUBLIC_POSTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GETS).permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(rolesFromToken()))
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .build();
    }

    /** Papel do token (claim "role") vira a autoridade ROLE_CUSTOMER, ROLE_PROFESSIONAL ou ROLE_ADMIN. */
    private static JwtAuthenticationConverter rolesFromToken() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(TokenService.ROLE);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
