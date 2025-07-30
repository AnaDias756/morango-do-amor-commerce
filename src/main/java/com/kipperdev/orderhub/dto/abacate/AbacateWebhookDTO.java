package com.kipperdev.orderhub.dto.abacate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacateWebhookDTO {

    private String event;
    private AbacateWebhookDataDTO data;
    
    @JsonProperty("devMode")
    private Boolean devMode;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbacateWebhookDataDTO {
        private AbacatePaymentDTO payment;
        private AbacateBillingDTO billing;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AbacatePaymentDTO {
            private Integer amount;
            private Integer fee;
            private String method;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AbacateBillingDTO {
            private String id;
            private Integer amount;
            private String status;
            private String frequency;
            private AbacateCustomerDTO customer;
        }
    }
}