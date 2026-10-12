package com.nailly.infra.security;

import com.nailly.application.salonservice.ListProcedureTypesUseCase;
import com.nailly.infra.web.salonservice.ProcedureTypeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fora do perfil local (open-endpoints desligado), nenhuma rota é acessível. */
@WebMvcTest(value = ProcedureTypeController.class, properties = "nailly.security.open-endpoints=false")
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListProcedureTypesUseCase listProcedureTypes;

    @Test
    void deniesEveryRouteWhenEndpointsAreClosed() throws Exception {
        mockMvc.perform(get("/procedure-types")).andExpect(status().isForbidden());
        mockMvc.perform(post("/appointments")).andExpect(status().isForbidden());
        verifyNoInteractions(listProcedureTypes);
    }
}
