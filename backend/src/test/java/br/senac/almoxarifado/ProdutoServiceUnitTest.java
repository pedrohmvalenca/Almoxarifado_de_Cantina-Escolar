package br.senac.almoxarifado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.senac.almoxarifado.exception.ConflitoException;
import br.senac.almoxarifado.exception.NaoEncontradoException;
import br.senac.almoxarifado.model.Produto;
import br.senac.almoxarifado.service.ProdutoService;
import br.senac.almoxarifado.web.dto.ProdutoRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Regras do ProdutoService testadas com repositório em memória (sem Spring e sem banco). */
class ProdutoServiceUnitTest {

    private FakeRepositorios.Produtos repo;
    private ProdutoService service;

    @BeforeEach
    void preparar() {
        repo = new FakeRepositorios.Produtos();
        service = new ProdutoService(repo.repo());
    }

    @Test
    void criar_removeEspacosDoNomeEIniciaComSaldoZero() {
        Produto p = service.criar(new ProdutoRequest("  Arroz  ", "kg", new BigDecimal("5")));

        assertNotNull(p.getId());
        assertEquals("Arroz", p.getNome());
        assertEquals("kg", p.getUnidade());
        assertEquals(0, new BigDecimal("5").compareTo(p.getEstoqueMinimo()));
        assertEquals(0, BigDecimal.ZERO.compareTo(p.getSaldo()));
        assertEquals(1, repo.dados.size());
    }

    @Test
    void criar_estoqueMinimoOmitidoAssumeZero() {
        Produto p = service.criar(new ProdutoRequest("Sal", "kg", null));
        assertEquals(0, BigDecimal.ZERO.compareTo(p.getEstoqueMinimo()));
    }

    @Test
    void criar_nomeDuplicadoIgnorandoCaixaEEspacos_lancaConflitoENaoSalva() {
        repo.adicionar("Arroz", "kg", "0", "0");

        ConflitoException ex = assertThrows(ConflitoException.class,
                () -> service.criar(new ProdutoRequest("  arroz ", "kg", BigDecimal.ONE)));

        assertEquals("J\u00E1 existe um produto com o nome \"arroz\"", ex.getMessage());
        assertEquals(1, repo.dados.size());
    }

    @Test
    void buscar_produtoExistenteEDevolvido() {
        Produto arroz = repo.adicionar("Arroz", "kg", "0", "0");
        assertSame(arroz, service.buscar(arroz.getId()));
    }

    @Test
    void buscar_produtoInexistente_lancaNaoEncontrado() {
        NaoEncontradoException ex = assertThrows(NaoEncontradoException.class, () -> service.buscar(99L));
        assertEquals("Produto n\u00E3o encontrado", ex.getMessage());
    }

    @Test
    void listar_devolveOrdenadoPorNome() {
        repo.adicionar("Feij\u00E3o", "kg", "0", "0");
        repo.adicionar("Arroz", "kg", "0", "0");
        repo.adicionar("Caf\u00E9", "kg", "0", "0");

        List<String> nomes = service.listar().stream().map(Produto::getNome).toList();

        assertEquals(List.of("Arroz", "Caf\u00E9", "Feij\u00E3o"), nomes);
    }

    @Test
    void abaixoDoMinimo_saldoIgualAoMinimoNaoEntra() {
        repo.adicionar("Abaixo", "un", "5", "4");
        repo.adicionar("Igual", "un", "5", "5");
        repo.adicionar("Acima", "un", "5", "9");

        List<String> nomes = service.abaixoDoMinimo().stream().map(Produto::getNome).toList();

        assertEquals(List.of("Abaixo"), nomes);
    }
}
