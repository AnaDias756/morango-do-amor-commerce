package com.kipperdev.orderhub.service;

import com.kipperdev.orderhub.dto.*;
import com.kipperdev.orderhub.entity.Customer;
import com.kipperdev.orderhub.entity.Order;
import com.kipperdev.orderhub.entity.OrderItem;
import com.kipperdev.orderhub.entity.OrderStatus;
import com.kipperdev.orderhub.event.OrderCreatedEvent;
import com.kipperdev.orderhub.mapper.OrderMapper;
import com.kipperdev.orderhub.repository.CustomerRepository;
import com.kipperdev.orderhub.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;
    private final CustomerService customerService;
    private final AbacatePayService abacatePayService;
    private final KafkaProducerService kafkaProducerService;
    
    public OrderService(OrderRepository orderRepository,
                       CustomerRepository customerRepository,
                       OrderMapper orderMapper,
                       CustomerService customerService,
                       AbacatePayService abacatePayService,
                       @Autowired(required = false) KafkaProducerService kafkaProducerService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderMapper = orderMapper;
        this.customerService = customerService;
        this.abacatePayService = abacatePayService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request) {
        log.info("Criando novo pedido para cliente: {}", request.getCustomer().getEmail());
        
        Customer customer = customerService.getOrCreateCustomer(request.getCustomer());
        
        Order order = new Order();
        order.setCustomer(customer);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        
        List<OrderItem> orderItems = request.getItems().stream()
            .map(itemDTO -> {
                OrderItem item = orderMapper.toOrderItemEntity(itemDTO);
                item.setOrder(order);
                // Calcular totalPrice manualmente antes de usar
                if (item.getUnitPrice() != null && item.getQuantity() != null) {
                    item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
                return item;
            })
            .collect(Collectors.toList());
        
        order.setItems(orderItems);
        
        BigDecimal totalAmount = orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);
        log.info("Pedido criado com ID: {}", savedOrder.getId());
        
        try {
            String paymentLink = abacatePayService.createPayment(savedOrder);
            savedOrder.setPaymentLink(paymentLink);
            savedOrder = orderRepository.save(savedOrder);
        } catch (Exception e) {
            log.error("Erro ao criar pagamento no Abacate Pay para pedido {}: {}", savedOrder.getId(), e.getMessage());
        }
        
        publishOrderCreatedEvent(savedOrder);
        
        return orderMapper.toOrderResponseDTO(savedOrder);
    }
    
    public Optional<OrderStatusDTO> getOrderStatus(Long orderId) {
        return orderRepository.findById(orderId)
            .map(orderMapper::toOrderStatusDTO);
    }
    
    public List<OrderStatusDTO> getOrdersByCustomerEmail(String email) {
        List<Order> orders = orderRepository.findByCustomerEmail(email);
        return orders.stream()
            .map(orderMapper::toOrderStatusDTO)
            .collect(Collectors.toList());
    }
    
    public Page<OrderResponseDTO> filterOrders(Specification<Order> spec, Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(orderMapper::toOrderResponseDTO);
    }
    
    public Optional<OrderResponseDTO> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .map(orderMapper::toOrderResponseDTO);
    }
    
    public Map<String, Object> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalOrders = orderRepository.count();
        stats.put("totalOrders", totalOrders);
        
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByStatus(status);
            stats.put("orders" + status.name(), count);
        }
        
        List<Order> paidOrders = orderRepository.findByStatus(OrderStatus.PAID, Pageable.unpaged()).getContent();
        BigDecimal totalRevenue = paidOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);
        
        return stats;
    }
    
    public String exportOrders(OrderStatus status, String customerEmail, 
                              LocalDateTime startDate, LocalDateTime endDate, String format) {
        List<Order> orders = orderRepository.findAll();
        
        if ("csv".equalsIgnoreCase(format)) {
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Cliente,Email,Status,Valor Total,Data Criação\n");
            
            for (Order order : orders) {
                csv.append(order.getId()).append(",")
                   .append(order.getCustomer().getName()).append(",")
                   .append(order.getCustomer().getEmail()).append(",")
                   .append(order.getStatus()).append(",")
                   .append(order.getTotalAmount()).append(",")
                   .append(order.getCreatedAt()).append("\n");
            }
            
            return csv.toString();
        } else {
            return orders.stream()
                .map(orderMapper::toOrderResponseDTO)
                .collect(Collectors.toList())
                .toString();
        }
    }
    
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderId));
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Status do pedido {} alterado de {} para {}", orderId, oldStatus, newStatus);
        
        return orderMapper.toOrderResponseDTO(updatedOrder);
    }
    
    @Transactional
    public void updateOrderFromAbacateWebhook(String abacateTransactionId, String status) {
        Optional<Order> orderOpt = orderRepository.findByAbacateTransactionId(abacateTransactionId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            OrderStatus newStatus = mapAbacateStatusToOrderStatus(status);
            
            if (newStatus != null && !newStatus.equals(order.getStatus())) {
                order.setStatus(newStatus);
                if (newStatus == OrderStatus.PAID) {
                    order.setPaidAt(LocalDateTime.now());
                }
                orderRepository.save(order);
                
                log.info("Pedido {} atualizado via webhook do Abacate Pay para status: {}", order.getId(), newStatus);
                
                if (newStatus == OrderStatus.PAID && kafkaProducerService != null) {
                    kafkaProducerService.publishPaymentConfirmedEvent(order);
                }
            }
        } else {
            log.warn("Pedido não encontrado para transação Abacate: {}", abacateTransactionId);
        }
    }
    
    private void publishOrderCreatedEvent(Order order) {
        if (kafkaProducerService == null) {
            log.debug("KafkaProducerService não disponível, pulando publicação de evento");
            return;
        }
        
        try {
            OrderCreatedEvent event = new OrderCreatedEvent();
            event.setOrderId(order.getId());
            event.setCustomerEmail(order.getCustomer().getEmail());
            event.setCustomerName(order.getCustomer().getName());
            event.setTotalAmount(order.getTotalAmount());
            event.setPaymentMethod(order.getPaymentMethod());
            event.setCreatedAt(order.getCreatedAt());
            
            List<OrderCreatedEvent.OrderItemEvent> itemEvents = order.getItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItemEvent(
                    item.getProductSku(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()
                ))
                .collect(Collectors.toList());
            event.setItems(itemEvents);
            
            kafkaProducerService.publishOrderCreatedEvent(event);
        } catch (Exception e) {
            log.error("Erro ao publicar evento de pedido criado: {}", e.getMessage());
        }
    }
    
    private OrderStatus mapAbacateStatusToOrderStatus(String abacateStatus) {
        return switch (abacateStatus.toUpperCase()) {
            case "PAID", "COMPLETED" -> OrderStatus.PAID;
            case "FAILED", "CANCELLED" -> OrderStatus.FAILED;
            case "PENDING" -> OrderStatus.PENDING_PAYMENT;
            default -> null;
        };
    }
}