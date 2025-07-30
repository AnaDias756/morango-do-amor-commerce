package com.kipperdev.orderhub.controller;

import com.kipperdev.orderhub.dto.abacate.AbacateWebhookDTO;
import com.kipperdev.orderhub.service.AbacatePayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/webhooks/abacatepay")
@RequiredArgsConstructor
@Slf4j
public class AbacatePayWebhookController {

    private final AbacatePayService abacatePayService;
    
    @Value("${abacate.webhook.secret:}")
    private String webhookSecret;
    
    @Value("${abacate.webhook.signature.enabled:false}")
    private boolean signatureValidationEnabled;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody AbacateWebhookDTO webhook,
            @RequestHeader(value = "X-Abacate-Signature", required = false) String signature,
            @RequestBody String rawBody) {
        
        try {
            log.info("Webhook recebido do Abacatepay: event={}, devMode={}", 
                webhook.getEvent(), webhook.getDevMode());
            
            // Validar assinatura se habilitado
            if (signatureValidationEnabled && !validateSignature(rawBody, signature)) {
                log.warn("Assinatura do webhook inválida");
                return ResponseEntity.status(401).body("Invalid signature");
            }
            
            // Processar webhook baseado no evento
            if ("billing.paid".equals(webhook.getEvent())) {
                String billingId = webhook.getData().getBilling().getId();
                String status = webhook.getData().getBilling().getStatus();
                
                abacatePayService.processWebhook(billingId, webhook.getEvent(), status);
                
                log.info("Webhook de pagamento processado com sucesso: billing={}", billingId);
            } else {
                log.info("Evento de webhook não processado: {}", webhook.getEvent());
            }
            
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            log.error("Erro ao processar webhook do Abacatepay: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    
    private boolean validateSignature(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isEmpty() || signature == null) {
            return false;
        }
        
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = HexFormat.of().formatHex(hash);
            
            return signature.equals(expectedSignature);
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Erro ao validar assinatura do webhook: {}", e.getMessage());
            return false;
        }
    }
}