package com.docesamor.morangoamor.dto;

import com.docesamor.morangoamor.entity.SaborMorango;
import com.docesamor.morangoamor.entity.TipoDoce;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DoceDTO {

    private Long id;

    private String nome;

    private String descricao;

    private BigDecimal preco;

    private TipoDoce tipo;

    private String tipoDescricao;

    private SaborMorango sabor;

    private String saborDescricao;

    private Integer pesoGramas;

    private Integer caloriasPorUnidade;

    private String ingredientesEspeciais;

    private Integer tempoPreparoMinutos;

    private Boolean disponivel;

    private Integer estoqueAtual;

    private Integer estoqueMinimo;

    private String urlImagem;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean estoqueBaixo;

    public String getTipoDescricao() {
        return tipo != null ? tipo.getDescricao() : null;
    }

    public String getSaborDescricao() {
        return sabor != null ? sabor.getDescricao() : null;
    }

    public Boolean getEstoqueBaixo() {
        return estoqueAtual != null && estoqueMinimo != null && estoqueAtual <= estoqueMinimo;
    }
}