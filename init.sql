-- Inicialização do banco de dados OrderHub
-- Este script é executado automaticamente pelo Docker Compose

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Criar schema se não existir
CREATE SCHEMA IF NOT EXISTS orderhub;

-- Definir schema padrão
SET search_path TO orderhub, public;

-- Comentários das tabelas (serão criadas automaticamente pelo Hibernate)
-- Tabela: customers
-- Armazena informações dos clientes

-- Tabela: orders
-- Armazena informações dos pedidos

-- Tabela: order_items
-- Armazena itens dos pedidos

-- Inserir dados de exemplo (opcional)
-- Estes dados serão inseridos apenas se as tabelas existirem

-- Função para inserir dados de exemplo
CREATE OR REPLACE FUNCTION insert_sample_data()
RETURNS void AS $$
BEGIN
    -- Verificar se a tabela customers existe
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'customers') THEN
        -- Inserir clientes de exemplo
        INSERT INTO customers (id, name, email, phone, created_at, updated_at) VALUES
        (1, 'João Silva', 'joao.silva@email.com', '+5511999999999', NOW(), NOW()),
        (2, 'Maria Santos', 'maria.santos@email.com', '+5511888888888', NOW(), NOW()),
        (3, 'Pedro Oliveira', 'pedro.oliveira@email.com', '+5511777777777', NOW(), NOW())
        ON CONFLICT (email) DO NOTHING;
        
        RAISE NOTICE 'Dados de exemplo inseridos com sucesso!';
    ELSE
        RAISE NOTICE 'Tabelas ainda não foram criadas pelo Hibernate.';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Comentário sobre a execução
-- A função insert_sample_data() pode ser chamada manualmente após a aplicação criar as tabelas:
-- SELECT insert_sample_data();

-- Configurações de performance para desenvolvimento
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_min_duration_statement = 1000;

-- Recarregar configurações
SELECT pg_reload_conf();

RAISE NOTICE 'Banco de dados OrderHub inicializado com sucesso!';
RAISE NOTICE 'Para inserir dados de exemplo, execute: SELECT insert_sample_data();';
RAISE NOTICE 'Aguardando a aplicação Spring Boot criar as tabelas...';