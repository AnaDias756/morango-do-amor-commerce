package com.kipperdev.orderhub.entity;

public enum OrderStatus {
    PENDING_PAYMENT("Aguardando Pagamento"),
    PAID("Pago"),
    FAILED("Falha no Pagamento"),
    READY_TO_SHIP("Pronto para Envio"),
    SHIPPED("Enviado"),
    COMPLETED("Concluído"),
    CANCELLED("Cancelado");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}