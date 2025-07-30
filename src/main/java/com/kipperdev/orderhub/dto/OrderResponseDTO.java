package com.kipperdev.orderhub.dto;

import com.kipperdev.orderhub.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private CustomerDTO customer;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private List<OrderItemDTO> items;
    private String statusUrl;
}