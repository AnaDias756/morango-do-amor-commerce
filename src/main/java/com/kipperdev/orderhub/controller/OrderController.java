package com.kipperdev.orderhub.controller;

import com.kipperdev.orderhub.dto.CreateOrderRequestDTO;
import com.kipperdev.orderhub.dto.OrderResponseDTO;
import com.kipperdev.orderhub.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        try {
            log.info("Recebida solicitação de criação de pedido para cliente: {}", request.getCustomer().getEmail());
            
            OrderResponseDTO response = orderService.createOrder(request);
            
            log.info("Pedido criado com sucesso: ID={}, Status={}", response.getId(), response.getStatus());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Erro ao criar pedido: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na criação do pedido: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
        try {
            return orderService.getOrderById(id)
                .map(order -> ResponseEntity.ok(order))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Erro ao buscar pedido {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}