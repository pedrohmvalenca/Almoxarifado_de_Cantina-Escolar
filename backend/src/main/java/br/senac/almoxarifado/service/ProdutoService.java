package br.senac.almoxarifado.service;

import br.senac.almoxarifado.exception.ConflitoException;
import br.senac.almoxarifado.exception.NaoEncontradoException;
import br.senac.almoxarifado.model.Produto;
import br.senac.almoxarifado.repository.ProdutoRepository;
import br.senac.almoxarifado.web.dto.ProdutoRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de negócio do cadastro e da consulta de produtos. */
@Service
public class ProdutoService {

    private final ProdutoRepository repo;

    public ProdutoService(ProdutoRepository repo) {
        this.repo = repo;
    }

    /**
     * Cadastra um produto com saldo inicial zero.
     *
     * @throws ConflitoException se já existir produto com o mesmo nome (sem diferenciar maiúsculas)
     */
    @Transactional
    public Produto criar(ProdutoRequest req) {
        String nome = req.nome().trim();
        if (repo.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe um produto com o nome \"" + nome + "\"");
        }
        BigDecimal minimo = req.estoqueMinimo() == null ? BigDecimal.ZERO : req.estoqueMinimo();
        return repo.save(new Produto(nome, req.unidade(), minimo)); // saldo inicial = 0
    }

    /** Lista todos os produtos ordenados por nome. */
    @Transactional(readOnly = true)
    public List<Produto> listar() {
        return repo.listarOrdenado();
    }

    /** @throws NaoEncontradoException se o produto não existir */
    @Transactional(readOnly = true)
    public Produto buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> new NaoEncontradoException("Produto não encontrado"));
    }

    /** Produtos com saldo estritamente menor que o estoque mínimo. */
    @Transactional(readOnly = true)
    public List<Produto> abaixoDoMinimo() {
        return repo.findAbaixoDoMinimo();
    }
}
