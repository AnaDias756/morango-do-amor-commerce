# 🍓 Morango do Amor - Sistema de Pedidos

Sistema de gerenciamento de pedidos para a doceria especializada em doces de morango "Morango do Amor".

## 🚀 Tecnologias Utilizadas

- **Java 17** - Linguagem de programação
- **Spring Boot 3.x** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL** - Banco de dados
- **Apache Kafka** - Mensageria e eventos
- **MapStruct** - Mapeamento de objetos
- **Maven** - Gerenciamento de dependências
- **Docker** - Containerização

## 📋 Funcionalidades

### 🛒 Gestão de Pedidos
- Criação de pedidos com múltiplos itens
- Cálculo automático de valores e descontos
- Integração com gateway de pagamento (Abacate Pay)
- Controle de status do pedido
- Estimativa de tempo de preparo e entrega

### 🍰 Catálogo de Doces
- Cadastro de doces com diferentes tipos e sabores
- Controle de estoque em tempo real
- Filtros por tipo, sabor, preço e disponibilidade
- Alertas de estoque baixo

### 👥 Gestão de Clientes
- Cadastro completo de clientes
- Sistema de clientes VIP
- Histórico de pedidos
- Preferências e alergias

### 📊 Painel Administrativo
- Dashboard com métricas de vendas
- Relatórios de pedidos
- Exportação de dados (CSV/JSON)
- Estatísticas de produtos mais vendidos

### 🔔 Sistema de Eventos
- Notificações via Kafka
- Webhooks para atualizações de pagamento
- Eventos de estoque baixo
- Rastreamento de status de pedidos

## 🛠️ Configuração do Ambiente

### Pré-requisitos
- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Apache Kafka (opcional para desenvolvimento)
- Docker e Docker Compose (opcional)

### 1. Configuração do Banco de Dados

```sql
-- Criar banco de dados
CREATE DATABASE morango_amor_db;

-- Criar usuário
CREATE USER morango_user WITH PASSWORD 'morango123';
GRANT ALL PRIVILEGES ON DATABASE morango_amor_db TO morango_user;
```

### 2. Configuração das Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
# Banco de Dados
DB_USERNAME=morango_user
DB_PASSWORD=morango123

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Abacate Pay
ABACATEPAY_URL=https://api.abacatepay.com
ABACATEPAY_API_KEY=sua_api_key_aqui
WEBHOOK_URL=http://localhost:8080/api/webhooks/abacate-pay
MOCK_PAYMENT=true

# Aplicação
SERVER_PORT=8080
LOG_LEVEL=INFO
SHOW_SQL=false
```

### 3. Executar com Docker Compose

```bash
# Subir todos os serviços (PostgreSQL + Kafka + Aplicação)
docker-compose up -d

# Verificar logs
docker-compose logs -f morango-amor
```

### 4. Executar Localmente

```bash
# Compilar o projeto
mvn clean compile

# Executar testes
mvn test

# Executar a aplicação
mvn spring-boot:run
```

## 📚 API Endpoints

### 🛒 Pedidos
- `POST /api/pedidos` - Criar novo pedido
- `GET /api/pedidos/{id}` - Buscar pedido por ID
- `GET /api/pedidos/cliente/{email}` - Buscar pedidos por email do cliente

### 🍰 Doces
- `GET /api/doces` - Listar doces com filtros
- `GET /api/doces/{id}` - Buscar doce por ID
- `GET /api/doces/tipos` - Listar tipos de doces
- `GET /api/doces/sabores` - Listar sabores disponíveis
- `GET /api/doces/promocao` - Doces em promoção
- `GET /api/doces/populares` - Doces mais populares
- `GET /api/doces/estoque-baixo` - Doces com estoque baixo

### 👨‍💼 Administração
- `GET /api/admin/pedidos` - Listar pedidos com filtros
- `PUT /api/admin/pedidos/{id}/status` - Atualizar status do pedido
- `GET /api/admin/pedidos/estatisticas` - Estatísticas de pedidos
- `GET /api/admin/pedidos/dashboard` - Dados para dashboard
- `GET /api/admin/pedidos/exportar` - Exportar pedidos
- `DELETE /api/admin/pedidos/{id}` - Cancelar pedido

### 🔗 Webhooks
- `POST /webhooks/abacate-pay` - Webhook do Abacate Pay
- `POST /webhooks/test/pagamento-aprovado/{pedidoId}` - Simular pagamento aprovado
- `POST /webhooks/test/pagamento-cancelado/{pedidoId}` - Simular pagamento cancelado

## 🧪 Testes

### Executar Testes Unitários
```bash
mvn test
```

### Executar Testes de Integração
```bash
mvn verify
```

### Testar Endpoints com cURL

```bash
# Criar um pedido
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": {
      "nome": "João Silva",
      "email": "joao@email.com",
      "telefone": "(11) 99999-9999"
    },
    "itens": [
      {
        "doceId": 1,
        "quantidade": 2
      }
    ],
    "formaPagamento": "PIX",
    "tipoEntrega": "ENTREGA",
    "enderecoEntrega": "Rua das Flores, 123"
  }'

# Listar doces
curl http://localhost:8080/api/doces

# Buscar doces por tipo
curl "http://localhost:8080/api/doces?tipo=CHOCOLATE"
```

## 📊 Monitoramento

### Health Check
```bash
curl http://localhost:8080/api/actuator/health
```

### Métricas
```bash
curl http://localhost:8080/api/actuator/metrics
```

### Prometheus
```bash
curl http://localhost:8080/api/actuator/prometheus
```

## 🔧 Configurações Avançadas

### Kafka Topics
Os seguintes tópicos são criados automaticamente:
- `pedido-criado` - Eventos de criação de pedidos
- `status-atualizado` - Atualizações de status
- `estoque-baixo` - Alertas de estoque
- `webhook-events` - Eventos de webhooks

### Profiles de Ambiente
- `dev` - Desenvolvimento (logs detalhados, mock habilitado)
- `prod` - Produção (logs otimizados, validações rigorosas)
- `test` - Testes (banco em memória, mocks)

```bash
# Executar com profile específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🚀 Deploy

### Docker
```bash
# Build da imagem
docker build -t morango-amor:latest .

# Executar container
docker run -p 8080:8080 \
  -e DB_USERNAME=morango_user \
  -e DB_PASSWORD=morango123 \
  morango-amor:latest
```

### Kubernetes
```bash
# Aplicar manifests
kubectl apply -f k8s/

# Verificar status
kubectl get pods -l app=morango-amor
```

## 📝 Logs

Os logs são salvos em:
- Console: Formato simplificado
- Arquivo: `logs/morango-amor.log` (formato detalhado)

### Níveis de Log
- `ERROR` - Erros críticos
- `WARN` - Avisos importantes
- `INFO` - Informações gerais
- `DEBUG` - Detalhes de depuração

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 📞 Suporte

Para suporte técnico ou dúvidas:
- Email: suporte@morangodoamor.com.br
- Documentação: [Wiki do Projeto](wiki)
- Issues: [GitHub Issues](issues)

---

**Morango do Amor** - Doces que conquistam corações! 🍓❤️