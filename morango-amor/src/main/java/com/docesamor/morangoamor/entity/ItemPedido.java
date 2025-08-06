package com.docesamor.morangoamor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Pedido é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @NotNull(message = "Doce é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doce_id", nullable = false)
    private Doce doce;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Column(nullable = false)
    private Integer quantidade;

    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero")
    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @NotNull(message = "Preço total é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço total deve ser maior que zero")
    @Column(name = "preco_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoTotal;

    @Column(name = "observacoes_item")
    private String observacoesItem;

    @Column(name = "personalizacao")
    private String personalizacao; // Ex: "Sem açúcar", "Extra chocolate", etc.

    @PrePersist
    @PreUpdate
    protected void calcularPrecoTotal() {
        if (quantidade != null && precoUnitario != null) {
            precoTotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }

    public BigDecimal calcularSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}