package com.kipperdev.orderhub.dto.abacate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacateCustomerResponseDTO {
    
    private AbacateCustomerMetadataDTO data;
    private String error;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbacateCustomerMetadataDTO {
        private String id;
        private AbacateCustomerDTO metadata;
    }
}