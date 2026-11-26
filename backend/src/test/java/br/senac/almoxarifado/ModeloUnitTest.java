package br.senac.almoxarifado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.senac.almoxarifado.model.Movimentacao;
import br.senac.almoxarifado.model.Produto;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Testes unitários das entidades. Não sobem Spring nem banco. */
class ModeloUnitTest {

    @Test
    void produtoNovo_comecaComSaldoZeroESemId() {
        Produto p = new Produto("Arroz", "kg", new BigDecimal("2.5"));

        assertNull(p.getId());
        assertEquals("Arroz", p.getNome());
        assertEquals("kg", p.getUnidade());
        assertEquals(0, new BigDecimal("2.5").compareTo(p.getEstoqueMinimo()));
        assertEquals(0, BigDecimal.ZERO.compareTo(p.getSaldo()));
    }

    @Test
    void movimentacao_guardaOsDadosEMarcaOHorarioDeCriacao() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);
        Movimentacao m = new Movimentacao(7L, Movimentacao.SAIDA, new BigDecimal("4"), "merenda");
        LocalDateTime depois = LocalDateTime.now().plusSeconds(1);

        assertNull(m.getId());
        assertEquals(7L, m.getProdutoId());
        assertEquals("saida", m.getTipo());
        assertEquals(0, new BigDecimal("4").compareTo(m.getQuantidade()));
        assertEquals("merenda", m.getMotivo());
        assertNotNull(m.getCriadoEm());
        assertTrue(m.getCriadoEm().isAfter(antes) && m.getCriadoEm().isBefore(depois));
    }

    @Test
    void constantesDeTipoSaoOsValoresAceitosPeloBanco() {
        assertEquals("entrada", Movimentacao.ENTRADA);
        assertEquals("saida", Movimentacao.SAIDA);
    }

    @Test
    void entidadesNaoTemSetters_saldoSoMudaPorMovimentacaoEHistoricoEImutavel() {
        for (Class<?> tipo : new Class<?>[] {Produto.class, Movimentacao.class}) {
            boolean temSetter = Arrays.stream(tipo.getDeclaredMethods())
                    .map(Method::getName)
                    .anyMatch(n -> n.startsWith("set"));
            assertFalse(temSetter, tipo.getSimpleName() + " não deveria ter setters");
        }
    }
}
