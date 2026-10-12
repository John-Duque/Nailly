package com.nailly.domain.appointment;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.DomainValidation;
import com.nailly.domain.shared.Money;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Agendamento de um ou mais serviços (combo) da mesma profissional.
 * Datas em UTC. O domínio não lê o relógio: o instante atual chega como {@code now}.
 * Conflito de horário e horário de funcionamento são verificados nos casos de uso.
 */
public class Appointment {

    private final Long id;
    private final long customerId;
    private final long professionalId;
    private final Instant startAt;
    private final Instant endAt;
    private final Instant expiresAt;
    private final ServiceLocation location;
    private final List<AppointmentItem> items;
    private final Money homeVisitFee;
    private final Money itemsTotal;
    private final Money totalPrice;
    private final String notes;
    private final Instant createdAt;
    private AppointmentStatus status;
    private String statusReason;
    private Instant decidedAt;
    private Instant canceledAt;

    private Appointment(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customerId;
        this.professionalId = builder.professionalId;
        this.startAt = builder.startAt;
        this.endAt = builder.endAt;
        this.status = builder.status;
        this.expiresAt = builder.expiresAt;
        this.location = builder.location;
        this.items = List.copyOf(builder.items);
        this.homeVisitFee = builder.homeVisitFee;
        this.itemsTotal = builder.itemsTotal;
        this.totalPrice = builder.totalPrice;
        this.notes = builder.notes;
        this.statusReason = builder.statusReason;
        this.decidedAt = builder.decidedAt;
        this.canceledAt = builder.canceledAt;
        this.createdAt = builder.createdAt;
    }

    /**
     * Reconstrói um agendamento já gravado (usado pelos mappers da infra), sem revalidar
     * as regras de criação. Novos pedidos usam {@link #request}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Novo pedido de agendamento feito em {@code now}. */
    public static Appointment request(long customerId, Professional professional, List<SalonService> services,
                                      Instant startAt, ServiceLocation location, String notes, Instant now) {
        long professionalId = Objects.requireNonNull(professional.getId(), "professional.id");
        if (!professional.isActive()) {
            throw new DomainException("Esta profissional não está recebendo agendamentos.");
        }
        if (!startAt.isAfter(now)) {
            throw new DomainException("Escolha um horário no futuro.");
        }
        requireBookableServices(professionalId, services);

        List<AppointmentItem> items = services.stream().map(AppointmentItem::of).toList();
        int totalMinutes = items.stream().mapToInt(AppointmentItem::getDurationMinutes).sum();
        Money itemsTotal = items.stream().map(AppointmentItem::getPrice).reduce(Money.ZERO, Money::plus);
        Money homeVisitFee = location.isHomeVisit()
                ? homeVisitFee(professional.getHomeVisitPolicy(), services, location)
                : Money.ZERO;

        ApprovalPolicy approval = professional.getApprovalPolicy();
        ApprovalMode mode = location.isHomeVisit() ? approval.getHomeVisitApproval() : approval.getBookingApproval();
        boolean automatic = mode == ApprovalMode.AUTOMATIC;

        return builder()
                .customerId(customerId)
                .professionalId(professionalId)
                .startAt(startAt)
                .endAt(startAt.plus(Duration.ofMinutes(totalMinutes)))
                .status(automatic ? AppointmentStatus.CONFIRMED : AppointmentStatus.PENDING)
                .expiresAt(automatic ? null : approval.expiresAt(now, startAt).orElse(null))
                .location(location)
                .items(items)
                .homeVisitFee(homeVisitFee)
                .itemsTotal(itemsTotal)
                .totalPrice(itemsTotal.plus(homeVisitFee))
                .notes(DomainValidation.optionalText(notes))
                .decidedAt(automatic ? now : null)
                .createdAt(now)
                .build();
    }

    private static void requireBookableServices(long professionalId, List<SalonService> services) {
        if (services.isEmpty()) {
            throw new DomainException("Escolha ao menos um serviço.");
        }
        Set<Long> serviceIds = new HashSet<>();
        for (SalonService service : services) {
            String title = service.getDetails().getTitle();
            if (!service.belongsTo(professionalId)) {
                throw new DomainException("Todos os serviços devem ser da mesma profissional.");
            }
            if (!service.isActive()) {
                throw new DomainException("O serviço " + title + " não está disponível.");
            }
            if (!serviceIds.add(service.getId())) {
                throw new DomainException("O serviço " + title + " foi escolhido mais de uma vez.");
            }
        }
    }

    private static Money homeVisitFee(HomeVisitPolicy policy, List<SalonService> services,
                                      ServiceLocation location) {
        if (!policy.isOffered()) {
            throw new DomainException("Esta profissional não atende a domicílio.");
        }
        for (SalonService service : services) {
            if (!service.getDetails().isAvailableAtHome()) {
                throw new DomainException("O serviço " + service.getDetails().getTitle()
                        + " não é feito a domicílio.");
            }
        }
        return policy.feeFor(location.getDistanceKm())
                .orElseThrow(() -> new DomainException("O endereço está fora da área de atendimento a domicílio."));
    }

