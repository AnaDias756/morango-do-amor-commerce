package com.kipperdev.orderhub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kipperdev.orderhub.entity.OrderStatus;
import com.kipperdev.orderhub.event.InvoiceGeneratedEvent;
import com.kipperdev.orderhub.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConsumerService {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR,
        kafkaTemplate = "stringKafkaTemplate"
    )
    @KafkaListener(topics = "stock.reserved", groupId = "orderhub-stock-group")
    public void handleStockReservedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Recebido evento de estoque reservado: topic={}, partition={}, offset={}", 
                topic, partition, offset);
            
            StockReservedEvent event = objectMapper.readValue(message, StockReservedEvent.class);
            
            // Processar evento de estoque
            if ("SUCCESS".equals(event.getStatus())) {
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.READY_TO_SHIP);
                log.info("Pedido {} atualizado para READY_TO_SHIP após reserva de estoque", event.getOrderId());
            } else {
                log.warn("Falha na reserva de estoque para pedido {}: {}", event.getOrderId(), event.getMessage());
                // Aqui poderia implementar lógica de compensação
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Erro ao processar evento de estoque reservado: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no processamento do evento de estoque", e);
        }
    }

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR,
        kafkaTemplate = "stringKafkaTemplate"
    )
    @KafkaListener(topics = "invoice.generated", groupId = "orderhub-invoice-group")
    public void handleInvoiceGeneratedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Recebido evento de nota fiscal gerada: topic={}, partition={}, offset={}", 
                topic, partition, offset);
            
            InvoiceGeneratedEvent event = objectMapper.readValue(message, InvoiceGeneratedEvent.class);
            
            // Processar evento de nota fiscal
            if ("GENERATED".equals(event.getStatus())) {
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.COMPLETED);
                log.info("Pedido {} finalizado após geração da nota fiscal {}", 
                    event.getOrderId(), event.getInvoiceNumber());
            } else {
                log.warn("Falha na geração da nota fiscal para pedido {}: {}", 
                    event.getOrderId(), event.getMessage());
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Erro ao processar evento de nota fiscal gerada: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no processamento do evento de nota fiscal", e);
        }
    }

    // Listener para Dead Letter Topic (DLT)
    @KafkaListener(topics = "stock.reserved.DLT", groupId = "orderhub-stock-dlt-group")
    public void handleStockReservedDLT(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.error("Mensagem enviada para DLT de estoque: topic={}, message={}", topic, message);
        // Aqui poderia implementar notificação para equipe de suporte
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "invoice.generated.DLT", groupId = "orderhub-invoice-dlt-group")
    public void handleInvoiceGeneratedDLT(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.error("Mensagem enviada para DLT de nota fiscal: topic={}, message={}", topic, message);
        // Aqui poderia implementar notificação para equipe de suporte
        acknowledgment.acknowledge();
    }
}