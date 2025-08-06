package com.docesamor.morangoamor.dto;

import com.docesamor.morangoamor.entity.FormaPagamento;
import com.docesamor.morangoamor.entity.StatusPedido;
import com.docesamor.morangoamor.entity.TipoEntrega;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoResponseDTO {

    private Long id;

    private ClienteDTO cliente;

    private StatusPedido status;

    private String statusDescricao;

    private BigDecimal valorTotal;

    private BigDecimal descontoAplicado;

    private BigDecimal valorFinal;

    private FormaPagamento formaPagamento;

    private TipoEntrega tipoEntrega;

    private String enderecoEntrega;

    private String observacoes;

    private Integer tempoPreparoEstimado;

    private LocalDateTime dataEntregaPrevista;

    private String paymentLink;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime paidAt;

    private LocalDateTime preparedAt;

    private LocalDateTime deliveredAt;

    private List<ItemPedidoDTO> itens;

    private String statusUrl;

    public String getStatusDescricao() {
        return status != null ? status.getDescricao() : null;
    }
}