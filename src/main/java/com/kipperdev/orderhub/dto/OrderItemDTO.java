package com.kipperdev.orderhub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long id;

    @NotBlank(message = "Nome do produto é obrigatório")
    private String productName;

    @NotBlank(message = "SKU do produto é obrigatório")
    private String productSku;

    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantity;

    @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero")
    private BigDecimal unitPrice;

    private BigDecimal totalPrice;
}