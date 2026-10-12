package com.nailly.infra.web.customer;

import com.nailly.application.appointment.ListCustomerAppointmentsUseCase;
import com.nailly.application.customer.AddCustomerAddressUseCase;
import com.nailly.application.customer.DeactivateCustomerAddressUseCase;
import com.nailly.application.customer.GetCustomerUseCase;
import com.nailly.application.customer.ListCustomerAddressesUseCase;
import com.nailly.application.customer.UpdateCustomerAddressUseCase;
import com.nailly.application.customer.UpdateCustomerProfileUseCase;
import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.customer.Customer;
import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Place;
import com.nailly.domain.user.Role;
import com.nailly.infra.config.ClockConfig;
import com.nailly.infra.security.JwtConfig;
import com.nailly.infra.security.ProblemDetailsSecurityHandler;
import com.nailly.infra.security.SecurityConfig;
import com.nailly.infra.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;

import static com.nailly.infra.web.WebTestData.appointment;
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

/** Perfil, endereços e agendamentos da cliente logada (/customers/me). */
@WebMvcTest({CustomerController.class, CustomerAddressController.class})
@ActiveProfiles("test")
// Segurança real: mesmas regras de acesso e tokens assinados pela mesma chave da aplicação
@Import({SecurityConfig.class, JwtConfig.class, TokenService.class, ProblemDetailsSecurityHandler.class,
        ClockConfig.class})
class CustomerControllersTest {

