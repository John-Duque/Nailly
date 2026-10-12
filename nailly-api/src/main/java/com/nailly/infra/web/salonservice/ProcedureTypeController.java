package com.nailly.infra.web.salonservice;

import com.nailly.application.salonservice.ListProcedureTypesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/procedure-types")
public class ProcedureTypeController {

    private final ListProcedureTypesUseCase listProcedureTypes;

    public ProcedureTypeController(ListProcedureTypesUseCase listProcedureTypes) {
        this.listProcedureTypes = listProcedureTypes;
    }

    @GetMapping
    public List<ProcedureTypeResponse> list() {
        return listProcedureTypes.execute().stream().map(ProcedureTypeResponse::from).toList();
    }
}
