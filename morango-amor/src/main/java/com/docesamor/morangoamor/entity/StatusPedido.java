package com.docesamor.morangoamor.entity;

public enum StatusPedido {
    AGUARDANDO_PAGAMENTO("Aguardando Pagamento"),
    PAGO("Pago"),
    FALHA_PAGAMENTO("Falha no Pagamento"),
    PREPARANDO("Preparando Doces"),
    PRONTO_RETIRADA("Pronto para Retirada"),
    SAIU_ENTREGA("Saiu para Entrega"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}