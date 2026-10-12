package com.nailly.infra.web.auth;

import com.jayway.jsonpath.JsonPath;
import com.nailly.application.customer.RegisterCustomerUseCase;
import com.nailly.application.professional.RegisterProfessionalUseCase;
import com.nailly.application.user.AuthenticateUseCase;
import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.user.InvalidCredentialsException;
import com.nailly.domain.user.Role;
import com.nailly.infra.config.ClockConfig;
import com.nailly.infra.security.JwtConfig;
import com.nailly.infra.security.ProblemDetailsSecurityHandler;
import com.nailly.infra.security.SecurityConfig;
import com.nailly.infra.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cadastro e login são públicos (sem token) e devolvem um token válido. */
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
// Segurança real: mesmas regras de acesso e tokens assinados pela mesma chave da aplicação
@Import({SecurityConfig.class, JwtConfig.class, TokenService.class, ProblemDetailsSecurityHandler.class,
        ClockConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private RegisterCustomerUseCase registerCustomer;
    @MockitoBean
    private RegisterProfessionalUseCase registerProfessional;
    @MockitoBean
    private AuthenticateUseCase authenticate;

    private Jwt tokenIn(String json) {
        return jwtDecoder.decode(JsonPath.read(json, "$.accessToken"));
    }

    @Test
    @DisplayName("Cliente se cadastra e recebe token")
    void customerRegistersAndReceivesToken() throws Exception {
        when(registerCustomer.execute("bia@nailly.com", "senha-forte", "Bia", "11999990000"))
                .thenReturn(new AuthenticatedAccount(8L, Role.CUSTOMER, null, 20L));

        String json = mockMvc.perform(post("/auth/register/customer").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "bia@nailly.com", "password": "senha-forte", "name": "Bia",
                                 "phone": "11999990000"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(8))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.customerId").value(20))
                .andExpect(jsonPath("$.professionalId").doesNotExist())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn().getResponse().getContentAsString();

        Jwt token = tokenIn(json);
        assertThat(token.getSubject()).isEqualTo("8");
        assertThat(token.<Long>getClaim("customerId")).isEqualTo(20L);
    }

    @Test
    @DisplayName("Profissional se cadastra e recebe token")
    void professionalRegistersAndReceivesToken() throws Exception {
        when(registerProfessional.execute("ana@nailly.com", "senha-forte", "Ana", null, "Nail designer"))
                .thenReturn(new AuthenticatedAccount(7L, Role.PROFESSIONAL, 10L, null));

        String json = mockMvc.perform(post("/auth/register/professional").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "ana@nailly.com", "password": "senha-forte", "name": "Ana",
                                 "bio": "Nail designer"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("PROFESSIONAL"))
                .andExpect(jsonPath("$.professionalId").value(10))
                .andReturn().getResponse().getContentAsString();

        assertThat(tokenIn(json).getClaimAsString("role")).isEqualTo("PROFESSIONAL");
    }

    @Test
    @DisplayName("Recusa cadastro inválido listando os campos")
    void invalidRegistrationIsRefused() throws Exception {
        mockMvc.perform(post("/auth/register/customer").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "nao-e-email", "password": "curta"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(containsInAnyOrder("email", "name", "password", "phone")));
        verifyNoInteractions(registerCustomer);
    }

    @Test
    @DisplayName("E-mail já cadastrado vira 422")
    void emailAlreadyRegisteredBecomes422() throws Exception {
        when(registerProfessional.execute("ana@nailly.com", "senha-forte", "Ana", null, null))
                .thenThrow(new DomainException("Este e-mail já está cadastrado."));

        mockMvc.perform(post("/auth/register/professional").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "ana@nailly.com", "password": "senha-forte", "name": "Ana"}
                        """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Este e-mail já está cadastrado."));
    }

    @Test
    @DisplayName("Login devolve token")
    void loginReturnsToken() throws Exception {
        when(authenticate.execute("ana@nailly.com", "senha-forte"))
                .thenReturn(new AuthenticatedAccount(7L, Role.PROFESSIONAL, 10L, null));

        String json = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "ana@nailly.com", "password": "senha-forte"}
                        """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(tokenIn(json).<Long>getClaim("professionalId")).isEqualTo(10L);
    }

    @Test
    @DisplayName("E-mail ou senha errados viram 401")
    void wrongCredentialsBecome401() throws Exception {
        when(authenticate.execute("ana@nailly.com", "errada123")).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "ana@nailly.com", "password": "errada123"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Não autenticado"))
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
    }

    @Test
    @DisplayName("Login exige e-mail e senha")
    void loginRequiresEmailAndPassword() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("email", "password")));
        verifyNoInteractions(authenticate);
    }
}
