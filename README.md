# OrderHub - Sistema de Gerenciamento de Pedidos

OrderHub é um sistema completo de gerenciamento de pedidos desenvolvido com Spring Boot, integrando pagamentos via Abacate Pay, processamento assíncrono com Kafka e APIs reativas com WebFlux.

## 🚀 Funcionalidades

### Core Features
- ✅ **Criação de Pedidos**: API REST para criação de pedidos com validação
- ✅ **Integração de Pagamentos**: Integração com Abacate Pay via Feign Client
- ✅ **Processamento Assíncrono**: Eventos Kafka para comunicação entre serviços
- ✅ **APIs Reativas**: Endpoints WebFlux para consultas em tempo real
- ✅ **Painel Administrativo**: Endpoints para gerenciamento e relatórios
- ✅ **Webhooks**: Processamento de webhooks do Abacate Pay

### Recursos Técnicos
- 🔄 **Event-Driven Architecture**: Kafka para eventos de pedidos, pagamentos, estoque e faturas
- 🔒 **Segurança**: Spring Security com autenticação básica para endpoints admin
- 📊 **Monitoramento**: Spring Boot Actuator para métricas e health checks
- 🗄️ **Persistência**: JPA/Hibernate com suporte a H2 (dev) e PostgreSQL (prod)
- 🔍 **Filtros Dinâmicos**: JPA Specifications para consultas complexas
- 📱 **Streaming**: Server-Sent Events para atualizações em tempo real

## 🏗️ Arquitetura

### Estrutura do Projeto
```
src/main/java/com/kipperdev/orderhub/
├── config/          # Configurações (Kafka, Feign, Security, WebFlux)
├── controller/      # Controllers REST e WebFlux
├── dto/            # Data Transfer Objects
├── entity/         # Entidades JPA
├── event/          # Eventos Kafka
├── mapper/         # MapStruct mappers
├── repository/     # Repositórios JPA
├── service/        # Lógica de negócio
├── specification/  # JPA Specifications para filtros
└── OrderhubApplication.java
```

### Fluxo de Pedidos
1. **Criação**: Cliente cria pedido via API REST
2. **Pagamento**: Integração automática com Abacate Pay
3. **Eventos**: Publicação de eventos Kafka
4. **Processamento**: Consumo de eventos de estoque e faturamento
5. **Atualizações**: Webhooks do Abacate Pay atualizam status
6. **Notificações**: Streaming de status em tempo real

## 🛠️ Tecnologias

- **Java 21** - Linguagem principal
- **Spring Boot 3.5.4** - Framework base
- **Spring Data JPA** - Persistência
- **Spring Security** - Segurança
- **Spring WebFlux** - APIs reativas
- **Spring Kafka** - Mensageria
- **Spring Cloud OpenFeign** - Cliente HTTP
- **MapStruct** - Mapeamento de objetos
- **Lombok** - Redução de boilerplate
- **H2/PostgreSQL** - Banco de dados
- **Maven** - Gerenciamento de dependências

## 🚦 Endpoints

### APIs Públicas
```http
# Criar pedido
POST /orders
Content-Type: application/json

# Consultar status do pedido
GET /orders/{id}

# Consultar pedidos por email (reativo)
GET /public/orders/customer/{email}/status

# Stream de status em tempo real
GET /public/orders/{id}/status/stream
Accept: text/event-stream
```

### APIs Administrativas (Autenticação Requerida)
```http
# Listar pedidos com filtros
GET /admin/orders?status=PAID&customerEmail=user@example.com

# Atualizar status do pedido
PUT /admin/orders/{id}/status?status=SHIPPED

# Estatísticas
GET /admin/orders/stats

# Exportar dados
GET /admin/orders/export?format=csv

# Cancelar pedido
POST /admin/orders/{id}/cancel
```

### Webhooks
```http
# Webhook do Abacate Pay
POST /webhook/abacate
Content-Type: application/json
X-Abacate-Signature: {signature}
```

## 📋 Configuração

### Variáveis de Ambiente
```bash
# Banco de Dados (Produção)
DATABASE_URL=jdbc:postgresql://localhost:5432/orderhub
DATABASE_USERNAME=orderhub
DATABASE_PASSWORD=password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Abacate Pay
ABACATEAPI_TOKEN=your-api-token
ABACATEMOCK_ENABLED=false
ABACATEWEBHOOK_SECRET=your-webhook-secret
ABACATEWEBHOOK_SIGNATURE_ENABLED=true

# Admin
ADMIN_USERNAME=admin
ADMIN_PASSWORD=secure-password

# App
APP_BASE_URL=https://your-domain.com
```

### Profiles
- **default**: Desenvolvimento com H2 e mocks habilitados
- **prod**: Produção com PostgreSQL e integrações reais
- **test**: Testes com Kafka embarcado

## 🚀 Execução

### Desenvolvimento
```bash
# Clonar repositório
git clone <repository-url>
cd orderhub

# Executar com Maven
./mvnw spring-boot:run

# Ou com profile específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Docker (Opcional)
```bash
# Build
docker build -t orderhub .

# Run
docker run -p 8080:8080 orderhub
```

### Kafka Local (Desenvolvimento)
```bash
# Usando Docker Compose
docker-compose up -d kafka zookeeper
```

## 📊 Monitoramento

### Health Check
```http
GET /actuator/health
```

### Métricas
```http
GET /actuator/metrics
```

### Console H2 (Desenvolvimento)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:orderhub
User: sa
Password: (vazio)
```

## 🔄 Eventos Kafka

### Tópicos
- `orders.created` - Pedidos criados
- `payments.confirmed` - Pagamentos confirmados
- `stock.reserved` - Estoque reservado
- `invoice.generated` - Faturas geradas
- `*.dlt` - Dead Letter Topics para falhas

### Exemplo de Evento
```json
{
  "orderId": 123,
  "customerEmail": "user@example.com",
  "customerName": "João Silva",
  "totalAmount": 99.90,
  "paymentMethod": "PIX",
  "createdAt": "2024-01-15T10:30:00",
  "items": [
    {
      "productSku": "PROD-001",
      "productName": "Produto Exemplo",
      "quantity": 2,
      "unitPrice": 49.95,
      "totalPrice": 99.90
    }
  ]
}
```

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Testes com profile específico
./mvnw test -Dspring.profiles.active=test
```

## 📝 Exemplo de Uso

### Criar Pedido
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customer": {
      "name": "João Silva",
      "email": "joao@example.com",
      "phone": "+5511999999999"
    },
    "items": [
      {
        "productName": "Produto A",
        "productSku": "PROD-A",
        "quantity": 2,
        "unitPrice": 49.99
      }
    ],
    "paymentMethod": "PIX"
  }'
```

### Consultar Status
```bash
curl http://localhost:8080/orders/1
```

### Stream de Status
```bash
curl -N http://localhost:8080/public/orders/1/status/stream
```

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 📞 Suporte

Para suporte e dúvidas:
- 📧 Email: support@kipperdev.com
- 📱 GitHub Issues: [Criar Issue](https://github.com/kipperdev/orderhub/issues)
- 📖 Documentação: [Wiki do Projeto](https://github.com/kipperdev/orderhub/wiki)