package com.docesamor.morangoamor.controller;

import com.docesamor.morangoamor.dto.CriarPedidoRequestDTO;
import com.docesamor.morangoamor.dto.PedidoResponseDTO;
import com.docesamor.morangoamor.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@Valid @RequestBody CriarPedidoRequestDTO request) {
        try {
            log.info("Recebida solicitação de pedido de doces para cliente: {}", request.getCliente().getEmail());
            
            PedidoResponseDTO response = pedidoService.criarPedido(request);
            
            log.info("Pedido de doces criado com sucesso: ID={}, Status={}, Valor={}", 
                response.getId(), response.getStatus(), response.getValorFinal());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Erro ao criar pedido de doces: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na criação do pedido: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPedido(@PathVariable Long id) {
        try {
            log.info("Consultando pedido de doces: {}", id);
            
            return pedidoService.buscarPedidoPorId(id)
                .map(pedido -> {
                    log.info("Pedido encontrado: ID={}, Status={}", pedido.getId(), pedido.getStatus());
                    return ResponseEntity.ok(pedido);
                })
                .orElse(ResponseEntity.notFound().build());
                
        } catch (Exception e) {
            log.error("Erro ao buscar pedido {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cliente/{email}")
    public ResponseEntity<?> buscarPedidosPorCliente(@PathVariable String email) {
        try {
            log.info("Consultando pedidos do cliente: {}", email);
            
            var pedidos = pedidoService.buscarPedidosPorCliente(email);
            
            log.info("Encontrados {} pedidos para o cliente: {}", pedidos.size(), email);
            
            return ResponseEntity.ok(pedidos);
            
        } catch (Exception e) {
            log.error("Erro ao buscar pedidos do cliente {}: {}", email, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}