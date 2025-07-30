package com.kipperdev.orderhub.dto.abacate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacateCustomerDTO {

    private String id;
    private AbacateCustomerMetadataDTO metadata;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbacateCustomerMetadataDTO {
        private String name;
        private String email;
        
        @JsonProperty("cellphone")
        private String cellphone;
        
        @JsonProperty("taxId")
        private String taxId;
    }
}