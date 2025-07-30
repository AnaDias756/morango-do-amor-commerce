package com.kipperdev.orderhub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kipperdev.orderhub.entity.Order;
import com.kipperdev.orderhub.event.OrderCreatedEvent;
import com.kipperdev.orderhub.event.PaymentConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducerService(KafkaTemplate<String, String> stringKafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = stringKafkaTemplate;
        this.objectMapper = objectMapper;
    }

    private static final String ORDERS_CREATED_TOPIC = "orders.created";
    private static final String PAYMENTS_CONFIRMED_TOPIC = "payments.confirmed";

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = "order-" + event.getOrderId();
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(ORDERS_CREATED_TOPIC, key, eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Evento OrderCreated publicado com sucesso para pedido {}: offset={}", 
                        event.getOrderId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Falha ao publicar evento OrderCreated para pedido {}: {}", 
                        event.getOrderId(), ex.getMessage());
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar evento OrderCreated para pedido {}: {}", 
                event.getOrderId(), e.getMessage());
        }
    }

    public void publishPaymentConfirmedEvent(Order order) {
        try {
            PaymentConfirmedEvent event = new PaymentConfirmedEvent();
            event.setOrderId(order.getId());
            event.setCustomerEmail(order.getCustomer().getEmail());
            event.setAmount(order.getTotalAmount());
            event.setAbacateTransactionId(order.getAbacateTransactionId());
            event.setPaymentMethod(order.getPaymentMethod());
            event.setPaidAt(order.getPaidAt());
            
            String eventJson = objectMapper.writeValueAsString(event);
            String key = "payment-" + order.getId();
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(PAYMENTS_CONFIRMED_TOPIC, key, eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Evento PaymentConfirmed publicado com sucesso para pedido {}: offset={}", 
                        order.getId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Falha ao publicar evento PaymentConfirmed para pedido {}: {}", 
                        order.getId(), ex.getMessage());
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar evento PaymentConfirmed para pedido {}: {}", 
                order.getId(), e.getMessage());
        }
    }
}