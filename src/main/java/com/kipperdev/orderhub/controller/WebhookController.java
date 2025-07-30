package com.kipperdev.orderhub.controller;

import com.kipperdev.orderhub.dto.abacate.AbacateWebhookDTO;
import com.kipperdev.orderhub.service.OrderService;
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
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final OrderService orderService;
    
    @Value("${abacate.webhook.secret:default-secret}")
    private String webhookSecret;
    
    @Value("${abacate.webhook.signature.enabled:false}")
    private boolean signatureValidationEnabled;

    @PostMapping("/abacate")
    public ResponseEntity<String> handleAbacateWebhook(
            @RequestBody AbacateWebhookDTO webhook,
            @RequestHeader(value = "X-Abacate-Signature", required = false) String signature) {
        
        try {
            log.info("Recebido webhook do Abacate Pay: event={}, chargeId={}, orderId={}, status={}", 
                webhook.getEvent(), webhook.getChargeId(), webhook.getOrderId(), webhook.getStatus());
            
            if (signatureValidationEnabled && !validateSignature(webhook, signature)) {
                log.warn("Assinatura inválida no webhook do Abacate Pay");
                return ResponseEntity.badRequest().body("Invalid signature");
            }
            
            switch (webhook.getEvent().toLowerCase()) {
                case "payment.completed":
                case "payment.paid":
                    orderService.updateOrderFromAbacateWebhook(webhook.getChargeId(), "PAID");
                    break;
                    
                case "payment.failed":
                case "payment.cancelled":
                    orderService.updateOrderFromAbacateWebhook(webhook.getChargeId(), "FAILED");
                    break;
                    
                case "payment.pending":
                    orderService.updateOrderFromAbacateWebhook(webhook.getChargeId(), "PENDING");
                    break;
                    
                default:
                    log.info("Evento de webhook não processado: {}", webhook.getEvent());
                    break;
            }
            
            log.info("Webhook do Abacate Pay processado com sucesso: chargeId={}", webhook.getChargeId());
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            log.error("Erro ao processar webhook do Abacate Pay: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error processing webhook");
        }
    }
    
    private boolean validateSignature(AbacateWebhookDTO webhook, String receivedSignature) {
        if (receivedSignature == null || receivedSignature.isEmpty()) {
            return false;
        }
        
        try {
            String payload = webhook.getEvent() + webhook.getChargeId() + webhook.getStatus();
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = HexFormat.of().formatHex(hash);
            
            return calculatedSignature.equals(receivedSignature.toLowerCase());
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Erro ao validar assinatura do webhook: {}", e.getMessage());
            return false;
        }
    }
}