    private static final long CUSTOMER_ID = 20L;
    private static final long ADDRESS_ID = 4L;
    private static final String HOME_JSON = """
            {"name": "Casa", "street": "Rua B", "number": "20", "complement": "Apto 3", "neighborhood": "Jardins",
             "city": "São Paulo", "state": "sp", "zipCode": "01400000", "latitude": -23.56, "longitude": -46.65}
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokens;

    @MockitoBean
    private GetCustomerUseCase getCustomer;
    @MockitoBean
    private UpdateCustomerProfileUseCase updateProfile;
    @MockitoBean
    private ListCustomerAppointmentsUseCase listAppointments;
    @MockitoBean
    private AddCustomerAddressUseCase addAddress;
    @MockitoBean
    private UpdateCustomerAddressUseCase updateAddress;
    @MockitoBean
    private DeactivateCustomerAddressUseCase deactivateAddress;
    @MockitoBean
    private ListCustomerAddressesUseCase listAddresses;

    /** Cabeçalho Authorization de uma profissional logada. */
    private String professionalToken(long professionalId) {
        return "Bearer " + tokens.issue(
                new AuthenticatedAccount(100L + professionalId, Role.PROFESSIONAL, professionalId, null)).getValue();
    }

    /** Cabeçalho Authorization de uma cliente logada. */
    private String customerToken(long customerId) {
        return "Bearer " + tokens.issue(
                new AuthenticatedAccount(200L + customerId, Role.CUSTOMER, null, customerId)).getValue();
    }

    private MockHttpServletRequestBuilder asCustomer(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, customerToken(CUSTOMER_ID));
    }

    private static CustomerAddress home() {
        return new CustomerAddress(ADDRESS_ID, CUSTOMER_ID, new Place("Casa",
                new Address("Rua B", "20", "Apto 3", "Jardins", "São Paulo", "SP", "01400-000"),
                new GeoPoint(-23.56, -46.65)));
    }

    @Test
    @DisplayName("Cliente vê o próprio perfil")
    void customerSeesOwnProfile() throws Exception {
        when(getCustomer.execute(CUSTOMER_ID)).thenReturn(new Customer(CUSTOMER_ID, 8L, "Bia", "11999990000"));

        mockMvc.perform(asCustomer(get("/customers/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CUSTOMER_ID))
                .andExpect(jsonPath("$.name").value("Bia"))
                .andExpect(jsonPath("$.phone").value("11999990000"));
    }

    @Test
    @DisplayName("Perfil exige login de cliente")
    void profileRequiresCustomerLogin() throws Exception {
        mockMvc.perform(get("/customers/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/customers/me").header(HttpHeaders.AUTHORIZATION, professionalToken(10L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Acesso permitido apenas para clientes."));
        verifyNoInteractions(getCustomer);
    }

    @Test
    @DisplayName("Cliente atualiza o perfil")
    void customerUpdatesProfile() throws Exception {
        when(updateProfile.execute(CUSTOMER_ID, "Bia Lima", "11988887777"))
                .thenReturn(new Customer(CUSTOMER_ID, 8L, "Bia Lima", "11988887777"));

        mockMvc.perform(asCustomer(put("/customers/me")).contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Bia Lima", "phone": "11988887777"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bia Lima"));
    }

    @Test
    @DisplayName("Cliente lista os próprios agendamentos do período")
    void customerListsOwnAppointmentsOfThePeriod() throws Exception {
        Instant from = Instant.parse("2026-10-01T03:00:00Z");
        Instant to = Instant.parse("2026-10-31T03:00:00Z");
        when(listAppointments.execute(CUSTOMER_ID, from, to)).thenReturn(List.of(appointment(AppointmentStatus.CONFIRMED)));

        mockMvc.perform(asCustomer(get("/customers/me/appointments"))
                        .param("from", from.toString()).param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Cliente cadastra endereço")
    void customerAddsAddress() throws Exception {
        when(addAddress.execute(eq(CUSTOMER_ID), any())).thenReturn(home());

        mockMvc.perform(asCustomer(post("/customers/me/addresses")).contentType(MediaType.APPLICATION_JSON)
                        .content(HOME_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/customers/me/addresses/" + ADDRESS_ID))
                .andExpect(jsonPath("$.id").value(ADDRESS_ID))
                .andExpect(jsonPath("$.name").value("Casa"))
                .andExpect(jsonPath("$.street").value("Rua B"))
                .andExpect(jsonPath("$.number").value("20"))
                .andExpect(jsonPath("$.complement").value("Apto 3"))
                .andExpect(jsonPath("$.neighborhood").value("Jardins"))
                .andExpect(jsonPath("$.city").value("São Paulo"))
                .andExpect(jsonPath("$.state").value("SP"))
                .andExpect(jsonPath("$.zipCode").value("01400-000"))
                .andExpect(jsonPath("$.formatted").value("Rua B, 20 - Apto 3, Jardins, São Paulo/SP, 01400-000"))
                .andExpect(jsonPath("$.latitude").value(-23.56))
                .andExpect(jsonPath("$.longitude").value(-46.65))
                .andExpect(jsonPath("$.active").value(true));

        ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
        verify(addAddress).execute(eq(CUSTOMER_ID), captor.capture());
        assertThat(captor.getValue().getAddress().getState()).isEqualTo("SP");
        assertThat(captor.getValue().getLocation()).isEqualTo(new GeoPoint(-23.56, -46.65));
    }

    @Test
    @DisplayName("Recusa endereço inválido")
    void invalidAddressIsRefused() throws Exception {
        mockMvc.perform(asCustomer(post("/customers/me/addresses")).contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Casa", "street": "Rua B", "number": "20", "neighborhood": "Jardins",
                         "city": "São Paulo", "state": "SPP", "zipCode": "123", "latitude": -23.56}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(containsInAnyOrder("coordinatesComplete", "state", "zipCode")));
        verifyNoInteractions(addAddress);
    }

    @Test
    @DisplayName("Cliente lista, edita e remove os próprios endereços")
    void customerManagesOwnAddresses() throws Exception {
        CustomerAddress removed = home();
        removed.deactivate();
        when(listAddresses.execute(CUSTOMER_ID)).thenReturn(List.of(home()));
        when(updateAddress.execute(eq(CUSTOMER_ID), eq(ADDRESS_ID), any())).thenReturn(home());
        when(deactivateAddress.execute(CUSTOMER_ID, ADDRESS_ID)).thenReturn(removed);

        mockMvc.perform(asCustomer(get("/customers/me/addresses")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ADDRESS_ID));
        mockMvc.perform(asCustomer(put("/customers/me/addresses/{id}", ADDRESS_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(HOME_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(asCustomer(post("/customers/me/addresses/{id}/deactivate", ADDRESS_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
