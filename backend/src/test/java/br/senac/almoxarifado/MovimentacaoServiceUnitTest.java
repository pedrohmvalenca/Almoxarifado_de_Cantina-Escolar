package br.senac.almoxarifado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.senac.almoxarifado.exception.ConflitoException;
import br.senac.almoxarifado.exception.NaoEncontradoException;
import br.senac.almoxarifado.model.Movimentacao;
import br.senac.almoxarifado.model.Produto;
import br.senac.almoxarifado.service.MovimentacaoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Regras do MovimentacaoService (critérios de aceitação) com repositórios em memória. */
class MovimentacaoServiceUnitTest {

    private FakeRepositorios.Produtos produtos;
    private FakeRepositorios.Movimentacoes movs;
    private MovimentacaoService service;
    private Produto arroz;

    @BeforeEach
    void preparar() {
        produtos = new FakeRepositorios.Produtos();
        movs = new FakeRepositorios.Movimentacoes();
        service = new MovimentacaoService(produtos.repo(), movs.repo());
        arroz = produtos.adicionar("Arroz", "kg", "5", "0");
    }

    private void assertSaldo(String esperado, Produto p) {
        assertEquals(0, new BigDecimal(esperado).compareTo(p.getSaldo()),
                "saldo esperado " + esperado + " mas foi " + p.getSaldo());
    }

    @Test
    void entrada_somaAoSaldoEGravaAMovimentacaoSemEspacosNoMotivo() {
        Movimentacao m = service.registrar(arroz.getId(), "entrada", new BigDecimal("10"), "  compra  ");

        assertNotNull(m.getId());
        assertEquals("entrada", m.getTipo());
        assertEquals("compra", m.getMotivo());
        assertSaldo("10", arroz);
        assertEquals(1, movs.dados.size());
    }

    @Test
    void ca1_entrada10Saida4_saldo6EDuasLinhas() {
        service.registrar(arroz.getId(), "entrada", new BigDecimal("10"), "compra");
        service.registrar(arroz.getId(), "saida", new BigDecimal("4"), "merenda");

        assertSaldo("6", arroz);
        assertEquals(2, movs.dados.size());
    }

    @Test
    void ca2_saidaMaiorQueSaldo_lancaConflitoENadaMuda() {
        service.registrar(arroz.getId(), "entrada", new BigDecimal("3"), "compra");

        ConflitoException ex = assertThrows(ConflitoException.class,
                () -> service.registrar(arroz.getId(), "saida", new BigDecimal("4"), "merenda"));

        assertEquals("Saldo insuficiente para essa sa\u00EDda", ex.getMessage());
        assertSaldo("3", arroz);
        assertEquals(1, movs.dados.size(), "a saída recusada não pode ser gravada");
    }

    @Test
    void saidaIgualAoSaldo_zeraOEstoque() {
        service.registrar(arroz.getId(), "entrada", new BigDecimal("3"), "compra");
        service.registrar(arroz.getId(), "saida", new BigDecimal("3"), "merenda");
        assertSaldo("0", arroz);
        assertEquals(2, movs.dados.size());
    }

    @Test
    void saidaComEstoqueZerado_ehRecusada() {
        assertThrows(ConflitoException.class,
                () -> service.registrar(arroz.getId(), "saida", new BigDecimal("0.001"), "teste"));
        assertTrue(movs.dados.isEmpty());
    }

    @Test
    void registrar_produtoInexistente_lancaNaoEncontradoENadaEGravado() {
        NaoEncontradoException ex = assertThrows(NaoEncontradoException.class,
                () -> service.registrar(99L, "entrada", BigDecimal.ONE, "x"));

        assertEquals("Produto n\u00E3o encontrado", ex.getMessage());
        assertTrue(movs.dados.isEmpty());
    }

    @Test
    void historico_ordenaDoMaisRecenteParaOMaisAntigoEDesempataPorIdDecrescente() {
        LocalDateTime t0 = LocalDateTime.of(2026, 3, 10, 8, 0);
        Movimentacao m1 = movs.adicionar(arroz.getId(), "entrada", "1", "a", t0);
        Movimentacao m2 = movs.adicionar(arroz.getId(), "saida", "1", "b", t0.plusMinutes(5));
        Movimentacao m3 = movs.adicionar(arroz.getId(), "entrada", "1", "c", t0.plusMinutes(5));

        List<Long> ids = service.historico(arroz.getId()).stream().map(Movimentacao::getId).toList();

        assertEquals(List.of(m3.getId(), m2.getId(), m1.getId()), ids);
    }

    @Test
    void historico_naoMisturaMovimentacoesDeOutroProduto() {
        Produto sal = produtos.adicionar("Sal", "kg", "0", "0");
        LocalDateTime t0 = LocalDateTime.of(2026, 3, 10, 8, 0);
        movs.adicionar(arroz.getId(), "entrada", "1", "arroz", t0);
        movs.adicionar(sal.getId(), "entrada", "1", "sal", t0);

        List<Movimentacao> doSal = service.historico(sal.getId());

        assertEquals(1, doSal.size());
        assertEquals("sal", doSal.get(0).getMotivo());
    }

    @Test
    void historico_produtoSemMovimentacaoDevolveListaVazia() {
        assertTrue(service.historico(arroz.getId()).isEmpty());
    }

    @Test
    void historico_produtoInexistente_lancaNaoEncontrado() {
        assertThrows(NaoEncontradoException.class, () -> service.historico(99L));
    }
}
