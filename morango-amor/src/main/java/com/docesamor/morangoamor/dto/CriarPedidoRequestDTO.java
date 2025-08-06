package com.docesamor.morangoamor.dto;

import com.docesamor.morangoamor.entity.FormaPagamento;
import com.docesamor.morangoamor.entity.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CriarPedidoRequestDTO {

    @Valid
    @NotNull(message = "Dados do cliente são obrigatórios")
    private ClienteDTO cliente;

    @Valid
    @NotEmpty(message = "Pelo menos um item deve ser incluído no pedido")
    private List<ItemPedidoDTO> itens;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private FormaPagamento formaPagamento;

    @NotNull(message = "Tipo de entrega é obrigatório")
    private TipoEntrega tipoEntrega;

    private String enderecoEntrega;

    private String observacoes;

    private String cupomDesconto;
}