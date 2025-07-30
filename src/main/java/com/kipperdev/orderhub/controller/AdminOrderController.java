package com.kipperdev.orderhub.controller;

import com.kipperdev.orderhub.dto.OrderResponseDTO;
import com.kipperdev.orderhub.entity.OrderStatus;
import com.kipperdev.orderhub.service.OrderService;
import com.kipperdev.orderhub.specification.OrderSpecification;
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
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Consulta administrativa de pedidos - status: {}, email: {}, nome: {}, período: {} a {}", 
            status, customerEmail, customerName, startDate, endDate);
        
        var specification = OrderSpecification.withFilters(status, customerEmail, startDate, endDate);
        
        if (customerName != null && !customerName.trim().isEmpty()) {
            specification = specification.and(OrderSpecification.byCustomerName(customerName));
        }
        
        Page<OrderResponseDTO> orders = orderService.filterOrders(specification, pageable);
        
        log.info("Retornando {} pedidos de {} total", orders.getNumberOfElements(), orders.getTotalElements());
        
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        log.info("Consulta administrativa do pedido: {}", id);
        
        return orderService.getOrderById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        
        log.info("Atualizando status do pedido {} para: {}", id, status);
        
        try {
            OrderResponseDTO updatedOrder = orderService.updateOrderStatus(id, status);
            log.info("Status do pedido {} atualizado com sucesso para: {}", id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar status do pedido {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Consultando estatísticas de pedidos - período: {} a {}", startDate, endDate);
        
        Map<String, Object> stats = orderService.getOrderStatistics(startDate, endDate);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "csv") String format) {
        
        log.info("Exportando pedidos - formato: {}, status: {}, email: {}, período: {} a {}", 
            format, status, customerEmail, startDate, endDate);
        
        try {
            String exportData = orderService.exportOrders(status, customerEmail, startDate, endDate, format);
            
            return ResponseEntity.ok()
                .header("Content-Type", format.equals("csv") ? "text/csv" : "application/json")
                .header("Content-Disposition", "attachment; filename=orders." + format)
                .body(exportData);
                
        } catch (Exception e) {
            log.error("Erro ao exportar pedidos: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erro ao exportar dados");
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {
        log.info("Cancelando pedido: {}", id);
        
        try {
            OrderResponseDTO cancelledOrder = orderService.updateOrderStatus(id, OrderStatus.CANCELLED);
            log.info("Pedido {} cancelado com sucesso", id);
            return ResponseEntity.ok(cancelledOrder);
        } catch (RuntimeException e) {
            log.error("Erro ao cancelar pedido {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}