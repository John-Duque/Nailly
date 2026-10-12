package com.nailly.infra.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Respostas 401 e 403 dos filtros de segurança no mesmo formato (Problem Details) e idioma
 * dos demais erros da API.
 */
@Component
public class ProblemDetailsSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        unauthorized(response);
    }

    /**
     * Requisição com token não passa pelo CSRF (o resource server a libera); então um erro de CSRF
     * significa que o token não foi enviado: responde 401, como numa rota que exige login.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        if (exception instanceof CsrfException) {
            unauthorized(response);
        } else {
            write(response, HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para esta operação.");
        }
    }

    private static void unauthorized(HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        write(response, HttpStatus.UNAUTHORIZED, "Não autenticado",
                "Faça login e envie o token no cabeçalho Authorization (Bearer).");
    }

    /** Textos fixos, sem dado da requisição: não há o que escapar no JSON. */
    private static void write(HttpServletResponse response, HttpStatus status, String title, String detail)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + title + "\",\"status\":"
                + status.value() + ",\"detail\":\"" + detail + "\"}");
    }
}
