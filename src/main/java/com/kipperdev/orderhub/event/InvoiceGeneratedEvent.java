package com.kipperdev.orderhub.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceGeneratedEvent {

    private Long orderId;
    private String invoiceNumber;
    private BigDecimal invoiceAmount;
    private String invoiceUrl;
    private String status; // GENERATED, FAILED
    private LocalDateTime generatedAt;
    private String message;
}