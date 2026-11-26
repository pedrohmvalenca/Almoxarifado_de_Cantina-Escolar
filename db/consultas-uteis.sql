-- Consultas úteis para o responsável pelo almoxarifado (somente leitura).
-- docker exec -i almoxarifado-db psql -U postgres -d almoxarifado < db/consultas-uteis.sql

-- 1) Produtos sem saldo
SELECT nome, unidade, estoque_minimo FROM produtos WHERE saldo = 0 ORDER BY nome;

-- 2) Produtos que nunca tiveram movimentação
SELECT p.nome
FROM produtos p LEFT JOIN movimentacoes m ON m.produto_id = p.id
WHERE m.id IS NULL
ORDER BY p.nome;

-- 3) Saídas dos últimos 30 dias, por produto (do que mais saiu para o que menos saiu)
SELECT p.nome, p.unidade, SUM(m.quantidade) AS total_saidas
FROM movimentacoes m JOIN produtos p ON p.id = m.produto_id
WHERE m.tipo = 'saida' AND m.criado_em >= now() - interval '30 days'
GROUP BY p.id, p.nome, p.unidade
ORDER BY total_saidas DESC;

-- 4) Saídas por motivo (ajuda a achar desperdício e perdas)
SELECT motivo, COUNT(*) AS vezes, SUM(quantidade) AS quantidade_total
FROM movimentacoes
WHERE tipo = 'saida'
GROUP BY motivo
ORDER BY quantidade_total DESC;

-- 5) Dias de cobertura: quantos dias o saldo atual dura, pela média de saída dos últimos 30 dias
--    (dias_de_cobertura vazio = sem saídas no período)
SELECT p.nome, p.saldo,
       ROUND(COALESCE(SUM(m.quantidade), 0) / 30, 3) AS saida_media_por_dia,
       CASE WHEN COALESCE(SUM(m.quantidade), 0) = 0 THEN NULL
            ELSE ROUND(p.saldo / (SUM(m.quantidade) / 30), 1) END AS dias_de_cobertura
FROM produtos p
LEFT JOIN movimentacoes m ON m.produto_id = p.id AND m.tipo = 'saida'
                          AND m.criado_em >= now() - interval '30 days'
GROUP BY p.id, p.nome, p.saldo
ORDER BY dias_de_cobertura NULLS LAST, p.nome;
