package br.senac.almoxarifado;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Testes complementares das regras de validação e do formato padronizado de erro.
 * Os critérios de aceitação principais (CA1 a CA5) ficam em {@link CriteriosAceitacaoTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ValidacoesTest {

    private static final String PRODUTOS = "/api/produtos";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    private String nomeUnico() { return "Produto-" + UUID.randomUUID(); }

    private ResultActions postJson(String url, Object corpo) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(corpo)));
    }

    private long criarProduto(String nome) throws Exception {
        String res = postJson(PRODUTOS, Map.of("nome", nome, "unidade", "un", "estoqueMinimo", 0))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(res).get("id").asLong();
    }

    private String urlMov(long produtoId) { return PRODUTOS + "/" + produtoId + "/movimentacoes"; }

    // ---------- Produtos ----------

    @Test
    @DisplayName("Unidade fora de un/kg/L retorna 400")
    void unidadeInvalida_400() throws Exception {
        postJson(PRODUTOS, Map.of("nome", nomeUnico(), "unidade", "g", "estoqueMinimo", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", containsString("unidade")));
    }

    @Test
    @DisplayName("Nome em branco retorna 400")
    void nomeEmBranco_400() throws Exception {
        postJson(PRODUTOS, Map.of("nome", "   ", "unidade", "kg", "estoqueMinimo", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Nome duplicado ignora maiúsculas e espaços nas pontas (409)")
    void nomeDuplicadoIgnorandoCaixaEEspacos_409() throws Exception {
        String nome = nomeUnico();
        criarProduto(nome);
        postJson(PRODUTOS, Map.of("nome", "  " + nome.toUpperCase() + "  ", "unidade", "un", "estoqueMinimo", 0))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("Estoque mínimo omitido assume 0")
    void estoqueMinimoOmitido_assumeZero() throws Exception {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("nome", nomeUnico());
        corpo.put("unidade", "L");
        postJson(PRODUTOS, corpo)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estoqueMinimo", is(0.0), Double.class));
    }

    @Test
    @DisplayName("JSON malformado retorna 400 com mensagem genérica")
    void jsonMalformado_400() throws Exception {
        mvc.perform(post(PRODUTOS).contentType(MediaType.APPLICATION_JSON).content("{ nome: "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Requisição inválida")));
    }

    @Test
    @DisplayName("Id não numérico na URL retorna 400")
    void idNaoNumerico_400() throws Exception {
        mvc.perform(get(PRODUTOS + "/abc")).andExpect(status().isBadRequest());
    }

    // ---------- Movimentações ----------

    @Test
    @DisplayName("Tipo diferente de entrada/saida retorna 400")
    void tipoInvalido_400() throws Exception {
        long id = criarProduto(nomeUnico());
        postJson(urlMov(id), Map.of("tipo", "transferencia", "quantidade", 1, "motivo", "teste"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("tipo")));
    }

    @Test
    @DisplayName("Motivo em branco retorna 400 e não grava histórico")
    void motivoEmBranco_400() throws Exception {
        long id = criarProduto(nomeUnico());
        postJson(urlMov(id), Map.of("tipo", "entrada", "quantidade", 1, "motivo", ""))
                .andExpect(status().isBadRequest());
        mvc.perform(get(urlMov(id))).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Saída exatamente igual ao saldo é aceita e zera o estoque")
    void saidaIgualAoSaldo_zeraEstoque() throws Exception {
        long id = criarProduto(nomeUnico());
        postJson(urlMov(id), Map.of("tipo", "entrada", "quantidade", 5, "motivo", "compra"))
                .andExpect(status().isCreated());
        postJson(urlMov(id), Map.of("tipo", "saida", "quantidade", 5, "motivo", "uso"))
                .andExpect(status().isCreated());
        mvc.perform(get(PRODUTOS + "/" + id)).andExpect(jsonPath("$.saldo", is(0.0), Double.class));
    }

    @Test
    @DisplayName("Histórico de produto inexistente retorna 404")
    void historicoProdutoInexistente_404() throws Exception {
        mvc.perform(get(urlMov(999999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem", is("Produto não encontrado")));
    }
}
