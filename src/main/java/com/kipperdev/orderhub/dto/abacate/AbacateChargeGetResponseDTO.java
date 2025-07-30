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
public class AbacateChargeGetResponseDTO {
    
    private String error;
    private AbacateChargeDataDTO data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbacateChargeDataDTO {
        private String id;
        private Integer amount;
        private String status;
        private String frequency;
        private Boolean devMode;
        private List<String> methods;
        private Boolean allowCoupons;
        private List<Object> coupons;
        private List<Object> couponsUsed;
        private String url;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime paidAt;
        private List<AbacateProductResponseDTO> products;
        private AbacateChargeMetadataDTO metadata;
        private AbacateCustomerDTO customer;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AbacateProductResponseDTO {
            private String id;
            private String externalId;
            private Integer quantity;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AbacateChargeMetadataDTO {
            private Integer fee;
            private String returnUrl;
            private String completionUrl;
        }
    }
}