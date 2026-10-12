package com.nailly.infra.web.customer;

import com.nailly.application.customer.AddCustomerAddressUseCase;
import com.nailly.application.customer.DeactivateCustomerAddressUseCase;
import com.nailly.application.customer.ListCustomerAddressesUseCase;
import com.nailly.application.customer.UpdateCustomerAddressUseCase;
import com.nailly.domain.customer.CustomerAddress;
import com.nailly.infra.security.CurrentUser;
import com.nailly.infra.web.shared.PlaceRequest;
import com.nailly.infra.web.shared.PlaceResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** Endereços da cliente logada, usados no atendimento a domicílio. */
@RestController
@RequestMapping("/customers/me/addresses")
public class CustomerAddressController {

    private final AddCustomerAddressUseCase addAddress;
    private final UpdateCustomerAddressUseCase updateAddress;
    private final DeactivateCustomerAddressUseCase deactivateAddress;
    private final ListCustomerAddressesUseCase listAddresses;

    public CustomerAddressController(AddCustomerAddressUseCase addAddress, UpdateCustomerAddressUseCase updateAddress,
                                     DeactivateCustomerAddressUseCase deactivateAddress,
                                     ListCustomerAddressesUseCase listAddresses) {
        this.addAddress = addAddress;
        this.updateAddress = updateAddress;
        this.deactivateAddress = deactivateAddress;
        this.listAddresses = listAddresses;
    }

    @GetMapping
    public List<PlaceResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return listAddresses.execute(CurrentUser.of(jwt).customerId()).stream()
                .map(CustomerAddressController::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<PlaceResponse> add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PlaceRequest request) {
        CustomerAddress address = addAddress.execute(CurrentUser.of(jwt).customerId(), request.toPlace());
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(address.getId()).toUri())
                .body(toResponse(address));
    }

    @PutMapping("/{addressId}")
    public PlaceResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long addressId,
                                @Valid @RequestBody PlaceRequest request) {
        return toResponse(updateAddress.execute(CurrentUser.of(jwt).customerId(), addressId, request.toPlace()));
    }

    @PostMapping("/{addressId}/deactivate")
    public PlaceResponse deactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable long addressId) {
        return toResponse(deactivateAddress.execute(CurrentUser.of(jwt).customerId(), addressId));
    }

    private static PlaceResponse toResponse(CustomerAddress address) {
        return PlaceResponse.of(address.getId(), address.getPlace(), address.isActive());
    }
}
