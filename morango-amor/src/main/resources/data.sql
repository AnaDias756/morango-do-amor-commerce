-- Inserir dados de exemplo para doces
INSERT INTO doces (nome, descricao, preco, tipo, sabor, peso_gramas, calorias_por_unidade, ingredientes_especiais, tempo_preparo_minutos, disponivel, estoque_atual, estoque_minimo, url_imagem, created_at, updated_at) VALUES
('Morango com Chocolate', 'Delicioso morango fresco coberto com chocolate belga', 8.50, 'CHOCOLATE', 'TRADICIONAL', 50, 120, 'Chocolate belga, morangos frescos', 15, true, 25, 5, 'https://example.com/morango-chocolate.jpg', NOW(), NOW()),
('Morango com Leite Condensado', 'Morango doce com cobertura cremosa de leite condensado', 7.00, 'LEITE_CONDENSADO', 'TRADICIONAL', 45, 95, 'Leite condensado artesanal', 10, true, 30, 5, 'https://example.com/morango-leite.jpg', NOW(), NOW()),
('Morango Gourmet', 'Morango premium com chocolate branco e castanhas', 12.00, 'CHOCOLATE', 'GOURMET', 60, 150, 'Chocolate branco, castanhas, morangos premium', 20, true, 15, 3, 'https://example.com/morango-gourmet.jpg', NOW(), NOW()),
('Morango Diet', 'Morango com cobertura diet sem açúcar', 9.00, 'DIET', 'TRADICIONAL', 40, 60, 'Adoçante natural, sem açúcar', 12, true, 20, 5, 'https://example.com/morango-diet.jpg', NOW(), NOW()),
('Morango com Coco', 'Morango tropical com coco ralado fresco', 8.00, 'COCO', 'TROPICAL', 55, 110, 'Coco fresco ralado', 15, true, 18, 4, 'https://example.com/morango-coco.jpg', NOW(), NOW()),
('Morango Especial', 'Morango com cobertura especial da casa', 10.50, 'ESPECIAL', 'ESPECIAL', 65, 140, 'Receita secreta da casa', 25, true, 12, 3, 'https://example.com/morango-especial.jpg', NOW(), NOW()),
('Morango com Nutella', 'Morango irresistível com Nutella cremosa', 11.00, 'CHOCOLATE', 'GOURMET', 70, 180, 'Nutella original', 18, true, 22, 4, 'https://example.com/morango-nutella.jpg', NOW(), NOW()),
('Morango Fitness', 'Morango saudável com cobertura proteica', 13.00, 'FITNESS', 'TRADICIONAL', 50, 80, 'Whey protein, sem açúcar', 15, true, 10, 2, 'https://example.com/morango-fitness.jpg', NOW(), NOW()),
('Morango com Doce de Leite', 'Morango com doce de leite argentino', 9.50, 'DOCE_LEITE', 'TRADICIONAL', 55, 130, 'Doce de leite argentino', 16, true, 28, 5, 'https://example.com/morango-doce-leite.jpg', NOW(), NOW()),
('Morango Infantil', 'Morango colorido especial para crianças', 6.50, 'INFANTIL', 'TRADICIONAL', 35, 85, 'Confeitos coloridos', 10, true, 35, 8, 'https://example.com/morango-infantil.jpg', NOW(), NOW());

-- Inserir dados de exemplo para clientes
INSERT INTO clientes (nome, email, telefone, data_nascimento, endereco_entrega, cep, cidade, estado, preferencias_doces, alergias, cliente_vip, total_pedidos, created_at, updated_at) VALUES
('Maria Silva', 'maria.silva@email.com', '(11) 99999-1111', '1990-05-15', 'Rua das Flores, 123', '01234-567', 'São Paulo', 'SP', 'Chocolate, Gourmet', '', false, 0, NOW(), NOW()),
('João Santos', 'joao.santos@email.com', '(11) 99999-2222', '1985-08-22', 'Av. Paulista, 456', '01310-100', 'São Paulo', 'SP', 'Tradicional, Diet', 'Lactose', false, 0, NOW(), NOW()),
('Ana Costa', 'ana.costa@email.com', '(11) 99999-3333', '1992-12-03', 'Rua Augusta, 789', '01305-000', 'São Paulo', 'SP', 'Especial, Gourmet', '', true, 15, NOW(), NOW()),
('Pedro Oliveira', 'pedro.oliveira@email.com', '(11) 99999-4444', '1988-03-10', 'Rua Oscar Freire, 321', '01426-001', 'São Paulo', 'SP', 'Chocolate, Coco', 'Nozes', false, 0, NOW(), NOW()),
('Carla Mendes', 'carla.mendes@email.com', '(11) 99999-5555', '1995-07-18', 'Rua Consolação, 654', '01302-000', 'São Paulo', 'SP', 'Fitness, Diet', '', false, 0, NOW(), NOW());

