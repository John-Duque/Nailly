package com.nailly.infra.web.salonservice;

import com.nailly.application.salonservice.SalonServiceCommand;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.Money;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Dados de um serviço, usados ao cadastrar e ao editar. */
public class SalonServiceRequest {

    @NotNull(message = "Informe a técnica.")
    @Positive(message = "Técnica inválida.")
    private Long procedureTypeId;

    @NotBlank(message = "Informe o título.")
    @Size(max = 120, message = "O título deve ter no máximo 120 caracteres.")
    private String title;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
    private String description;

    @NotNull(message = "Informe a região (HANDS, FEET ou BOTH).")
    private BodyArea bodyArea;

    @NotNull(message = "Informe a duração em minutos.")
    @Positive(message = "A duração deve ser maior que zero.")
    @Max(value = ServiceDetails.MAX_DURATION_MINUTES, message = "A duração deve ser de no máximo 24 horas.")
    private Integer durationMinutes;

    @NotNull(message = "Informe o preço.")
    @PositiveOrZero(message = "O preço não pode ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "O preço deve ter até 8 dígitos e 2 casas decimais.")
    private BigDecimal price;

    @NotNull(message = "Informe se o serviço é feito a domicílio.")
    private Boolean availableAtHome;

    SalonServiceCommand toCommand() {
        return new SalonServiceCommand(procedureTypeId, title, description, bodyArea, durationMinutes,
                new Money(price), availableAtHome);
    }

    public void setProcedureTypeId(Long procedureTypeId) {
        this.procedureTypeId = procedureTypeId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBodyArea(BodyArea bodyArea) {
        this.bodyArea = bodyArea;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setAvailableAtHome(Boolean availableAtHome) {
        this.availableAtHome = availableAtHome;
    }
}
