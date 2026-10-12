package com.nailly.infra.web.salonservice;

import com.nailly.application.salonservice.CreateSalonServiceUseCase;
import com.nailly.application.salonservice.DeactivateSalonServiceUseCase;
import com.nailly.application.salonservice.ListProcedureTypesUseCase;
import com.nailly.application.salonservice.ListProfessionalServicesUseCase;
import com.nailly.application.salonservice.SalonServiceCommand;
import com.nailly.application.salonservice.UpdateSalonServiceUseCase;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.shared.Money;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.infra.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.nailly.infra.web.WebTestData.GEL;
import static com.nailly.infra.web.WebTestData.PROFESSIONAL_ID;
import static com.nailly.infra.web.WebTestData.SERVICE_ID;
import static com.nailly.infra.web.WebTestData.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SalonServiceController.class, ProcedureTypeController.class})
@Import(SecurityConfig.class)
class SalonServiceControllerTest {

    private static final String VALID_SERVICE = """
            {"procedureTypeId": 2, "title": "Gel nas mãos", "description": "Com cutilagem",
             "bodyArea": "HANDS", "durationMinutes": 60, "price": 80.50, "availableAtHome": true}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateSalonServiceUseCase createService;
    @MockitoBean
    private UpdateSalonServiceUseCase updateService;
    @MockitoBean
    private DeactivateSalonServiceUseCase deactivateService;
    @MockitoBean
    private ListProfessionalServicesUseCase listServices;
    @MockitoBean
    private ListProcedureTypesUseCase listProcedureTypes;

    @Test
    void listsProcedureTypes() throws Exception {
        when(listProcedureTypes.execute()).thenReturn(List.of(GEL));

        mockMvc.perform(get("/procedure-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Esmaltação em gel"));
    }

    @Test
    void listsCatalogOfProfessional() throws Exception {
        when(listServices.execute(PROFESSIONAL_ID)).thenReturn(List.of(service()));

        mockMvc.perform(get("/professionals/{id}/services", PROFESSIONAL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SERVICE_ID))
                .andExpect(jsonPath("$[0].professionalId").value(PROFESSIONAL_ID))
                .andExpect(jsonPath("$[0].procedureType.name").value("Esmaltação em gel"))
                .andExpect(jsonPath("$[0].title").value("Gel nas mãos"))
                .andExpect(jsonPath("$[0].description").value("Com cutilagem"))
                .andExpect(jsonPath("$[0].bodyArea").value("HANDS"))
                .andExpect(jsonPath("$[0].durationMinutes").value(60))
                .andExpect(jsonPath("$[0].price").value(80.5))
                .andExpect(jsonPath("$[0].availableAtHome").value(true))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void createsService() throws Exception {
        when(createService.execute(eq(PROFESSIONAL_ID), any())).thenReturn(service());

        mockMvc.perform(post("/professionals/{id}/services", PROFESSIONAL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_SERVICE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "http://localhost/professionals/" + PROFESSIONAL_ID + "/services/" + SERVICE_ID));

        ArgumentCaptor<SalonServiceCommand> captor = ArgumentCaptor.forClass(SalonServiceCommand.class);
        verify(createService).execute(eq(PROFESSIONAL_ID), captor.capture());
        SalonServiceCommand command = captor.getValue();
        assertThat(command.getProcedureTypeId()).isEqualTo(2L);
        assertThat(command.getTitle()).isEqualTo("Gel nas mãos");
        assertThat(command.getDescription()).isEqualTo("Com cutilagem");
        assertThat(command.getBodyArea()).isEqualTo(BodyArea.HANDS);
        assertThat(command.getDurationMinutes()).isEqualTo(60);
        assertThat(command.getPrice()).isEqualTo(Money.of("80.50"));
        assertThat(command.isAvailableAtHome()).isTrue();
    }

    @Test
    void invalidServiceIsRefused() throws Exception {
        mockMvc.perform(post("/professionals/{id}/services", PROFESSIONAL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"procedureTypeId": 2, "title": "", "bodyArea": "HANDS",
                                 "durationMinutes": 1441, "price": 10.001}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(containsInAnyOrder("availableAtHome", "durationMinutes", "price", "title")));
        verifyNoInteractions(createService);
    }

    @Test
    void updatesService() throws Exception {
        when(updateService.execute(eq(PROFESSIONAL_ID), eq(SERVICE_ID), any())).thenReturn(service());

        mockMvc.perform(put("/professionals/{id}/services/{serviceId}", PROFESSIONAL_ID, SERVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_SERVICE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SERVICE_ID));
    }

    @Test
    void serviceOfAnotherProfessionalIs404() throws Exception {
        when(updateService.execute(eq(PROFESSIONAL_ID), eq(99L), any()))
                .thenThrow(new NotFoundException("Serviço não encontrado."));

        mockMvc.perform(put("/professionals/{id}/services/99", PROFESSIONAL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_SERVICE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Serviço não encontrado."));
    }

    @Test
    void deactivatesService() throws Exception {
        when(deactivateService.execute(PROFESSIONAL_ID, SERVICE_ID)).thenReturn(service());

        mockMvc.perform(post("/professionals/{id}/services/{serviceId}/deactivate", PROFESSIONAL_ID, SERVICE_ID))
                .andExpect(status().isOk());
        verify(deactivateService).execute(PROFESSIONAL_ID, SERVICE_ID);
    }
}
