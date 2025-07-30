package com.kipperdev.orderhub.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmedEvent {

    private Long orderId;
    private String customerEmail;
    private BigDecimal amount;
    private String abacateTransactionId;
    private String paymentMethod;
    private LocalDateTime paidAt;
}