package br.senac.almoxarifado.exception;

/**
 * Recurso inexistente (por exemplo, produto com id desconhecido).
 * Convertida em HTTP 404 pelo {@link GlobalExceptionHandler}.
 */
public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String msg) { super(msg); }
}
