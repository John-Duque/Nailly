package com.nailly.domain.salonservice;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalonServiceTest {

    private static final ProcedureType GEL = new ProcedureType(2L, "Esmaltação em gel", true);
    private static final Money PRICE = Money.of("80");

    @Test
    @DisplayName("Cria serviço ativo tirando espaços dos textos")
    void createsActiveServiceWithNormalizedTexts() {
        ServiceDetails details = new ServiceDetails(GEL, " Gel nas mãos ", " ", BodyArea.HANDS, 90, PRICE, false);
        SalonService service = new SalonService(5L, 10L, details);

        assertThat(service.getId()).isEqualTo(5L);
        assertThat(service.getProfessionalId()).isEqualTo(10L);
        assertThat(service.isActive()).isTrue();
        assertThat(service.getDetails().getTitle()).isEqualTo("Gel nas mãos");
        assertThat(service.getDetails().getDescription()).isNull();
        assertThat(service.getDetails().isAvailableAtHome()).isFalse();
    }

    @Test
    @DisplayName("Pode ser desativado")
    void canBeDeactivated() {
        SalonService service = new SalonService(5L, 10L,
                new ServiceDetails(GEL, "Gel", null, BodyArea.HANDS, 90, PRICE, true));

        service.deactivate();

        assertThat(service.isActive()).isFalse();
    }

    @Test
    @DisplayName("Atualiza os dados do serviço")
    void updatesDetails() {
        SalonService service = new SalonService(5L, 10L,
                new ServiceDetails(GEL, "Gel", null, BodyArea.HANDS, 90, PRICE, true));
        ServiceDetails newDetails = new ServiceDetails(GEL, "Gel nos pés", "Inclui esfoliação", BodyArea.FEET,
                60, Money.of("70"), false);

        service.update(newDetails);

        assertThat(service.getDetails()).isSameAs(newDetails);
        assertThat(service.getDetails().getProcedureType()).isSameAs(GEL);
        assertThat(service.getDetails().getBodyArea()).isEqualTo(BodyArea.FEET);
        assertThat(service.getDetails().getDurationMinutes()).isEqualTo(60);
        assertThat(service.getDetails().getPrice()).isEqualTo(Money.of("70"));
    }

    @Test
    @DisplayName("Sabe a qual profissional pertence")
    void knowsItsProfessional() {
        SalonService service = new SalonService(5L, 10L,
                new ServiceDetails(GEL, "Gel", null, BodyArea.HANDS, 90, PRICE, true));

        assertThat(service.belongsTo(10L)).isTrue();
        assertThat(service.belongsTo(11L)).isFalse();
    }

    @Test
    @DisplayName("Técnica guarda id, nome e se está ativa")
    void procedureTypeKeepsItsData() {
        assertThat(GEL.getId()).isEqualTo(2L);
        assertThat(GEL.getName()).isEqualTo("Esmaltação em gel");
        assertThat(GEL.isActive()).isTrue();
    }

    @Test
    @DisplayName("Exige os dados do serviço")
    void requiresDetails() {
        assertThatThrownBy(() -> new SalonService(5L, 10L, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe os dados do serviço.");
    }

    @Test
    @DisplayName("Exige a técnica")
    void requiresProcedureType() {
        assertThatThrownBy(() -> new ServiceDetails(null, "Gel", null, BodyArea.HANDS, 90, PRICE, true))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe a técnica do serviço.");
    }

    @Test
    @DisplayName("Exige o título")
    void requiresTitle() {
        assertThatThrownBy(() -> new ServiceDetails(GEL, "", null, BodyArea.HANDS, 90, PRICE, true))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o título do serviço.");
    }

    @Test
    @DisplayName("Exige a região (mãos, pés ou ambos)")
    void requiresBodyArea() {
        assertThatThrownBy(() -> new ServiceDetails(GEL, "Gel", null, null, 90, PRICE, true))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe a região do serviço (mãos, pés ou ambos).");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -30})
    @DisplayName("Exige duração maior que zero")
    void requiresPositiveDuration(int minutes) {
        assertThatThrownBy(() -> new ServiceDetails(GEL, "Gel", null, BodyArea.HANDS, minutes, PRICE, true))
                .isInstanceOf(DomainException.class)
                .hasMessage("A duração do serviço deve ser maior que zero.");
    }

    @Test
    @DisplayName("Limita a duração a 24 horas")
    void limitsDurationToOneDay() {
        assertThat(new ServiceDetails(GEL, "Gel", null, BodyArea.HANDS, 1440, PRICE, true).getDurationMinutes())
                .isEqualTo(ServiceDetails.MAX_DURATION_MINUTES);
        assertThatThrownBy(() -> new ServiceDetails(GEL, "Gel", null, BodyArea.HANDS, 1441, PRICE, true))
                .isInstanceOf(DomainException.class)
                .hasMessage("A duração do serviço deve ser de no máximo 24 horas.");
    }

    @Test
    @DisplayName("Exige o preço")
    void requiresPrice() {
        assertThatThrownBy(() -> new ServiceDetails(GEL, "Gel", null, BodyArea.HANDS, 90, null, true))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o preço do serviço.");
    }

    @Test
    @DisplayName("Técnica exige nome")
    void procedureTypeRequiresName() {
        assertThatThrownBy(() -> new ProcedureType(1L, " ", true))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o nome da técnica.");
    }
}