    public void confirm(Instant now) {
        requirePending("Só é possível confirmar um pedido pendente.");
        if (isExpiredAt(now)) {
            throw new DomainException("O prazo para responder este pedido acabou.");
        }
        status = AppointmentStatus.CONFIRMED;
        decidedAt = now;
    }

    public void reject(String reason, Instant now) {
        requirePending("Só é possível recusar um pedido pendente.");
        status = AppointmentStatus.REJECTED;
        statusReason = DomainValidation.optionalText(reason);
        decidedAt = now;
    }

    /** Chamado pela rotina que encerra os pedidos que a profissional não respondeu a tempo. */
    public void expire(Instant now) {
        requirePending("Só um pedido pendente pode expirar.");
        if (!isExpiredAt(now)) {
            throw new DomainException("O pedido ainda está dentro do prazo.");
        }
        status = AppointmentStatus.EXPIRED;
    }

    public void cancel(String reason, Instant now) {
        if (!status.occupiesSchedule()) {
            throw new DomainException("Este agendamento não pode mais ser cancelado.");
        }
        if (!now.isBefore(startAt)) {
            throw new DomainException("Não é possível cancelar depois do horário marcado.");
        }
        status = AppointmentStatus.CANCELED;
        statusReason = DomainValidation.optionalText(reason);
        canceledAt = now;
    }

    public void complete(Instant now) {
        requireStartedConfirmed(now);
        status = AppointmentStatus.COMPLETED;
    }

    public void markNoShow(Instant now) {
        requireStartedConfirmed(now);
        status = AppointmentStatus.NO_SHOW;
    }

    /** Pedido pendente cujo prazo de resposta já passou. */
    public boolean isExpiredAt(Instant now) {
        return status == AppointmentStatus.PENDING && expiresAt != null && !now.isBefore(expiresAt);
    }

    /** Se ocupa a agenda em algum instante do intervalo [start, end). */
    public boolean conflictsWith(Instant start, Instant end) {
        return status.occupiesSchedule() && start.isBefore(endAt) && end.isAfter(startAt);
    }

    private void requirePending(String message) {
        if (status != AppointmentStatus.PENDING) {
            throw new DomainException(message);
        }
    }

    private void requireStartedConfirmed(Instant now) {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new DomainException("Só é possível finalizar um agendamento confirmado.");
        }
        if (now.isBefore(startAt)) {
            throw new DomainException("O atendimento ainda não começou.");
        }
    }

    public Long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public long getProfessionalId() {
        return professionalId;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public ServiceLocation getLocation() {
        return location;
    }

    public List<AppointmentItem> getItems() {
        return items;
    }

    public Money getHomeVisitFee() {
        return homeVisitFee;
    }

    public Money getItemsTotal() {
        return itemsTotal;
    }

    public Money getTotalPrice() {
        return totalPrice;
    }

    public String getNotes() {
        return notes;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Monta um {@link Appointment} campo a campo. */
    public static final class Builder {

        private Long id;
        private long customerId;
        private long professionalId;
        private Instant startAt;
        private Instant endAt;
        private AppointmentStatus status;
        private Instant expiresAt;
        private ServiceLocation location;
        private List<AppointmentItem> items = List.of();
        private Money homeVisitFee = Money.ZERO;
        private Money itemsTotal = Money.ZERO;
        private Money totalPrice = Money.ZERO;
        private String notes;
        private String statusReason;
        private Instant decidedAt;
        private Instant canceledAt;
        private Instant createdAt;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder customerId(long customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder professionalId(long professionalId) {
            this.professionalId = professionalId;
            return this;
        }

        public Builder startAt(Instant startAt) {
            this.startAt = startAt;
            return this;
        }

        public Builder endAt(Instant endAt) {
            this.endAt = endAt;
            return this;
        }

        public Builder status(AppointmentStatus status) {
            this.status = status;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder location(ServiceLocation location) {
            this.location = location;
            return this;
        }

        public Builder items(List<AppointmentItem> items) {
            this.items = items;
            return this;
        }

        public Builder homeVisitFee(Money homeVisitFee) {
            this.homeVisitFee = homeVisitFee;
            return this;
        }

        public Builder itemsTotal(Money itemsTotal) {
            this.itemsTotal = itemsTotal;
            return this;
        }

        public Builder totalPrice(Money totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder statusReason(String statusReason) {
            this.statusReason = statusReason;
            return this;
        }

        public Builder decidedAt(Instant decidedAt) {
            this.decidedAt = decidedAt;
            return this;
        }

        public Builder canceledAt(Instant canceledAt) {
            this.canceledAt = canceledAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Appointment build() {
            Objects.requireNonNull(startAt, "startAt");
            Objects.requireNonNull(endAt, "endAt");
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(location, "location");
            return new Appointment(this);
        }
    }
}
