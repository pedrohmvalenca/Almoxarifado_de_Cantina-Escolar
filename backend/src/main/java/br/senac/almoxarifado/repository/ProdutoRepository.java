package br.senac.almoxarifado.repository;

import br.senac.almoxarifado.model.Produto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acesso aos produtos. As alterações de saldo são feitas por UPDATE direto no banco
 * para que duas movimentações simultâneas não se sobrescrevam.
 */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /** Usado para impedir nomes duplicados sem diferenciar maiúsculas de minúsculas. */
    boolean existsByNomeIgnoreCase(String nome);

    /** Todos os produtos em ordem alfabética. */
    @Query("SELECT p FROM Produto p ORDER BY p.nome")
    List<Produto> listarOrdenado();

    /** Produtos com saldo estritamente menor que o estoque mínimo, em ordem alfabética. */
    @Query("SELECT p FROM Produto p WHERE p.saldo < p.estoqueMinimo ORDER BY p.nome")
    List<Produto> findAbaixoDoMinimo();

    /** Saída atômica: só altera se o saldo for suficiente. Retorna 0 linhas se recusada. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Produto p SET p.saldo = p.saldo - :qtd WHERE p.id = :id AND p.saldo >= :qtd")
    int baixarSaldo(@Param("id") Long id, @Param("qtd") BigDecimal qtd);

    /** Entrada atômica: soma a quantidade ao saldo. Retorna 0 se o produto não existir. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Produto p SET p.saldo = p.saldo + :qtd WHERE p.id = :id")
    int somarSaldo(@Param("id") Long id, @Param("qtd") BigDecimal qtd);
}
