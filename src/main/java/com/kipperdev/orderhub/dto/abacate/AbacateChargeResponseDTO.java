package com.kipperdev.orderhub.dto.abacate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacateChargeResponseDTO {

    private String id;
    private Integer amount;
    private String status;
    private String frequency;
    private List<String> kind;
    
    @JsonProperty("returnUrl")
    private String returnUrl;
    
    @JsonProperty("completionUrl")
    private String completionUrl;
    
    @JsonProperty("url")
    private String paymentLink;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("paidAt")
    private LocalDateTime paidAt;
    
    private AbacateCustomerDTO customer;
    private List<AbacateChargeRequestDTO.AbacateProductDTO> products;
}