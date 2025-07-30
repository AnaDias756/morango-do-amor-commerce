package com.kipperdev.orderhub.dto.abacate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacateChargeRequestDTO {

    private String customerId;
    private BigDecimal amount;
    private String description;
    private String orderId;
    private String paymentMethod;
}