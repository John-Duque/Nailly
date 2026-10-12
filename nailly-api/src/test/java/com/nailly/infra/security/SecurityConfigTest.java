package com.nailly.infra.security;

import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Regras de acesso com a aplicação inteira: o que é público, o que exige token, e respostas 401. */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void catalogIsPublic() throws Exception {
        mockMvc.perform(get("/procedure-types")).andExpect(status().isOk());
        mockMvc.perform(get("/professionals/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mockMvc.perform(get("/professionals/1/services")).andExpect(status().isOk());
        mockMvc.perform(get("/professionals/1/locations")).andExpect(status().isOk());
        mockMvc.perform(get("/professionals/1/working-hours")).andExpect(status().isOk());
    }

    @Test
    void loginAndRegistrationNeedNoTokenNorCsrf() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register/customer").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register/professional").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void everythingElseRequiresToken() throws Exception {
        mockMvc.perform(get("/customers/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.title").value("Não autenticado"))
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/professionals/1/locations").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidOrExpiredTokenIsRefused() throws Exception {
        TokenService expiredIssuer = new TokenService(jwtEncoder, Duration.ofMinutes(5),
                Clock.fixed(Instant.now().minus(Duration.ofHours(1)), ZoneOffset.UTC));
        String expired = expiredIssuer.issue(new AuthenticatedAccount(1L, Role.CUSTOMER, null, 1L)).getValue();

        mockMvc.perform(get("/customers/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/customers/me").header(HttpHeaders.AUTHORIZATION, "Bearer nao.e.token"))
                .andExpect(status().isUnauthorized());
    }
}
