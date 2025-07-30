# Arquitetura da Aplicação OrderHub

## Diagrama de Arquitetura

```mermaid
graph TB
    %% External Systems
    Client["🧑‍💻 Cliente/Frontend"]
    AbacatePay["💳 AbacatePay Gateway"]
    
    %% OrderHub Application
    subgraph "OrderHub Application"
        %% API Layer
        OrderAPI["📋 Order API\n/api/orders"]
        WebhookAPI["🔗 Webhook API\n/webhook/payment"]
        
        %% Service Layer
        OrderService["⚙️ Order Service"]
        PaymentService["💰 Payment Service"]
        KafkaProducer["📤 Kafka Producer Service"]
        KafkaConsumer["📥 Kafka Consumer Service"]
        
        %% Database
        Database[("🗄️ PostgreSQL\nDatabase")]
    end
    
    %% Kafka Infrastructure
    subgraph "Apache Kafka"
        OrderCreatedTopic["📨 orders.created\nTopic"]
        PaymentConfirmedTopic["✅ payments.confirmed\nTopic"]
        StockReservedTopic["📦 stock.reserved\nTopic"]
        InvoiceGeneratedTopic["🧾 invoice.generated\nTopic"]
    end
    
    %% Other OrderHub Consumers
    subgraph "OrderHub Consumers"
        StockService["📦 Stock Service"]
        InvoiceService["🧾 Invoice Service"]
        NotificationService["📧 Notification Service"]
    end
    
    %% Flow 1: Order Creation
    Client -->|"1. POST /api/orders\n{customer, items}"| OrderAPI
    OrderAPI --> OrderService
    OrderService -->|"2. Create Payment Request"| AbacatePay
    AbacatePay -->|"3. Payment URL + Transaction ID"| OrderService
    OrderService -->|"4. Save Order\n(status: PENDING)"| Database
    OrderService --> KafkaProducer
    KafkaProducer -->|"5. Publish OrderCreatedEvent"| OrderCreatedTopic
    OrderAPI -->|"6. Return Order + Payment URL"| Client
    
    %% Flow 2: Payment Processing via Webhook
    AbacatePay -->|"7. Payment Status Update\nWebhook"| WebhookAPI
    WebhookAPI --> PaymentService
    PaymentService -->|"8. Update Order Status\n(PAID/FAILED)"| Database
    PaymentService --> KafkaProducer
    KafkaProducer -->|"9. Publish PaymentConfirmedEvent"| PaymentConfirmedTopic
    
    %% Flow 3: Event Processing
    OrderCreatedTopic --> KafkaConsumer
    PaymentConfirmedTopic --> KafkaConsumer
    
    %% Flow 4: Downstream Services
    PaymentConfirmedTopic --> StockService
    StockService -->|"Reserve Stock"| StockReservedTopic
    
    StockReservedTopic --> InvoiceService
    InvoiceService -->|"Generate Invoice"| InvoiceGeneratedTopic
    
    PaymentConfirmedTopic --> NotificationService
    InvoiceGeneratedTopic --> NotificationService
    
    %% Styling
    classDef external fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef api fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef service fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    classDef kafka fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef database fill:#fce4ec,stroke:#880e4f,stroke-width:2px
    classDef consumer fill:#f1f8e9,stroke:#33691e,stroke-width:2px
    
    class Client,AbacatePay external
    class OrderAPI,WebhookAPI api
    class OrderService,PaymentService,KafkaProducer,KafkaConsumer service
    class OrderCreatedTopic,PaymentConfirmedTopic,StockReservedTopic,InvoiceGeneratedTopic kafka
    class Database database
    class StockService,InvoiceService,NotificationService consumer
```

## Fluxo Detalhado

### 1. Criação de Pedido
1. **Cliente** envia requisição POST para `/api/orders` com dados do pedido
2. **Order Service** processa a requisição e cria uma solicitação de pagamento no **AbacatePay**
3. **AbacatePay** retorna URL de pagamento e Transaction ID
4. **Order Service** salva o pedido no banco com status `PENDING`
5. **Kafka Producer** publica evento `OrderCreatedEvent` no tópico `orders.created`
6. API retorna o pedido criado com URL de pagamento para o cliente

### 2. Processamento de Pagamento
7. **AbacatePay** envia webhook para `/webhook/payment` quando status do pagamento muda
8. **Payment Service** processa o webhook e atualiza status do pedido no banco
9. **Kafka Producer** publica evento `PaymentConfirmedEvent` no tópico `payments.confirmed`

### 3. Processamento de Eventos
- **Kafka Consumers** da própria aplicação processam eventos internamente
- **Stock Service** escuta `payments.confirmed` e reserva estoque
- **Invoice Service** escuta `stock.reserved` e gera nota fiscal
- **Notification Service** escuta múltiplos eventos para enviar notificações

## Características da Arquitetura

### ✅ Vantagens
- **Event-Driven**: Desacoplamento entre serviços via Kafka
- **Resiliente**: Dead Letter Topics para tratamento de falhas
- **Escalável**: Particionamento de tópicos Kafka
- **Observável**: Logging detalhado em todos os componentes
- **Idempotente**: Configuração de producers para evitar duplicatas

### 🔧 Componentes Técnicos
- **Spring Boot 3.5.4** com Java 21
- **Apache Kafka** para messaging
- **PostgreSQL** como banco principal
- **AbacatePay** como gateway de pagamento
- **Spring Security** para autenticação
- **Docker** para containerização

### 📊 Tópicos Kafka
- `orders.created` - Eventos de pedidos criados
- `payments.confirmed` - Confirmações de pagamento
- `stock.reserved` - Reservas de estoque
- `invoice.generated` - Notas fiscais geradas
- Tópicos DLT para cada um dos acima

### 🔄 Padrões Implementados
- **Saga Pattern** para transações distribuídas
- **Event Sourcing** para auditoria de eventos
- **CQRS** separação de comandos e consultas
- **Circuit Breaker** para resiliência
- **Webhook Pattern** para integração com gateway de pagamento