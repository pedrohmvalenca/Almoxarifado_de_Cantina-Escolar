package br.senac.almoxarifado;

import br.senac.almoxarifado.model.Movimentacao;
import br.senac.almoxarifado.model.Produto;
import br.senac.almoxarifado.repository.MovimentacaoRepository;
import br.senac.almoxarifado.repository.ProdutoRepository;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Repositórios em memória para testes unitários (sem Spring e sem banco).
 * Usam java.lang.reflect.Proxy, então não dependem de Mockito e seguem o contrato
 * das interfaces reais, inclusive o do UPDATE condicional de saldo.
 * Não é um teste: só é usado pelas classes *UnitTest.
 */
final class FakeRepositorios {

    private FakeRepositorios() {}

    /** Define um campo privado (id, saldo, criadoEm), que as entidades não permitem alterar por setter. */
    static void definir(Object alvo, String campo, Object valor) {
        try {
            Field f = alvo.getClass().getDeclaredField(campo);
            f.setAccessible(true);
            f.set(alvo, valor);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static Produto produto(Long id, String nome, String unidade, String minimo, String saldo) {
        Produto p = new Produto(nome, unidade, new BigDecimal(minimo));
        definir(p, "id", id);
        definir(p, "saldo", new BigDecimal(saldo));
        return p;
    }

    private static Object metodoDeObject(Object proxy, Method m, Object[] a, String nome) {
        switch (m.getName()) {
            case "toString": return nome;
            case "hashCode": return System.identityHashCode(proxy);
            case "equals": return proxy == a[0];
            default: throw new UnsupportedOperationException(nome + " não simula " + m.getName());
        }
    }

    static final class Produtos implements InvocationHandler {
        final List<Produto> dados = new ArrayList<>();
        private long seq = 0;

        ProdutoRepository repo() {
            return (ProdutoRepository) Proxy.newProxyInstance(
                    ProdutoRepository.class.getClassLoader(), new Class<?>[] {ProdutoRepository.class}, this);
        }

        Produto adicionar(String nome, String unidade, String minimo, String saldo) {
            Produto p = produto(++seq, nome, unidade, minimo, saldo);
            dados.add(p);
            return p;
        }

        Produto achar(Long id) {
            return dados.stream().filter(p -> p.getId().equals(id)).findFirst().orElseThrow();
        }

        /** Mesmo contrato do UPDATE condicional: devolve 1 se alterou e 0 se recusou. */
        private int alterar(Long id, BigDecimal qtd, boolean baixa) {
            Produto p = dados.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
            if (p == null) return 0;
            if (baixa && p.getSaldo().compareTo(qtd) < 0) return 0;
            definir(p, "saldo", baixa ? p.getSaldo().subtract(qtd) : p.getSaldo().add(qtd));
            return 1;
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] a) {
            switch (m.getName()) {
                case "existsByNomeIgnoreCase":
                    return dados.stream().anyMatch(p -> p.getNome().equalsIgnoreCase((String) a[0]));
                case "save": {
                    Produto p = (Produto) a[0];
                    if (p.getId() == null) {
                        definir(p, "id", ++seq);
                        dados.add(p);
                    }
                    return p;
                }
                case "findById":
                    return dados.stream().filter(p -> p.getId().equals(a[0])).findFirst();
                case "existsById":
                    return dados.stream().anyMatch(p -> p.getId().equals(a[0]));
                case "count":
                    return (long) dados.size();
                case "listarOrdenado":
                    return dados.stream().sorted(Comparator.comparing(Produto::getNome)).toList();
                case "findAbaixoDoMinimo":
                    return dados.stream()
                            .filter(p -> p.getSaldo().compareTo(p.getEstoqueMinimo()) < 0)
                            .sorted(Comparator.comparing(Produto::getNome)).toList();
                case "baixarSaldo":
                    return alterar((Long) a[0], (BigDecimal) a[1], true);
                case "somarSaldo":
                    return alterar((Long) a[0], (BigDecimal) a[1], false);
                default:
                    return metodoDeObject(proxy, m, a, "FakeProdutoRepository");
            }
        }
    }

    static final class Movimentacoes implements InvocationHandler {
        final List<Movimentacao> dados = new ArrayList<>();
        private long seq = 0;

        MovimentacaoRepository repo() {
            return (MovimentacaoRepository) Proxy.newProxyInstance(
                    MovimentacaoRepository.class.getClassLoader(), new Class<?>[] {MovimentacaoRepository.class}, this);
        }

        Movimentacao adicionar(Long produtoId, String tipo, String qtd, String motivo, LocalDateTime em) {
            Movimentacao m = new Movimentacao(produtoId, tipo, new BigDecimal(qtd), motivo);
            definir(m, "id", ++seq);
            definir(m, "criadoEm", em);
            dados.add(m);
            return m;
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] a) {
            switch (m.getName()) {
                case "save": {
                    Movimentacao mov = (Movimentacao) a[0];
                    if (mov.getId() == null) {
                        definir(mov, "id", ++seq);
                        dados.add(mov);
                    }
                    return mov;
                }
                case "count":
                    return (long) dados.size();
                case "findByProdutoIdOrderByCriadoEmDescIdDesc":
                    return dados.stream()
                            .filter(x -> x.getProdutoId().equals(a[0]))
                            .sorted(Comparator.comparing(Movimentacao::getCriadoEm)
                                    .thenComparing(Movimentacao::getId).reversed())
                            .toList();
                default:
                    return metodoDeObject(proxy, m, a, "FakeMovimentacaoRepository");
            }
        }
    }
}
