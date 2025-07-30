package com.kipperdev.orderhub.dto.abacate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacateWebhookDTO {

    private String event;
    private String chargeId;
    private String orderId;
    private String status;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String signature;
}