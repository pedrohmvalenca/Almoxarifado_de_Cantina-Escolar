package br.senac.almoxarifado.exception;

/**
 * Violação de regra de negócio (nome duplicado, saldo insuficiente).
 * Convertida em HTTP 409 pelo {@link GlobalExceptionHandler}.
 */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String msg) { super(msg); }
}
