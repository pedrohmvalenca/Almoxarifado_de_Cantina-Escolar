package br.senac.almoxarifado.service;

import br.senac.almoxarifado.model.Produto;
import br.senac.almoxarifado.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gera o relatório de estoque em CSV, no padrão que o Excel em português abre direto
 * (separador {@code ;}, decimal com vírgula).
 */
@Service
public class ExportacaoService {

    private static final String CABECALHO = "id;nome;unidade;estoque_minimo;saldo;situacao";

    private final ProdutoRepository repo;

    public ExportacaoService(ProdutoRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public String estoqueCsv() {
        return gerarCsv(repo.listarOrdenado());
    }

    /** Separado do acesso ao banco para poder ser testado isoladamente. */
    public String gerarCsv(List<Produto> produtos) {
        StringBuilder sb = new StringBuilder(CABECALHO).append("\r\n");
        for (Produto p : produtos) {
            boolean abaixo = p.getSaldo().compareTo(p.getEstoqueMinimo()) < 0;
            sb.append(p.getId() == null ? "" : p.getId().toString()).append(';')
              .append(texto(p.getNome())).append(';')
              .append(texto(p.getUnidade())).append(';')
              .append(numero(p.getEstoqueMinimo())).append(';')
              .append(numero(p.getSaldo())).append(';')
              .append(abaixo ? "ABAIXO DO M\u00CDNIMO" : "OK")
              .append("\r\n");
        }
        return sb.toString();
    }

    private static String numero(BigDecimal n) {
        return n.toPlainString().replace('.', ',');
    }

    /**
     * Protege contra "CSV injection": texto que começa com = + - @ vira fórmula no Excel,
     * então recebe um apóstrofo na frente. Também escapa aspas, separador e quebras de linha.
     */
    static String texto(String valor) {
        String v = valor == null ? "" : valor;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
