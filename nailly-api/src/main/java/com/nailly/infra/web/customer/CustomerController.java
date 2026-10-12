package com.nailly.infra.web.customer;

import com.nailly.application.appointment.ListCustomerAppointmentsUseCase;
import com.nailly.application.customer.GetCustomerUseCase;
import com.nailly.application.customer.UpdateCustomerProfileUseCase;
import com.nailly.infra.security.CurrentUser;
import com.nailly.infra.web.appointment.AppointmentResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/** Perfil e agendamentos da cliente logada. */
@RestController
@RequestMapping("/customers/me")
public class CustomerController {

    private final GetCustomerUseCase getCustomer;
    private final UpdateCustomerProfileUseCase updateProfile;
    private final ListCustomerAppointmentsUseCase listAppointments;

    public CustomerController(GetCustomerUseCase getCustomer, UpdateCustomerProfileUseCase updateProfile,
                              ListCustomerAppointmentsUseCase listAppointments) {
        this.getCustomer = getCustomer;
        this.updateProfile = updateProfile;
        this.listAppointments = listAppointments;
    }

    @GetMapping
    public CustomerResponse get(@AuthenticationPrincipal Jwt jwt) {
        return CustomerResponse.from(getCustomer.execute(CurrentUser.of(jwt).customerId()));
    }

    @PutMapping
    public CustomerResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CustomerProfileRequest request) {
        return CustomerResponse.from(
                updateProfile.execute(CurrentUser.of(jwt).customerId(), request.getName(), request.getPhone()));
    }

    /** Agendamentos em [from, to), datas em UTC (ISO-8601), no máximo 31 dias. */
    @GetMapping("/appointments")
    public List<AppointmentResponse> appointments(@AuthenticationPrincipal Jwt jwt, @RequestParam Instant from,
                                                  @RequestParam Instant to) {
        return listAppointments.execute(CurrentUser.of(jwt).customerId(), from, to).stream()
                .map(AppointmentResponse::from)
                .toList();
    }
}
