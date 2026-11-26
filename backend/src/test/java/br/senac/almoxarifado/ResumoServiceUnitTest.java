package br.senac.almoxarifado;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.senac.almoxarifado.service.ResumoService;
import br.senac.almoxarifado.web.dto.ResumoResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Indicadores do painel calculados sobre repositórios em memória. */
class ResumoServiceUnitTest {

    private FakeRepositorios.Produtos produtos;
    private FakeRepositorios.Movimentacoes movs;
    private ResumoService service;

    @BeforeEach
    void preparar() {
        produtos = new FakeRepositorios.Produtos();
        movs = new FakeRepositorios.Movimentacoes();
        service = new ResumoService(produtos.repo(), movs.repo());
    }

    @Test
    void semDados_tudoZero() {
        assertEquals(new ResumoResponse(0, 0, 0, 0), service.gerar());
    }

    @Test
    void contaProdutosAbaixoDoMinimoSemEstoqueEMovimentacoes() {
        produtos.adicionar("A", "un", "5", "0");  // abaixo do mínimo e sem estoque
        produtos.adicionar("B", "un", "5", "10"); // ok
        produtos.adicionar("C", "un", "5", "2");  // abaixo do mínimo, com estoque
        produtos.adicionar("D", "un", "0", "0");  // sem estoque, mas mínimo 0 (não está abaixo)
        produtos.adicionar("E", "un", "1", "7");  // ok (3 com saldo contra 2 sem saldo: o teste distingue contas erradas)
        LocalDateTime t = LocalDateTime.of(2026, 3, 10, 8, 0);
        movs.adicionar(2L, "entrada", "10", "compra", t);
        movs.adicionar(2L, "saida", "1", "uso", t);
        movs.adicionar(3L, "entrada", "2", "compra", t);

        ResumoResponse r = service.gerar();

        assertEquals(5, r.totalProdutos());
        assertEquals(2, r.abaixoDoMinimo());
        assertEquals(2, r.semEstoque());
        assertEquals(3, r.totalMovimentacoes());
    }
}
