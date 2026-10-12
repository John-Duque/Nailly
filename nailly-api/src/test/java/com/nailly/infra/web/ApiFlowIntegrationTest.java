package com.nailly.infra.web;

import com.jayway.jsonpath.JsonPath;
import com.nailly.infra.persistence.salonservice.ProcedureTypeEntity;
import com.nailly.infra.persistence.salonservice.ProcedureTypeJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.nailly.infra.persistence.PersistenceTestData.CUSTOMER_ID;
import static com.nailly.infra.persistence.PersistenceTestData.LOCATION_ID;
import static com.nailly.infra.persistence.PersistenceTestData.USER_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A API completa: JSON → controller → caso de uso → JPA, sem simulações. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProcedureTypeJpaRepository procedureTypeRepository;

    private ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static long idOf(ResultActions result) throws Exception {
        return ((Number) JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    void professionalPublishesServiceAndCustomerBooksIt() throws Exception {
        long procedureTypeId = procedureTypeRepository.save(new ProcedureTypeEntity("Teste gel", true)).getId();

        long professionalId = idOf(postJson("/professionals", """
                {"userId": %d, "name": "Ana"}
                """.formatted(USER_ID)).andExpect(status().isCreated()));

        long serviceId = idOf(postJson("/professionals/" + professionalId + "/services", """
                {"procedureTypeId": %d, "title": "Gel nas mãos", "bodyArea": "HANDS",
                 "durationMinutes": 60, "price": 80.50, "availableAtHome": false}
                """.formatted(procedureTypeId)).andExpect(status().isCreated()));

        mockMvc.perform(get("/professionals/" + professionalId + "/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(serviceId));

        String booking = """
                {"customerId": %d, "professionalId": %d, "serviceIds": [%d], "startAt": "%s",
                 "mode": "AT_LOCATION", "locationId": %d, "address": "Rua A, 10"}
                """.formatted(CUSTOMER_ID, professionalId, serviceId,
                Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS), LOCATION_ID);

        long appointmentId = idOf(postJson("/appointments", booking)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalPrice").value(80.5)));

        postJson("/appointments", booking)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Este horário não está mais disponível."));

        mockMvc.perform(post("/appointments/" + appointmentId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/appointments/" + appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void homeVisitIsRefusedWhenProfessionalDoesNotOfferIt() throws Exception {
        long procedureTypeId = procedureTypeRepository.save(new ProcedureTypeEntity("Teste gel", true)).getId();
        long professionalId = idOf(postJson("/professionals", """
                {"userId": %d, "name": "Ana"}
                """.formatted(USER_ID)));
        long serviceId = idOf(postJson("/professionals/" + professionalId + "/services", """
                {"procedureTypeId": %d, "title": "Gel", "bodyArea": "HANDS",
                 "durationMinutes": 60, "price": 50, "availableAtHome": true}
                """.formatted(procedureTypeId)));

        postJson("/appointments", """
                {"customerId": %d, "professionalId": %d, "serviceIds": [%d], "startAt": "2099-01-01T10:00:00Z",
                 "mode": "HOME_VISIT", "customerAddressId": 1, "address": "Rua B, 20",
                 "latitude": -23.56, "longitude": -46.65}
                """.formatted(CUSTOMER_ID, professionalId, serviceId))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Esta profissional não atende a domicílio."));
    }
}
