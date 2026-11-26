package br.senac.almoxarifado;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Teste estático dos arquivos de configuração e das migrations: só lê texto do classpath,
 * sem subir Spring nem banco. Trava decisões de segurança e de integridade do projeto.
 */
class ConfiguracaoEstaticaTest {

    private static String recurso(String caminho) {
        try (InputStream in = ConfiguracaoEstaticaTest.class.getResourceAsStream(caminho)) {
            assertNotNull(in, "arquivo não encontrado no classpath: " + caminho);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String sql(String arquivo) {
        return recurso("/db/migration/" + arquivo).replaceAll("\\s+", " ");
    }

    private static boolean tem(String texto, String regex) {
        return Pattern.compile(regex, Pattern.MULTILINE).matcher(texto).find();
    }

    // ---------- migrations ----------

    @Test
    void migrationV1_criaProdutosComRestricoesDeIntegridade() {
        String v1 = sql("V1__produtos.sql");

        assertTrue(v1.contains("CREATE TABLE produtos"));
        assertTrue(v1.contains("nome VARCHAR(100) NOT NULL UNIQUE"), "nome deve ser único");
        assertTrue(v1.contains("CHECK (estoque_minimo >= 0)"), "estoque mínimo nunca negativo");
        assertTrue(v1.contains("CHECK (saldo >= 0)"), "saldo nunca negativo");
        assertFalse(v1.contains("movimentacoes"), "V1 não deve conhecer movimentações");
    }

    @Test
    void migrationV2_criaMovimentacoesLigadasAProdutosComQuantidadePositiva() {
        String v2 = sql("V2__movimentacoes.sql");

        assertTrue(v2.contains("CREATE TABLE movimentacoes"));
        assertTrue(v2.contains("REFERENCES produtos(id)"), "movimentação precisa de produto existente");
        assertTrue(v2.contains("CHECK (quantidade > 0)"), "quantidade sempre positiva");
        assertTrue(v2.contains("motivo VARCHAR(200) NOT NULL"), "motivo obrigatório (rastreabilidade)");
        assertTrue(v2.contains("CREATE INDEX idx_mov_produto ON movimentacoes (produto_id, criado_em DESC)"),
                "índice do histórico por produto");
    }

    // ---------- application.yml ----------

    @Test
    void hibernateNaoGeraNemAlteraOEsquema_quemManda_eoFlyway() {
        String yml = recurso("/application.yml");

        assertTrue(tem(yml, "^\\s*ddl-auto:\\s*none\\s*$"), "ddl-auto deve ser none");
        assertTrue(Pattern.compile("flyway:\\s*\\n\\s*enabled:\\s*true").matcher(yml).find(),
                "Flyway deve estar habilitado");
    }

    @Test
    void openInViewDesligado() {
        assertTrue(tem(recurso("/application.yml"), "^\\s*open-in-view:\\s*false\\s*$"));
    }

    @Test
    void portaBancoECorsVemDeVariaveisDeAmbiente() {
        String yml = recurso("/application.yml");

        assertTrue(yml.contains("${PORT:"), "porta configurável por PORT");
        assertTrue(yml.contains("${DB_URL:"), "URL do banco configurável por DB_URL");
        assertTrue(yml.contains("${DB_USER:"), "usuário do banco configurável por DB_USER");
        assertTrue(yml.contains("${DB_PASSWORD:"), "senha do banco configurável por DB_PASSWORD");
        assertTrue(yml.contains("${CORS_ORIGINS:"), "origens do CORS configuráveis por CORS_ORIGINS");
    }

    // ---------- application-test.yml ----------

    @Test
    void perfilDeTesteUsaH2EmMemoriaEmModoPostgres() {
        String yml = recurso("/application-test.yml");

        assertTrue(yml.contains("jdbc:h2:mem:"), "testes usam H2 em memória");
        assertTrue(yml.contains("MODE=PostgreSQL"), "H2 em modo PostgreSQL, como em produção");
    }
}
