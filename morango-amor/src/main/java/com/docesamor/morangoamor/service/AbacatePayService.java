package com.docesamor.morangoamor.service;

import com.docesamor.morangoamor.entity.Pedido;
import com.docesamor.morangoamor.entity.StatusPedido;
import com.docesamor.morangoamor.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbacatePayService {

    private final RestTemplate restTemplate;
    private final PedidoRepository pedidoRepository;
    private final EventoService eventoService;

    @Value("${app.abacate-pay.api-url:https://api.abacatepay.com}")
    private String abacatePayApiUrl;

    @Value("${app.abacate-pay.api-key:mock-api-key}")
    private String abacatePayApiKey;

    @Value("${app.abacate-pay.webhook-url:http://localhost:8080/webhooks/abacate-pay}")
    private String webhookUrl;

    @Value("${app.abacate-pay.mock-enabled:true}")
    private boolean mockEnabled;

    public String criarLinkPagamento(Pedido pedido) {
        log.info("Criando link de pagamento para pedido: {}", pedido.getId());
        
        try {
            if (mockEnabled) {
                return criarLinkPagamentoMock(pedido);
            }
            
            // Preparar dados para a API do Abacate Pay
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("amount", pedido.getValorFinal().multiply(new BigDecimal("100")).intValue()); // Centavos
            paymentData.put("currency", "BRL");
            paymentData.put("description", "Pedido de Doces #" + pedido.getId());
            paymentData.put("external_id", pedido.getId().toString());
            paymentData.put("webhook_url", webhookUrl);
            
            // Dados do cliente
            Map<String, Object> customer = new HashMap<>();
            customer.put("name", pedido.getCliente().getNome());
            customer.put("email", pedido.getCliente().getEmail());
            customer.put("phone", pedido.getCliente().getTelefone());
            paymentData.put("customer", customer);
            
            // Itens do pedido
            paymentData.put("items", pedido.getItens().stream().map(item -> {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("name", item.getDoce().getNome());
                itemData.put("quantity", item.getQuantidade());
                itemData.put("unit_price", item.getPrecoUnitario().multiply(new BigDecimal("100")).intValue());
                return itemData;
            }).toList());
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(abacatePayApiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentData, headers);
            
            // Fazer chamada para API
            ResponseEntity<Map> response = restTemplate.postForEntity(
                abacatePayApiUrl + "/v1/payments", request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                String transactionId = (String) responseBody.get("id");
                String paymentLink = (String) responseBody.get("payment_url");
                
                // Atualizar pedido com transaction ID
                pedido.setAbacateTransactionId(transactionId);
                pedidoRepository.save(pedido);
                
                log.info("Link de pagamento criado com sucesso para pedido {}: {}", 
                    pedido.getId(), paymentLink);
                
                return paymentLink;
            } else {
                throw new RuntimeException("Erro ao criar link de pagamento: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Erro ao criar link de pagamento para pedido {}: {}", 
                pedido.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao criar link de pagamento: " + e.getMessage(), e);
        }
    }

    private String criarLinkPagamentoMock(Pedido pedido) {
        log.info("Criando link de pagamento MOCK para pedido: {}", pedido.getId());
        
        // Gerar transaction ID mock
        String transactionId = "mock_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Atualizar pedido com transaction ID mock
        pedido.setAbacateTransactionId(transactionId);
        pedidoRepository.save(pedido);
        
        // Retornar link mock
        String mockPaymentLink = String.format(
            "https://mock.abacatepay.com/payment/%s?amount=%.2f&description=Pedido%%20de%%20Doces%%20#%d",
            transactionId, pedido.getValorFinal(), pedido.getId());
        
        log.info("Link de pagamento MOCK criado: {}", mockPaymentLink);
        
        return mockPaymentLink;
    }

    @Transactional
    public void processarWebhookPagamento(Map<String, Object> webhookData) {
        log.info("Processando webhook de pagamento: {}", webhookData);
        
        try {
            String eventType = (String) webhookData.get("event_type");
            Map<String, Object> paymentData = (Map<String, Object>) webhookData.get("data");
            
            if (paymentData == null) {
                log.warn("Webhook sem dados de pagamento");
                return;
            }
            
            String transactionId = (String) paymentData.get("id");
            String externalId = (String) paymentData.get("external_id");
            String status = (String) paymentData.get("status");
            
            if (transactionId == null && externalId == null) {
                log.warn("Webhook sem transaction_id ou external_id");
                return;
            }
            
            // Buscar pedido
            Pedido pedido = null;
            if (transactionId != null) {
                pedido = pedidoRepository.findByAbacateTransactionId(transactionId).orElse(null);
            }
            if (pedido == null && externalId != null) {
                try {
                    Long pedidoId = Long.parseLong(externalId);
                    pedido = pedidoRepository.findById(pedidoId).orElse(null);
                } catch (NumberFormatException e) {
                    log.warn("External ID inválido: {}", externalId);
                }
            }
            
            if (pedido == null) {
                log.warn("Pedido não encontrado para transaction_id: {} ou external_id: {}", 
                    transactionId, externalId);
                return;
            }
            
            // Processar evento baseado no tipo
            switch (eventType) {
                case "payment.approved":
                case "payment.paid":
                    processarPagamentoAprovado(pedido, paymentData);
                    break;
                case "payment.cancelled":
                case "payment.failed":
                    processarPagamentoCancelado(pedido, paymentData);
                    break;
                case "payment.refunded":
                    processarPagamentoEstornado(pedido, paymentData);
                    break;
                default:
                    log.info("Evento de webhook não processado: {}", eventType);
            }
            
            // Publicar evento de webhook processado
            eventoService.publicarEventoWebhook("WEBHOOK_PROCESSADO", Map.of(
                "eventType", eventType,
                "pedidoId", pedido.getId(),
                "transactionId", transactionId != null ? transactionId : "N/A",
                "status", status != null ? status : "N/A"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao processar webhook de pagamento: {}", e.getMessage(), e);
            
            // Publicar evento de erro
            eventoService.publicarEventoWebhook("WEBHOOK_ERRO", Map.of(
                "erro", e.getMessage(),
                "webhookData", webhookData.toString()
            ));
        }
    }

    private void processarPagamentoAprovado(Pedido pedido, Map<String, Object> paymentData) {
        log.info("Processando pagamento aprovado para pedido: {}", pedido.getId());
        
        if (pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO) {
            pedido.setStatus(StatusPedido.PAGO);
            pedido.setPaidAt(LocalDateTime.now());
            
            // Atualizar transaction ID se não estava definido
            String transactionId = (String) paymentData.get("id");
            if (transactionId != null && pedido.getAbacateTransactionId() == null) {
                pedido.setAbacateTransactionId(transactionId);
            }
            
            pedidoRepository.save(pedido);
            
            log.info("Pagamento confirmado para pedido: {}", pedido.getId());
            
            // Publicar evento de pagamento confirmado
            eventoService.publicarEventoPagamentoConfirmado(pedido);
        } else {
            log.warn("Tentativa de confirmar pagamento para pedido {} em status inválido: {}", 
                pedido.getId(), pedido.getStatus());
        }
    }

    private void processarPagamentoCancelado(Pedido pedido, Map<String, Object> paymentData) {
        log.info("Processando pagamento cancelado para pedido: {}", pedido.getId());
        
        if (pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO) {
            pedido.setStatus(StatusPedido.CANCELADO);
            pedidoRepository.save(pedido);
            
            log.info("Pedido {} cancelado devido ao pagamento não aprovado", pedido.getId());
            
            // Publicar evento de status atualizado
            eventoService.publicarEventoStatusAtualizado(pedido, StatusPedido.AGUARDANDO_PAGAMENTO);
        } else {
            log.warn("Tentativa de cancelar pedido {} em status inválido: {}", 
                pedido.getId(), pedido.getStatus());
        }
    }

    private void processarPagamentoEstornado(Pedido pedido, Map<String, Object> paymentData) {
        log.info("Processando estorno de pagamento para pedido: {}", pedido.getId());
        
        // Lógica de estorno - pode variar dependendo do status atual
        if (pedido.getStatus() == StatusPedido.PAGO || pedido.getStatus() == StatusPedido.PREPARANDO) {
            pedido.setStatus(StatusPedido.CANCELADO);
            pedidoRepository.save(pedido);
            
            log.info("Pedido {} cancelado devido ao estorno do pagamento", pedido.getId());
            
            // Publicar evento de estorno
            eventoService.publicarEventoWebhook("PAGAMENTO_ESTORNADO", Map.of(
                "pedidoId", pedido.getId(),
                "valorEstornado", paymentData.getOrDefault("amount", 0),
                "motivo", paymentData.getOrDefault("reason", "Não informado")
            ));
        }
    }

    public void simularPagamentoAprovado(Long pedidoId) {
        if (!mockEnabled) {
            throw new RuntimeException("Simulação de pagamento disponível apenas em modo mock");
        }
        
        log.info("Simulando pagamento aprovado para pedido: {}", pedidoId);
        
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + pedidoId));
        
        Map<String, Object> mockWebhook = new HashMap<>();
        mockWebhook.put("event_type", "payment.approved");
        
        Map<String, Object> mockPaymentData = new HashMap<>();
        mockPaymentData.put("id", pedido.getAbacateTransactionId());
        mockPaymentData.put("external_id", pedidoId.toString());
        mockPaymentData.put("status", "approved");
        mockPaymentData.put("amount", pedido.getValorFinal().multiply(new BigDecimal("100")).intValue());
        
        mockWebhook.put("data", mockPaymentData);
        
        processarWebhookPagamento(mockWebhook);
    }

    public void simularPagamentoCancelado(Long pedidoId) {
        if (!mockEnabled) {
            throw new RuntimeException("Simulação de pagamento disponível apenas em modo mock");
        }
        
        log.info("Simulando pagamento cancelado para pedido: {}", pedidoId);
        
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + pedidoId));
        
        Map<String, Object> mockWebhook = new HashMap<>();
        mockWebhook.put("event_type", "payment.cancelled");
        
        Map<String, Object> mockPaymentData = new HashMap<>();
        mockPaymentData.put("id", pedido.getAbacateTransactionId());
        mockPaymentData.put("external_id", pedidoId.toString());
        mockPaymentData.put("status", "cancelled");
        
        mockWebhook.put("data", mockPaymentData);
        
        processarWebhookPagamento(mockWebhook);
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }
}