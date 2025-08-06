package com.docesamor.morangoamor.entity;

public enum TipoEntrega {
    RETIRADA_LOJA("Retirada na Loja"),
    ENTREGA_DOMICILIO("Entrega em Domicílio"),
    DRIVE_THRU("Drive Thru");

    private final String descricao;

    TipoEntrega(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}