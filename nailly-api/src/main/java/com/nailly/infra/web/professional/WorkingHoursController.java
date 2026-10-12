package com.nailly.infra.web.professional;

import com.nailly.application.professional.DefineWorkingHoursUseCase;
import com.nailly.application.professional.ListWorkingHoursUseCase;
import com.nailly.infra.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Horário de funcionamento. Consulta pública; só a própria profissional define. */
@RestController
@RequestMapping("/professionals/{professionalId}/working-hours")
public class WorkingHoursController {

    private final DefineWorkingHoursUseCase defineWorkingHours;
    private final ListWorkingHoursUseCase listWorkingHours;

    public WorkingHoursController(DefineWorkingHoursUseCase defineWorkingHours,
                                  ListWorkingHoursUseCase listWorkingHours) {
        this.defineWorkingHours = defineWorkingHours;
        this.listWorkingHours = listWorkingHours;
    }

    @GetMapping
    public List<WorkingHoursResponse> list(@PathVariable long professionalId) {
        return listWorkingHours.execute(professionalId).stream().map(WorkingHoursResponse::from).toList();
    }

    @PutMapping
    public WorkingHoursResponse define(@AuthenticationPrincipal Jwt jwt, @PathVariable long professionalId,
                                       @Valid @RequestBody WorkingHoursRequest request) {
        CurrentUser.of(jwt).requireProfessional(professionalId);
        return WorkingHoursResponse.from(
                defineWorkingHours.execute(professionalId, request.getLocationId(), request.toPeriods()));
    }
}
