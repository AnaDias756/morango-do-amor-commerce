package com.docesamor.morangoamor.controller;

import com.docesamor.morangoamor.dto.PedidoResponseDTO;
import com.docesamor.morangoamor.entity.StatusPedido;
import com.docesamor.morangoamor.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/pedidos")
@RequiredArgsConstructor
@Slf4j
public class AdminPedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<Page<PedidoResponseDTO>> listarTodosPedidos(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) String clienteEmail,
            @RequestParam(required = false) String clienteNome,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Consulta administrativa de pedidos - status: {}, email: {}, nome: {}, período: {} a {}", 
            status, clienteEmail, clienteNome, dataInicio, dataFim);
        
        Page<PedidoResponseDTO> pedidos = pedidoService.filtrarPedidos(
            status, clienteEmail, clienteNome, dataInicio, dataFim, pageable);
        
        log.info("Retornando {} pedidos de {} total", pedidos.getNumberOfElements(), pedidos.getTotalElements());
        
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPedidoPorId(@PathVariable Long id) {
        log.info("Consulta administrativa do pedido: {}", id);
        
        return pedidoService.buscarPedidoPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PedidoResponseDTO> atualizarStatusPedido(
            @PathVariable Long id,
            @RequestParam StatusPedido status) {
        
        log.info("Atualizando status do pedido {} para: {}", id, status);
        
        try {
            PedidoResponseDTO pedidoAtualizado = pedidoService.atualizarStatusPedido(id, status);
            log.info("Status do pedido {} atualizado com sucesso para: {}", id, status);
            return ResponseEntity.ok(pedidoAtualizado);
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar status do pedido {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        
        log.info("Consultando estatísticas de pedidos - período: {} a {}", dataInicio, dataFim);
        
        Map<String, Object> estatisticas = pedidoService.obterEstatisticasPedidos(dataInicio, dataFim);
        
        return ResponseEntity.ok(estatisticas);
    }

    @GetMapping("/exportar")
    public ResponseEntity<String> exportarPedidos(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) String clienteEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(defaultValue = "csv") String formato) {
        
        log.info("Exportando pedidos - formato: {}, status: {}, email: {}, período: {} a {}", 
            formato, status, clienteEmail, dataInicio, dataFim);
        
        try {
            String dadosExportacao = pedidoService.exportarPedidos(status, clienteEmail, dataInicio, dataFim, formato);
            
            return ResponseEntity.ok()
                .header("Content-Type", formato.equals("csv") ? "text/csv" : "application/json")
                .header("Content-Disposition", "attachment; filename=pedidos_doces." + formato)
                .body(dadosExportacao);
                
        } catch (Exception e) {
            log.error("Erro ao exportar pedidos: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erro ao exportar dados");
        }
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Long id) {
        log.info("Cancelando pedido: {}", id);
        
        try {
            PedidoResponseDTO pedidoCancelado = pedidoService.atualizarStatusPedido(id, StatusPedido.CANCELADO);
            log.info("Pedido {} cancelado com sucesso", id);
            return ResponseEntity.ok(pedidoCancelado);
        } catch (RuntimeException e) {
            log.error("Erro ao cancelar pedido {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> obterDashboard() {
        log.info("Consultando dados do dashboard");
        
        Map<String, Object> dashboard = pedidoService.obterDadosDashboard();
        
        return ResponseEntity.ok(dashboard);
    }
}