package br.senac.almoxarifado.service;

import br.senac.almoxarifado.model.Produto;
import br.senac.almoxarifado.repository.MovimentacaoRepository;
import br.senac.almoxarifado.repository.ProdutoRepository;
import br.senac.almoxarifado.web.dto.ResumoResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Monta os indicadores gerais do estoque reaproveitando os repositórios existentes. */
@Service
public class ResumoService {

    private final ProdutoRepository produtoRepo;
    private final MovimentacaoRepository movRepo;

    public ResumoService(ProdutoRepository produtoRepo, MovimentacaoRepository movRepo) {
        this.produtoRepo = produtoRepo;
        this.movRepo = movRepo;
    }

    @Transactional(readOnly = true)
    public ResumoResponse gerar() {
        List<Produto> produtos = produtoRepo.listarOrdenado();
        long semEstoque = produtos.stream().filter(p -> p.getSaldo().signum() == 0).count();
        long abaixo = produtoRepo.findAbaixoDoMinimo().size();
        return new ResumoResponse(produtos.size(), abaixo, semEstoque, movRepo.count());
    }
}
