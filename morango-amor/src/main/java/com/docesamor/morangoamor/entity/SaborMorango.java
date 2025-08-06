package com.docesamor.morangoamor.entity;

public enum SaborMorango {
    MORANGO_NATURAL("Morango Natural"),
    MORANGO_DOCE("Morango Doce"),
    MORANGO_AZEDO("Morango Azedinho"),
    MORANGO_SILVESTRE("Morango Silvestre"),
    MORANGO_PREMIUM("Morango Premium"),
    MORANGO_ORGANICO("Morango Orgânico");

    private final String descricao;

    SaborMorango(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}