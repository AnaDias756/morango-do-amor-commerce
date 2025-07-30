# Implementação do OrderCancelledEvent no Kafka

## 📋 Visão Geral

Este guia mostra como implementar o evento `OrderCancelledEvent` no sistema Kafka do OrderHub, incluindo configuração de tópicos, publicação e consumo do evento.

## 🎯 Cenário de Uso

O `OrderCancelledEvent` é disparado quando:
- Um pedido é cancelado pelo cliente
- Uma saga falha e precisa cancelar o pedido (rollback)
- O sistema cancela automaticamente por timeout ou falha

## 🚀 Implementação Passo a Passo

### 1. Configurar Tópico Kafka

Adicione a configuração do tópico no `KafkaConfig.java`:

```java
@Bean
public NewTopic orderCancelledTopic() {
    return TopicBuilder.name("order.cancelled")
            .partitions(3)
            .replicas(1)
            .build();
}

@Bean
public NewTopic orderCancelledDltTopic() {
    return TopicBuilder.name("order.cancelled.DLT")
            .partitions(3)
            .replicas(1)
            .build();
}
```

### 2. Classe do Evento (Já Implementada)

O evento `OrderCancelledEvent` já foi criado em:
```
src/main/java/com/kipperdev/orderhub/event/OrderCancelledEvent.java
```

**Estrutura do evento:**
```java
public class OrderCancelledEvent {
    private Long orderId;
    private String reason;
    private String compensationId;
    private LocalDateTime cancelledAt;
    private String status;
    private String cancellationType;
    private boolean customerNotified;
    private String notes;
}
```

### 3. Publicar o Evento

Adicione método no `KafkaProducerService.java`:

```java
public void publishOrderCancelledEvent(OrderCancelledEvent event) {
    try {
        String eventJson = objectMapper.writeValueAsString(event);
        
        kafkaTemplate.send("order.cancelled", "order-" + event.getOrderId(), eventJson)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Evento OrderCancelled publicado com sucesso para pedido: {}", 
                        event.getOrderId());
                } else {
                    log.error("❌ Erro ao publicar evento OrderCancelled para pedido {}: {}", 
                        event.getOrderId(), ex.getMessage());
                }
            });
            
    } catch (JsonProcessingException e) {
        log.error("❌ Erro na serialização do OrderCancelledEvent para pedido {}: {}", 
            event.getOrderId(), e.getMessage());
    }
}
```

### 4. Consumir o Evento

Adicione listener no `KafkaConsumerService.java`:

```java
@KafkaListener(
    topics = "order.cancelled",
    groupId = "order-service",
    containerFactory = "kafkaListenerContainerFactory"
)
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2.0),
    dltStrategy = DltStrategy.FAIL_ON_ERROR,
    include = {Exception.class}
)
public void handleOrderCancelledEvent(
        OrderCancelledEvent event,
        Acknowledgment acknowledgment) {
    
    try {
        log.info("📨 Processando evento OrderCancelled para pedido: {} - Motivo: {}", 
            event.getOrderId(), event.getReason());
        
        // Processar cancelamento do pedido
        processOrderCancellation(event);
        
        // Notificar cliente se necessário
        if (event.isCustomerNotified()) {
            notifyCustomerAboutCancellation(event);
        }
        
        // Confirmar processamento
        acknowledgment.acknowledge();
        
        log.info("✅ Evento OrderCancelled processado com sucesso para pedido: {}", 
            event.getOrderId());
            
    } catch (Exception e) {
        log.error("❌ Erro ao processar evento OrderCancelled para pedido {}: {}", 
            event.getOrderId(), e.getMessage());
        throw e; // Rejeita a mensagem para retry
    }
}

@KafkaListener(
    topics = "order.cancelled.DLT",
    groupId = "order-service-dlt"
)
public void handleOrderCancelledDltEvent(
        OrderCancelledEvent event,
        Acknowledgment acknowledgment) {
    
    log.error("💀 Evento OrderCancelled enviado para DLT - Pedido: {} - Motivo: {}", 
        event.getOrderId(), event.getReason());
    
    // Implementar lógica de tratamento de erro crítico
    // Ex: alertas, notificações para equipe de suporte
    
    acknowledgment.acknowledge();
}

private void processOrderCancellation(OrderCancelledEvent event) {
    // Implementar lógica de cancelamento
    // Ex: atualizar status no banco, liberar recursos, etc.
}

private void notifyCustomerAboutCancellation(OrderCancelledEvent event) {
    // Implementar notificação ao cliente
    // Ex: email, SMS, push notification
}
```

