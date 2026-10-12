package com.nailly.infra.web.professional;

import com.nailly.application.professional.AddProfessionalLocationUseCase;
import com.nailly.application.professional.DeactivateProfessionalLocationUseCase;
import com.nailly.application.professional.ListProfessionalLocationsUseCase;
import com.nailly.application.professional.UpdateProfessionalLocationUseCase;
import com.nailly.domain.professional.ProfessionalLocation;
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

/** Locais de atendimento. Consulta pública; só a própria profissional altera. */
@RestController
@RequestMapping("/professionals/{professionalId}/locations")
public class ProfessionalLocationController {

    private final AddProfessionalLocationUseCase addLocation;
    private final UpdateProfessionalLocationUseCase updateLocation;
    private final DeactivateProfessionalLocationUseCase deactivateLocation;
    private final ListProfessionalLocationsUseCase listLocations;

    public ProfessionalLocationController(AddProfessionalLocationUseCase addLocation,
                                          UpdateProfessionalLocationUseCase updateLocation,
                                          DeactivateProfessionalLocationUseCase deactivateLocation,
                                          ListProfessionalLocationsUseCase listLocations) {
        this.addLocation = addLocation;
        this.updateLocation = updateLocation;
        this.deactivateLocation = deactivateLocation;
        this.listLocations = listLocations;
    }

    @GetMapping
    public List<PlaceResponse> list(@PathVariable long professionalId) {
        return listLocations.execute(professionalId).stream()
                .map(ProfessionalLocationController::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<PlaceResponse> add(@AuthenticationPrincipal Jwt jwt, @PathVariable long professionalId,
                                             @Valid @RequestBody PlaceRequest request) {
        CurrentUser.of(jwt).requireProfessional(professionalId);
        ProfessionalLocation location = addLocation.execute(professionalId, request.toPlace());
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(location.getId()).toUri())
                .body(toResponse(location));
    }

    @PutMapping("/{locationId}")
    public PlaceResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long professionalId,
                                @PathVariable long locationId, @Valid @RequestBody PlaceRequest request) {
        CurrentUser.of(jwt).requireProfessional(professionalId);
        return toResponse(updateLocation.execute(professionalId, locationId, request.toPlace()));
    }

    @PostMapping("/{locationId}/deactivate")
    public PlaceResponse deactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable long professionalId,
                                    @PathVariable long locationId) {
        CurrentUser.of(jwt).requireProfessional(professionalId);
        return toResponse(deactivateLocation.execute(professionalId, locationId));
    }

    private static PlaceResponse toResponse(ProfessionalLocation location) {
        return PlaceResponse.of(location.getId(), location.getPlace(), location.isActive());
    }
}
