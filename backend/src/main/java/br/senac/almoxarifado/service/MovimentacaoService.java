package br.senac.almoxarifado.service;

import br.senac.almoxarifado.exception.ConflitoException;
import br.senac.almoxarifado.exception.NaoEncontradoException;
import br.senac.almoxarifado.model.Movimentacao;
import br.senac.almoxarifado.repository.MovimentacaoRepository;
import br.senac.almoxarifado.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de negócio das entradas e saídas de estoque. */
@Service
public class MovimentacaoService {

    private final ProdutoRepository produtoRepo;
    private final MovimentacaoRepository movRepo;

    public MovimentacaoService(ProdutoRepository produtoRepo, MovimentacaoRepository movRepo) {
        this.produtoRepo = produtoRepo;
        this.movRepo = movRepo;
    }

    /**
     * Atualiza o saldo e grava a movimentação na MESMA transação.
     * A saída usa UPDATE condicional (saldo >= qtd): se nenhuma linha for afetada,
     * a saída é recusada (409) e nada é gravado. Duas saídas simultâneas não passam do saldo.
     */
    @Transactional
    public Movimentacao registrar(Long produtoId, String tipo, BigDecimal qtd, String motivo) {
        if (!produtoRepo.existsById(produtoId)) {
            throw new NaoEncontradoException("Produto não encontrado");
        }

        int afetadas = Movimentacao.SAIDA.equals(tipo)
                ? produtoRepo.baixarSaldo(produtoId, qtd)
                : produtoRepo.somarSaldo(produtoId, qtd);

        if (afetadas == 0) {
            throw new ConflitoException("Saldo insuficiente para essa saída");
        }

        return movRepo.save(new Movimentacao(produtoId, tipo, qtd, motivo.trim()));
    }

    /**
     * Histórico do produto, da movimentação mais recente para a mais antiga.
     *
     * @throws NaoEncontradoException se o produto não existir
     */
    @Transactional(readOnly = true)
    public List<Movimentacao> historico(Long produtoId) {
        if (!produtoRepo.existsById(produtoId)) {
            throw new NaoEncontradoException("Produto não encontrado");
        }
        return movRepo.findByProdutoIdOrderByCriadoEmDescIdDesc(produtoId);
    }
}
