package br.senac.almoxarifado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.senac.almoxarifado.model.Produto;
import br.senac.almoxarifado.service.ExportacaoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Teste unitário puro: não sobe o Spring nem usa banco de dados,
 * então não interfere nos demais testes do projeto.
 */
class ExportacaoServiceTest {

    // gerarCsv não usa o repositório, por isso pode receber null
    private final ExportacaoService service = new ExportacaoService(null);

    @Test
    void csv_temCabecalhoDecimalComVirgulaESituacao() {
        String csv = service.gerarCsv(List.of(new Produto("Arroz", "kg", new BigDecimal("10.5"))));

        String[] linhas = csv.split("\r\n");
        assertEquals("id;nome;unidade;estoque_minimo;saldo;situacao", linhas[0]);
        assertEquals(";Arroz;kg;10,5;0;ABAIXO DO M\u00CDNIMO", linhas[1]);
    }

    @Test
    void csv_produtoComMinimoZeroEstaOk() {
        String csv = service.gerarCsv(List.of(new Produto("Sal", "kg", BigDecimal.ZERO)));
        assertTrue(csv.contains(";Sal;kg;0;0;OK\r\n"));
    }

    @Test
    void csv_neutralizaFormulas() {
        String csv = service.gerarCsv(List.of(new Produto("=1+1", "un", BigDecimal.ZERO)));
        assertTrue(csv.contains(";'=1+1;"));
    }

    @Test
    void csv_escapaAspasESeparador() {
        String csv = service.gerarCsv(List.of(new Produto("P\u00E3o; \"franc\u00EAs\"", "un", BigDecimal.ZERO)));
        assertTrue(csv.contains("\"P\u00E3o; \"\"franc\u00EAs\"\"\""));
    }

    @Test
    void csv_listaVaziaTrazSoOCabecalho() {
        assertEquals("id;nome;unidade;estoque_minimo;saldo;situacao\r\n", service.gerarCsv(List.of()));
    }
}
