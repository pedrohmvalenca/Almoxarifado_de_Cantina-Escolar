package br.senac.almoxarifado.repository;

import br.senac.almoxarifado.model.Movimentacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso ao histórico de movimentações. Registros são apenas inseridos, nunca alterados. */
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
    /** Histórico de um produto, do mais recente ao mais antigo (o id desempata registros no mesmo instante). */
    List<Movimentacao> findByProdutoIdOrderByCriadoEmDescIdDesc(Long produtoId);
}
