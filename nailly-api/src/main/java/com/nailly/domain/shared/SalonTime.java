package com.nailly.domain.shared;

import java.time.ZoneId;

/** Fuso do salão: os horários de funcionamento são em hora local; os agendamentos, em UTC. */
public final class SalonTime {

    public static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private SalonTime() {
    }
}
