package com.kipperdev.orderhub.dto.abacate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacateChargeResponseDTO {

    private String id;
    private String customerId;
    private BigDecimal amount;
    private String status;
    private String paymentLink;
    private String orderId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}