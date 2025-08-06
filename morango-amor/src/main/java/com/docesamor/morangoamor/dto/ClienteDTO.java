package com.docesamor.morangoamor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClienteDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    private String email;

    private String telefone;

    private LocalDate dataNascimento;

    private String enderecoEntrega;

    private String cep;

    private String cidade;

    private String estado;

    private String preferenciasDoces;

    private String alergias;
}