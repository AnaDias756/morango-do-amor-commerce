package com.docesamor.morangoamor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @DecimalMin(value = "0.01", message = "Valor total deve ser maior que zero")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "desconto_aplicado", precision = 10, scale = 2)
    private BigDecimal descontoAplicado = BigDecimal.ZERO;

    @Column(name = "valor_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFinal;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega")
    private TipoEntrega tipoEntrega;

    @Column(name = "endereco_entrega")
    private String enderecoEntrega;

    @Column(name = "observacoes")
    private String observacoes;

    @Column(name = "tempo_preparo_estimado")
    private Integer tempoPreparoEstimado; // em minutos

    @Column(name = "data_entrega_prevista")
    private LocalDateTime dataEntregaPrevista;

    @Column(name = "abacate_transaction_id")
    private String abacateTransactionId;

    @Column(name = "payment_link")
    private String paymentLink;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "prepared_at")
    private LocalDateTime preparedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPedido> itens;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = StatusPedido.AGUARDANDO_PAGAMENTO;
        }
        if (descontoAplicado == null) {
            descontoAplicado = BigDecimal.ZERO;
        }
        calcularValorFinal();
        calcularTempoPreparoEstimado();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == StatusPedido.PAGO && paidAt == null) {
            paidAt = LocalDateTime.now();
        }
        if (status == StatusPedido.PREPARANDO && preparedAt == null) {
            preparedAt = LocalDateTime.now();
        }
        if (status == StatusPedido.ENTREGUE && deliveredAt == null) {
            deliveredAt = LocalDateTime.now();
        }
    }

    private void calcularValorFinal() {
        if (valorTotal != null && descontoAplicado != null) {
            valorFinal = valorTotal.subtract(descontoAplicado);
        }
    }

    private void calcularTempoPreparoEstimado() {
        if (itens != null && !itens.isEmpty()) {
            tempoPreparoEstimado = itens.stream()
                .mapToInt(item -> item.getDoce().getTempoPreparoMinutos() * item.getQuantidade())
                .sum();
            
            // Adiciona tempo base de preparo
            tempoPreparoEstimado += 15;
            
            // Calcula data de entrega prevista
            if (tipoEntrega == TipoEntrega.ENTREGA_DOMICILIO) {
                tempoPreparoEstimado += 30; // tempo de entrega
            }
            
            dataEntregaPrevista = LocalDateTime.now().plusMinutes(tempoPreparoEstimado);
        }
    }

    public void aplicarDesconto(BigDecimal desconto) {
        this.descontoAplicado = desconto;
        calcularValorFinal();
    }

    public boolean isClienteVip() {
        return cliente != null && cliente.getClienteVip();
    }
}