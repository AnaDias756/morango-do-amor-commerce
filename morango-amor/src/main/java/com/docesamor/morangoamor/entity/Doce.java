package com.docesamor.morangoamor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "doces")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do doce é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDoce tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaborMorango sabor;

    @Column(name = "peso_gramas")
    private Integer pesoGramas;

    @Column(name = "calorias_por_unidade")
    private Integer caloriasPorUnidade;

    @Column(name = "ingredientes_especiais")
    private String ingredientesEspeciais;

    @Column(name = "tempo_preparo_minutos")
    private Integer tempoPreparoMinutos;

    @Column(name = "disponivel")
    private Boolean disponivel = true;

    @Column(name = "estoque_atual")
    private Integer estoqueAtual = 0;

    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo = 5;

    @Column(name = "url_imagem")
    private String urlImagem;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (disponivel == null) {
            disponivel = true;
        }
        if (estoqueAtual == null) {
            estoqueAtual = 0;
        }
        if (estoqueMinimo == null) {
            estoqueMinimo = 5;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isEstoqueBaixo() {
        return estoqueAtual <= estoqueMinimo;
    }

    public boolean isDisponivel() {
        return disponivel && estoqueAtual > 0;
    }
}