package br.senac.almoxarifado.web.dto;

/** Números gerais do almoxarifado para o painel inicial. */
public record ResumoResponse(long totalProdutos, long abaixoDoMinimo, long semEstoque, long totalMovimentacoes) {
}
