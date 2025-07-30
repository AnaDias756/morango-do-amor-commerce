package com.kipperdev.orderhub.controller;

import com.kipperdev.orderhub.dto.OrderStatusDTO;
import com.kipperdev.orderhub.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/public/orders")
@RequiredArgsConstructor
@Slf4j
public class PublicOrderController {

    private final OrderService orderService;

    @GetMapping("/{id}/status")
    public ResponseEntity<OrderStatusDTO> getOrderStatus(@PathVariable Long id) {
        log.info("Consulta pública de status do pedido: {}", id);
        
        try {
            Optional<OrderStatusDTO> status = orderService.getOrderStatus(id);
            
            if (status.isPresent()) {
                log.info("Status do pedido {} consultado: {}", id, status.get().getStatus());
                return ResponseEntity.ok(status.get());
            } else {
                log.warn("Pedido {} não encontrado", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception error) {
            log.error("Erro ao consultar status do pedido {}: {}", id, error.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/customer/{email}/status")
    public ResponseEntity<List<OrderStatusDTO>> getOrdersByCustomerEmail(@PathVariable String email) {
        log.info("Consulta pública de pedidos por email: {}", email);
        
        try {
            List<OrderStatusDTO> orders = orderService.getOrdersByCustomerEmail(email);
            log.info("Consulta de pedidos por email {} concluída. {} pedidos encontrados", email, orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception error) {
            log.error("Erro ao consultar pedidos por email {}: {}", email, error.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}