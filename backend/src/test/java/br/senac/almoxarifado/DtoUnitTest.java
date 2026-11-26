package br.senac.almoxarifado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import br.senac.almoxarifado.model.Movimentacao;
import br.senac.almoxarifado.model.Produto;
import br.senac.almoxarifado.web.dto.MovimentacaoRequest;
import br.senac.almoxarifado.web.dto.MovimentacaoResponse;
import br.senac.almoxarifado.web.dto.ProdutoRequest;
import br.senac.almoxarifado.web.dto.ProdutoResponse;
import br.senac.almoxarifado.web.dto.ResumoResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Testes unitários da conversão entidade -> DTO e dos records de requisição. */
class DtoUnitTest {

    @Test
    void produtoResponse_copiaTodosOsCamposDoProduto() {
        Produto p = FakeRepositorios.produto(7L, "Arroz", "kg", "5", "3");

        ProdutoResponse r = ProdutoResponse.de(p);

        assertEquals(7L, r.id());
        assertEquals("Arroz", r.nome());
        assertEquals("kg", r.unidade());
        assertEquals(0, new BigDecimal("5").compareTo(r.estoqueMinimo()));
        assertEquals(0, new BigDecimal("3").compareTo(r.saldo()));
    }

    @Test
    void movimentacaoResponse_copiaTodosOsCamposDaMovimentacao() {
        LocalDateTime quando = LocalDateTime.of(2026, 3, 10, 8, 30);
        Movimentacao m = new Movimentacao(7L, "entrada", new BigDecimal("10"), "compra");
        FakeRepositorios.definir(m, "id", 99L);
        FakeRepositorios.definir(m, "criadoEm", quando);

        MovimentacaoResponse r = MovimentacaoResponse.de(m);

        assertEquals(99L, r.id());
        assertEquals(7L, r.produtoId());
        assertEquals("entrada", r.tipo());
        assertEquals(0, new BigDecimal("10").compareTo(r.quantidade()));
        assertEquals("compra", r.motivo());
        assertEquals(quando, r.criadoEm());
    }

    @Test
    void resumoResponse_comparaPorValor() {
        assertEquals(new ResumoResponse(1, 2, 3, 4), new ResumoResponse(1, 2, 3, 4));
        assertNotEquals(new ResumoResponse(1, 2, 3, 4), new ResumoResponse(1, 2, 3, 5));
        ResumoResponse r = new ResumoResponse(10, 2, 1, 30);
        assertEquals(10, r.totalProdutos());
        assertEquals(2, r.abaixoDoMinimo());
        assertEquals(1, r.semEstoque());
        assertEquals(30, r.totalMovimentacoes());
    }

    @Test
    void requisicoes_expoemOsValoresRecebidos() {
        ProdutoRequest pr = new ProdutoRequest("Sal", "kg", new BigDecimal("1"));
        assertEquals("Sal", pr.nome());
        assertEquals("kg", pr.unidade());
        assertEquals(0, BigDecimal.ONE.compareTo(pr.estoqueMinimo()));

        MovimentacaoRequest mr = new MovimentacaoRequest("saida", new BigDecimal("2"), "uso");
        assertEquals("saida", mr.tipo());
        assertEquals(0, new BigDecimal("2").compareTo(mr.quantidade()));
        assertEquals("uso", mr.motivo());
    }
}
