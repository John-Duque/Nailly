package com.nailly.infra.shared.exception;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Traduz as exceções para respostas HTTP no formato Problem Details (RFC 9457),
 * com mensagens em português. Erros padrão do Spring MVC (rota inexistente, método
 * não permitido etc.) continuam tratados pela classe base.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Não encontrado", exception.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException exception) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negócio violada", exception.getMessage());
    }

    /** Chave estrangeira ou única violada, por exemplo um cliente ou local que não existe. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException exception) {
        LOG.warn("Violação de integridade do banco", exception);
        return problem(HttpStatus.CONFLICT, "Dados inconsistentes",
                "Algum registro informado não existe ou já está cadastrado.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        LOG.error("Erro inesperado", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado",
                "Não foi possível concluir a operação. Tente novamente mais tarde.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<InvalidField> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new InvalidField(error.getField(),
                        Objects.requireNonNullElse(error.getDefaultMessage(), "Valor inválido.")))
                .sorted(Comparator.comparing(InvalidField::getField))
                .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Dados inválidos", "Corrija os campos indicados.");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "O corpo da requisição não pôde ser lido. Confira o JSON e os valores enviados."));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException exception, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "O valor '" + exception.getValue() + "' não é válido para '" + exception.getPropertyName() + "'."));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
