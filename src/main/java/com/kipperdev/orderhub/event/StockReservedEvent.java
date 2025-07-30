package com.kipperdev.orderhub.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedEvent {

    private Long orderId;
    private String status; // SUCCESS, FAILED, PARTIAL
    private List<StockItem> items;
    private LocalDateTime reservedAt;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockItem {
        private String productSku;
        private Integer requestedQuantity;
        private Integer reservedQuantity;
        private String status;
    }
}