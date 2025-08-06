package com.docesamor.morangoamor.entity;

public enum TipoDoce {
    MORANGO_AMOR_TRADICIONAL("Morango do Amor Tradicional"),
    MORANGO_AMOR_CHOCOLATE_BRANCO("Morango do Amor com Chocolate Branco"),
    MORANGO_AMOR_CHOCOLATE_AO_LEITE("Morango do Amor com Chocolate ao Leite"),
    MORANGO_AMOR_CHOCOLATE_MEIO_AMARGO("Morango do Amor com Chocolate Meio Amargo"),
    MORANGO_AMOR_GRANULADO("Morango do Amor com Granulado"),
    MORANGO_AMOR_COCO("Morango do Amor com Coco"),
    MORANGO_AMOR_AMENDOIM("Morango do Amor com Amendoim"),
    MORANGO_AMOR_CASTANHA("Morango do Amor com Castanha"),
    MORANGO_AMOR_DOCE_DE_LEITE("Morango do Amor com Doce de Leite"),
    MORANGO_AMOR_NUTELLA("Morango do Amor com Nutella"),
    MORANGO_AMOR_ESPECIAL("Morango do Amor Especial da Casa");

    private final String descricao;

    TipoDoce(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}