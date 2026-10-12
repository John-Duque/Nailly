package com.nailly.infra.web;

import com.jayway.jsonpath.JsonPath;
import com.nailly.domain.shared.SalonTime;
import com.nailly.infra.persistence.salonservice.ProcedureTypeEntity;
import com.nailly.infra.persistence.salonservice.ProcedureTypeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A API completa, sem simulações (JSON → segurança → controller → caso de uso → JPA no H2):
 * a profissional monta o atendimento, a cliente agenda e cada uma só mexe no que é seu.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiFlowIntegrationTest {

    /** Dia da semana que vem; o atendimento começa às 10h em São Paulo. */
    private static final LocalDate DAY = LocalDate.now(SalonTime.ZONE).plusDays(7);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProcedureTypeJpaRepository procedureTypeRepository;

    private String professionalToken;
    private long professionalId;
    private long locationId;
    private long serviceId;

    private static Instant at(int hour) {
        return DAY.atTime(LocalTime.of(hour, 0)).atZone(SalonTime.ZONE).toInstant();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String token, String body) {
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static String read(ResultActions result, String path) throws Exception {
        return String.valueOf((Object) JsonPath.read(result.andReturn().getResponse().getContentAsString(), path));
    }

    private ResultActions register(String type, String body) throws Exception {
        return mockMvc.perform(json(post("/auth/register/" + type), null, body)).andExpect(status().isCreated());
    }

    private String registerCustomer(String email) throws Exception {
        return read(register("customer", """
                {"email": "%s", "password": "senha-forte", "name": "Bia", "phone": "11999990000"}
                """.formatted(email)), "$.accessToken");
    }

    private String booking(int hour, String place) {
        return """
                {"professionalId": %d, "serviceIds": [%d], "startAt": "%s", %s}
                """.formatted(professionalId, serviceId, at(hour), place);
    }

    @BeforeEach
    void professionalSetsUpHerWork() throws Exception {
        ResultActions registered = register("professional", """
                {"email": "ana@nailly.com", "password": "senha-forte", "name": "Ana"}
                """);
        professionalToken = read(registered, "$.accessToken");
        professionalId = Long.parseLong(read(registered, "$.professionalId"));
        String base = "/professionals/" + professionalId;

        locationId = Long.parseLong(read(mockMvc.perform(json(post(base + "/locations"), professionalToken, """
                {"name": "Estúdio", "street": "Rua A", "number": "10", "neighborhood": "Centro",
                 "city": "São Paulo", "state": "SP", "zipCode": "01000-000"}
                """)).andExpect(status().isCreated()), "$.id"));

        String day = DAY.getDayOfWeek().name();
        mockMvc.perform(json(put(base + "/working-hours"), professionalToken, """
                {"locationId": %d, "periods": [{"dayOfWeek": "%s", "start": "09:00", "end": "18:00"}]}
                """.formatted(locationId, day))).andExpect(status().isOk());
        mockMvc.perform(json(put(base + "/working-hours"), professionalToken, """
                {"periods": [{"dayOfWeek": "%s", "start": "13:00", "end": "17:00"}]}
                """.formatted(day))).andExpect(status().isOk());
        mockMvc.perform(json(put(base + "/home-visit-policy"), professionalToken, """
                {"offered": true, "originLatitude": -23.550520, "originLongitude": -46.633308,
                 "fees": [{"maxDistanceKm": 5, "fee": 10}, {"maxDistanceKm": 15, "fee": 25}]}
                """)).andExpect(status().isOk());

        long procedureTypeId = procedureTypeRepository.save(new ProcedureTypeEntity("Teste gel", true)).getId();
        serviceId = Long.parseLong(read(mockMvc.perform(json(post(base + "/services"), professionalToken, """
                {"procedureTypeId": %d, "title": "Gel nas mãos", "bodyArea": "HANDS",
                 "durationMinutes": 60, "price": 80.50, "availableAtHome": true}
                """.formatted(procedureTypeId))).andExpect(status().isCreated()), "$.id"));
    }

    @Test
    void customerBooksAtTheStudioAndEachSideSeesIt() throws Exception {
        String customer = registerCustomer("bia@nailly.com");
        String atStudio = booking(10, "\"mode\": \"AT_LOCATION\", \"locationId\": " + locationId);

        ResultActions booked = mockMvc.perform(json(post("/appointments"), customer, atStudio))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.location.address").value("Rua A, 10, Centro, São Paulo/SP, 01000-000"))
                .andExpect(jsonPath("$.totalPrice").value(80.5));
        long appointmentId = Long.parseLong(read(booked, "$.id"));

        mockMvc.perform(json(post("/appointments"), customer, atStudio))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Este horário não está mais disponível."));
        mockMvc.perform(json(post("/appointments"), customer, booking(19, "\"mode\": \"AT_LOCATION\", \"locationId\": "
                        + locationId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O horário escolhido está fora do horário de atendimento."));

        String period = "?from=" + at(0) + "&to=" + at(0).plus(1, ChronoUnit.DAYS);
        mockMvc.perform(get("/customers/me/appointments" + period)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem((int) appointmentId)));
        mockMvc.perform(get("/professionals/" + professionalId + "/agenda" + period)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(appointmentId));

        String stranger = registerCustomer("outra@nailly.com");
        mockMvc.perform(get("/appointments/" + appointmentId).header(HttpHeaders.AUTHORIZATION, "Bearer " + stranger))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/appointments/" + appointmentId + "/confirm")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customer))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/appointments/" + appointmentId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void homeVisitRequestWaitsForTheProfessional() throws Exception {
        String customer = registerCustomer("bia@nailly.com");
        long addressId = Long.parseLong(read(mockMvc.perform(json(post("/customers/me/addresses"), customer, """
                {"name": "Casa", "street": "Av. Paulista", "number": "1578", "neighborhood": "Bela Vista",
                 "city": "São Paulo", "state": "SP", "zipCode": "01310-200",
                 "latitude": -23.561414, "longitude": -46.655881}
                """)).andExpect(status().isCreated()), "$.id"));

        ResultActions requested = mockMvc.perform(json(post("/appointments"), customer,
                        booking(14, "\"mode\": \"HOME_VISIT\", \"customerAddressId\": " + addressId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.location.distanceKm").value(2.6))
                .andExpect(jsonPath("$.homeVisitFee").value(10.0))
                .andExpect(jsonPath("$.totalPrice").value(90.5));
        long appointmentId = Long.parseLong(read(requested, "$.id"));

        mockMvc.perform(post("/appointments/" + appointmentId + "/confirm")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void professionalLogsInAgain() throws Exception {
        String token = read(mockMvc.perform(json(post("/auth/login"), null, """
                {"email": "ANA@nailly.com", "password": "senha-forte"}
                """)).andExpect(status().isOk()), "$.accessToken");

        mockMvc.perform(json(put("/professionals/" + professionalId + "/profile"), token, """
                {"name": "Ana Paula"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Paula"));
        mockMvc.perform(json(post("/auth/login"), null, """
                {"email": "ana@nailly.com", "password": "errada-123"}
                """))
                .andExpect(status().isUnauthorized());
    }
}
