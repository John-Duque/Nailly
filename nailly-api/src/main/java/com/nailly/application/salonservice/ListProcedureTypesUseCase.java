package com.nailly.application.salonservice;

import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.ProcedureTypeGateway;

import java.util.List;

/** Técnicas que a profissional pode escolher ao cadastrar um serviço. */
public class ListProcedureTypesUseCase {

    private final ProcedureTypeGateway procedureTypeGateway;

    public ListProcedureTypesUseCase(ProcedureTypeGateway procedureTypeGateway) {
        this.procedureTypeGateway = procedureTypeGateway;
    }

    public List<ProcedureType> execute() {
        return procedureTypeGateway.findAllActive();
    }
}
