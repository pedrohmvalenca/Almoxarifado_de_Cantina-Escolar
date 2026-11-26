package br.senac.almoxarifado.web.dto;

import br.senac.almoxarifado.model.Produto;
import java.math.BigDecimal;

/** Produto como exposto pela API, com o saldo atual. */
public record ProdutoResponse(Long id, String nome, String unidade, BigDecimal estoqueMinimo, BigDecimal saldo) {
    public static ProdutoResponse de(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getUnidade(), p.getEstoqueMinimo(), p.getSaldo());
    }
}