### 5. Usar no OrderService

Adicione método no `OrderService.java`:

```java
@Autowired
private KafkaProducerService kafkaProducerService;

public void cancelOrder(Long orderId, String reason, String cancellationType) {
    try {
        // Buscar pedido
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Atualizar status
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        order.setCancelledAt(LocalDateTime.now());
        
        orderRepository.save(order);
        
        // Criar e publicar evento
        OrderCancelledEvent event = new OrderCancelledEvent();
        event.setOrderId(orderId);
        event.setReason(reason);
        event.setCancellationType(cancellationType);
        event.setCancelledAt(LocalDateTime.now());
        event.setStatus("CANCELLED");
        event.setCustomerNotified(true);
        
        kafkaProducerService.publishOrderCancelledEvent(event);
        
        log.info("🚫 Pedido {} cancelado com sucesso - Motivo: {}", orderId, reason);
        
    } catch (Exception e) {
        log.error("❌ Erro ao cancelar pedido {}: {}", orderId, e.getMessage());
        throw new OrderCancellationException("Falha ao cancelar pedido: " + e.getMessage());
    }
}
```

### 6. Endpoint REST

Adicione endpoint no `OrderController.java`:

```java
@PostMapping("/{orderId}/cancel")
public ResponseEntity<Map<String, String>> cancelOrder(
        @PathVariable Long orderId,
        @RequestBody Map<String, String> request) {
    
    try {
        String reason = request.getOrDefault("reason", "Cancelamento solicitado pelo cliente");
        String cancellationType = request.getOrDefault("type", "CUSTOMER_REQUEST");
        
        orderService.cancelOrder(orderId, reason, cancellationType);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Pedido cancelado com sucesso");
        response.put("orderId", orderId.toString());
        response.put("status", "CANCELLED");
        
        return ResponseEntity.ok(response);
        
    } catch (OrderNotFoundException e) {
        return ResponseEntity.notFound().build();
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Erro ao cancelar pedido: " + e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}
```

## 🧪 Testando a Implementação

### 1. Script de Teste

```bash
#!/bin/bash
# test-order-cancelled-event.sh

echo "🧪 Testando OrderCancelledEvent"
echo "=============================="

# 1. Criar um pedido
echo "📦 Criando pedido..."
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productName": "Produto Teste",
        "productSku": "TEST-001",
        "quantity": 1,
        "unitPrice": 29.99
      }
    ]
  }')

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.id')
echo "✅ Pedido criado: $ORDER_ID"

# 2. Cancelar o pedido
echo "🚫 Cancelando pedido..."
CANCEL_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/orders/$ORDER_ID/cancel" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Teste de cancelamento via API",
    "type": "CUSTOMER_REQUEST"
  }')

echo "📋 Resposta do cancelamento:"
echo $CANCEL_RESPONSE | jq .

# 3. Verificar status do pedido
echo "🔍 Verificando status do pedido..."
sleep 2
curl -s "http://localhost:8080/api/orders/$ORDER_ID" | jq '.status'

echo "✅ Teste concluído!"
```

### 2. Monitorar Tópico Kafka

```bash
# Consumir mensagens do tópico
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order.cancelled \
  --from-beginning \
  --property print.key=true
```

### 3. Verificar Logs

```bash
# Acompanhar logs da aplicação
tail -f logs/application.log | grep "OrderCancelled"
```

## 📊 Monitoramento

### Métricas Importantes

- **Taxa de cancelamento**: Quantos pedidos são cancelados
- **Motivos de cancelamento**: Principais causas
- **Tempo de processamento**: Latência do evento
- **Falhas de processamento**: Mensagens que vão para DLT

### Alertas Recomendados

- Alto volume de cancelamentos em pouco tempo
- Falhas recorrentes no processamento
- Mensagens acumuladas no DLT
- Timeout no processamento de eventos

## 🔧 Configurações Adicionais

### application.yml

```yaml
kafka:
  enabled: true
  topics:
    order-cancelled:
      partitions: 3
      replication-factor: 1
      retention-ms: 604800000 # 7 dias
```

### Configuração de Retry

```java
@RetryableTopic(
    attempts = "5",
    backoff = @Backoff(delay = 2000, multiplier = 2.0, maxDelay = 30000),
    dltStrategy = DltStrategy.FAIL_ON_ERROR,
    include = {Exception.class},
    exclude = {OrderNotFoundException.class}
)
```