-- Inserir dados de exemplo para pedidos
INSERT INTO pedidos (cliente_id, status, valor_subtotal, valor_desconto, valor_taxa_entrega, valor_final, forma_pagamento, tipo_pagamento, tipo_entrega, endereco_entrega, observacoes, cupom_desconto, id_transacao_pagamento, link_pagamento, tempo_estimado_preparo, tempo_estimado_entrega, data_pedido, data_confirmacao_pagamento, data_inicio_preparo, data_finalizacao, data_entrega, created_at, updated_at) VALUES
(1, 'AGUARDANDO_PAGAMENTO', 25.50, 0.00, 5.00, 30.50, 'PIX', 'ONLINE', 'ENTREGA', 'Rua das Flores, 123 - São Paulo/SP', 'Entregar no portão', '', 'TXN001', 'https://pay.abacatepay.com/link1', 30, 45, NOW(), NULL, NULL, NULL, NULL, NOW(), NOW()),
(2, 'PAGO', 42.00, 4.20, 5.00, 42.80, 'CARTAO_CREDITO', 'ONLINE', 'ENTREGA', 'Av. Paulista, 456 - São Paulo/SP', '', 'DESC10', 'TXN002', 'https://pay.abacatepay.com/link2', 25, 40, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '45 minutes', NULL, NULL, NULL, NOW(), NOW()),
(3, 'EM_PREPARO', 36.00, 0.00, 0.00, 36.00, 'PIX', 'ONLINE', 'RETIRADA', '', 'Cliente VIP - prioridade', '', 'TXN003', '', 20, 0, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour 45 minutes', NOW() - INTERVAL '30 minutes', NULL, NULL, NOW(), NOW());

-- Inserir dados de exemplo para itens de pedidos
INSERT INTO itens_pedido (pedido_id, doce_id, quantidade, preco_unitario, preco_total, observacoes_item, personalizacao) VALUES
(1, 1, 2, 8.50, 17.00, '', ''),
(1, 2, 1, 7.00, 7.00, 'Sem leite condensado extra', ''),
(1, 4, 1, 9.00, 9.00, '', 'Extra chocolate'),
(2, 3, 2, 12.00, 24.00, '', ''),
(2, 7, 1, 11.00, 11.00, '', ''),
(2, 9, 1, 9.50, 9.50, '', 'Doce de leite extra'),
(3, 6, 3, 10.50, 31.50, 'Embalagem especial', 'Decoração personalizada'),
(3, 8, 1, 13.00, 13.00, '', '');

-- Atualizar estoque dos doces baseado nos pedidos
UPDATE doces SET estoque_atual = estoque_atual - 2 WHERE id = 1; -- Morango com Chocolate
UPDATE doces SET estoque_atual = estoque_atual - 1 WHERE id = 2; -- Morango com Leite Condensado
UPDATE doces SET estoque_atual = estoque_atual - 2 WHERE id = 3; -- Morango Gourmet
UPDATE doces SET estoque_atual = estoque_atual - 1 WHERE id = 4; -- Morango Diet
UPDATE doces SET estoque_atual = estoque_atual - 1 WHERE id = 7; -- Morango com Nutella
UPDATE doces SET estoque_atual = estoque_atual - 3 WHERE id = 6; -- Morango Especial
UPDATE doces SET estoque_atual = estoque_atual - 1 WHERE id = 8; -- Morango Fitness
UPDATE doces SET estoque_atual = estoque_atual - 1 WHERE id = 9; -- Morango com Doce de Leite

-- Atualizar total de pedidos dos clientes
UPDATE clientes SET total_pedidos = 1 WHERE id IN (1, 2, 3);

-- Inserir alguns doces com estoque baixo para teste
UPDATE doces SET estoque_atual = 2 WHERE id IN (6, 8); -- Morango Especial e Fitness com estoque baixo