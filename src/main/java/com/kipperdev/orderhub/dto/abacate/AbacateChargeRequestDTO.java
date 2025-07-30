package com.kipperdev.orderhub.dto.abacate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacateChargeRequestDTO {

    private String frequency = "ONE_TIME";
    private List<String> methods;
    private List<AbacateProductDTO> products;
    
    @JsonProperty("returnUrl")
    private String returnUrl;
    
    @JsonProperty("completionUrl")
    private String completionUrl;
    
    private AbacateCustomerDTO customer;

    @JsonProperty("customerId")
    private String abacateCustomerId;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbacateProductDTO {
        @JsonProperty("externalId")
        private String externalId;
        private String name;
        private Integer quantity;
        private Integer price;
        private String description;
    }
}