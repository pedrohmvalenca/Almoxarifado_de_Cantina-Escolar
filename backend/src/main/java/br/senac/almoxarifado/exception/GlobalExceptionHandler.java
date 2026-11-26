package br.senac.almoxarifado.exception;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduz exceções em respostas de erro padronizadas, sem expor detalhes internos
 * da aplicação ou do banco de dados.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Corpo de toda resposta de erro da API. */
    public record Erro(int status, String mensagem) {}

    private ResponseEntity<Erro> resposta(HttpStatus status, String msg) {
        return ResponseEntity.status(status).body(new Erro(status.value(), msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> validacao(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return resposta(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Erro> corpoInvalido(Exception ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida");
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(NaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<Erro> conflito(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Última rede de segurança: UNIQUE/CHECK do banco. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> integridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "Operação viola uma regra de integridade do banco");
    }

    /**
     * Qualquer erro não previsto vira 500 com mensagem genérica.
     * O detalhe fica apenas no log do servidor, nunca na resposta ao cliente.
     * Erros do próprio Spring que já têm status (404 de rota, 405 de método etc.) mantêm esse status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Erro> inesperado(Exception ex) {
        if (ex instanceof ErrorResponse er) {
            HttpStatus status = HttpStatus.resolve(er.getStatusCode().value());
            if (status != null && !status.is5xxServerError()) {
                return resposta(status, status.getReasonPhrase());
            }
        }
        log.error("Erro inesperado ao processar a requisição", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }
}
