package com.docesamor.morangoamor.controller;

import com.docesamor.morangoamor.service.AbacatePayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final AbacatePayService abacatePayService;

    @PostMapping("/abacate-pay")
    public ResponseEntity<String> receberWebhookAbacatePay(
            @RequestBody Map<String, Object> webhookData,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        
        log.info("Webhook recebido do Abacate Pay: {}", webhookData);
        
        try {
            // Em produção, validar a assinatura do webhook
            if (signature != null) {
                log.debug("Assinatura do webhook: {}", signature);
                // Implementar validação da assinatura
            }
            
            // Processar webhook
            abacatePayService.processarWebhookPagamento(webhookData);
            
            log.info("Webhook processado com sucesso");
            return ResponseEntity.ok("Webhook processado com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao processar webhook do Abacate Pay: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erro ao processar webhook");
        }
    }

    @PostMapping("/test/pagamento-aprovado/{pedidoId}")
    public ResponseEntity<String> simularPagamentoAprovado(@PathVariable Long pedidoId) {
        log.info("Simulando pagamento aprovado para pedido: {}", pedidoId);
        
        try {
            abacatePayService.simularPagamentoAprovado(pedidoId);
            return ResponseEntity.ok("Pagamento simulado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao simular pagamento: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @PostMapping("/test/pagamento-cancelado/{pedidoId}")
    public ResponseEntity<String> simularPagamentoCancelado(@PathVariable Long pedidoId) {
        log.info("Simulando pagamento cancelado para pedido: {}", pedidoId);
        
        try {
            abacatePayService.simularPagamentoCancelado(pedidoId);
            return ResponseEntity.ok("Cancelamento simulado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao simular cancelamento: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Morango do Amor Webhooks",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}