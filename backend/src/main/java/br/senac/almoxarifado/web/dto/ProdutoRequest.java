package br.senac.almoxarifado.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Corpo do POST /api/produtos. O estoque mínimo é opcional (padrão 0) e o saldo inicial é sempre 0. */
public record ProdutoRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Pattern(regexp = "un|kg|L", message = "deve ser un, kg ou L") String unidade,
        @PositiveOrZero @Digits(integer = 9, fraction = 3) BigDecimal estoqueMinimo) {
}
