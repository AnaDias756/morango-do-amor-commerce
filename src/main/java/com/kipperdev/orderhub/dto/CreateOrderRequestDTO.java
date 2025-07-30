package com.kipperdev.orderhub.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDTO {

    @Valid
    @NotNull(message = "Dados do cliente são obrigatórios")
    private CustomerDTO customer;

    @Valid
    @NotEmpty(message = "Lista de itens não pode estar vazia")
    private List<OrderItemDTO> items;

    @NotBlank(message = "Método de pagamento é obrigatório")
    private String paymentMethod;
}