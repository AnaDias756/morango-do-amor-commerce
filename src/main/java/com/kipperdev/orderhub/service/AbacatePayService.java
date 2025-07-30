package com.kipperdev.orderhub.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import com.kipperdev.orderhub.dto.abacate.AbacateChargeResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kipperdev.orderhub.client.AbacatePayClient;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeRequestDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerResponseDTO;
import com.kipperdev.orderhub.entity.Order;
import com.kipperdev.orderhub.entity.OrderStatus;
import com.kipperdev.orderhub.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbacatePayService {

    private final AbacatePayClient abacatePayClient;
    private final OrderRepository orderRepository;
    
    @Value("${abacate.api.mock-enabled:true}")
    private final boolean mockEnabled;
    
    @Value("${app.base-url:http://localhost:8080}")
    private final String baseUrl;

    public String createPayment(Order order) {
        try {
            AbacateCustomerResponseDTO.AbacateCustomerMetadataDTO abacateCustomer = getOrCreateAbacateCustomer(order);

            AbacateChargeRequestDTO billingRequest = getAbacateChargeRequestDTO(order, abacateCustomer);

            AbacateChargeResponseDTO.AbacateChargeDataDTO billingResponse;
            
            if (mockEnabled) {
                billingResponse = createMockBilling(billingRequest);
                log.info("Billing mockado criado para pedido {}: {}", order.getId(), billingResponse.getId());
            } else {
                billingResponse = abacatePayClient.createBilling(billingRequest).getData();
                log.info("Billing real criado para pedido {}: {}", order.getId(), billingResponse.getId());
            }

            order.setAbacateTransactionId(billingResponse.getId());
            
            return billingResponse.getId();
            
        } catch (Exception e) {
            log.error("Erro ao criar pagamento no Abacate Pay para pedido {}: {}", order.getId(), e.getMessage());
            throw new RuntimeException("Falha na integração com gateway de pagamento", e);
        }
    }

    private AbacateChargeRequestDTO getAbacateChargeRequestDTO(Order order, AbacateCustomerResponseDTO.AbacateCustomerMetadataDTO abacateCustomer) {
        AbacateChargeRequestDTO billingRequest = new AbacateChargeRequestDTO();
        billingRequest.setFrequency("ONE_TIME");
        billingRequest.setMethods(Arrays.asList("PIX"));

        AbacateChargeRequestDTO.AbacateProductDTO product = new AbacateChargeRequestDTO.AbacateProductDTO();
        product.setExternalId(order.getId().toString());
        product.setName("Pedido #" + order.getId());
        product.setQuantity(1);
        product.setDescription("Pagamento do pedido #" + order.getId());

        product.setPrice(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());

        billingRequest.setProducts(Collections.singletonList(product));
        billingRequest.setReturnUrl(baseUrl + "/orders/" + order.getId());
        billingRequest.setCompletionUrl(baseUrl + "/orders/" + order.getId() + "/success");
        billingRequest.setCustomer(abacateCustomer.getMetadata());
        billingRequest.setAbacateCustomerId(abacateCustomer.getId());
        return billingRequest;
    }

    private AbacateCustomerResponseDTO.AbacateCustomerMetadataDTO getOrCreateAbacateCustomer(Order order) {
        if (mockEnabled) {
            AbacateCustomerResponseDTO.AbacateCustomerMetadataDTO mockCustomer = new AbacateCustomerResponseDTO.AbacateCustomerMetadataDTO();
            mockCustomer.setId("cust_" + UUID.randomUUID().toString().substring(0, 8));
            
            AbacateCustomerDTO metadata = new AbacateCustomerDTO();
            metadata.setName(order.getCustomer().getName());
            metadata.setEmail(order.getCustomer().getEmail());
            metadata.setCellphone(order.getCustomer().getPhone());
            metadata.setTaxId("");
            
            mockCustomer.setMetadata(metadata);
            return mockCustomer;
        } else {
            try {
                AbacateCustomerDTO customerRequest = new AbacateCustomerDTO();
                customerRequest.setName(order.getCustomer().getName());
                customerRequest.setEmail(order.getCustomer().getEmail());
                customerRequest.setCellphone(order.getCustomer().getPhone());
                customerRequest.setTaxId(order.getCustomer().getDocument());
                
                log.info("Criando cliente no Abacate Pay: name={}, email={}, cellphone={}, taxId={}", 
                    customerRequest.getName(), customerRequest.getEmail(), 
                    customerRequest.getCellphone(), customerRequest.getTaxId());

                return abacatePayClient.createCustomer(customerRequest).getData();
            } catch (Exception e) {
                log.error("Erro ao criar/buscar cliente no Abacatepay: {}", e.getMessage());
                throw new RuntimeException("Falha ao processar cliente no gateway de pagamento", e);
            }
        }
    }

    private AbacateChargeResponseDTO.AbacateChargeDataDTO createMockBilling(AbacateChargeRequestDTO request) {
        AbacateChargeResponseDTO.AbacateChargeDataDTO response = new AbacateChargeResponseDTO.AbacateChargeDataDTO();
        response.setId("bill_" + UUID.randomUUID().toString().substring(0, 8));
        response.setAmount(request.getProducts().get(0).getPrice());
        response.setStatus("PENDING");
        response.setFrequency(request.getFrequency());
        response.setMethods(request.getMethods());
        response.setCreatedAt(LocalDateTime.now());
        response.setCustomer(new AbacateCustomerResponseDTO.AbacateCustomerMetadataDTO("cust_13455", request.getCustomer()));
        response.setProducts(Collections.singletonList(
            new AbacateChargeResponseDTO.AbacateChargeDataDTO.AbacateProductResponseDTO(
            "prod_" + UUID.randomUUID().toString().substring(0, 8),
                request.getProducts().get(0).getExternalId(),
                request.getProducts().get(0).getQuantity()
            )
        ));

        String paymentLink = baseUrl + "/mock-payment/" + response.getId();
        response.setUrl(paymentLink);
        
        return response;
    }
    
    public AbacateChargeResponseDTO getBilling(String billingId) {
        if (mockEnabled) {
            AbacateChargeResponseDTO mockResponse = new AbacateChargeResponseDTO();
            AbacateChargeResponseDTO.AbacateChargeDataDTO mockData = new AbacateChargeResponseDTO.AbacateChargeDataDTO();
            mockData.setId(billingId);
            mockData.setStatus("PENDING");
            mockData.setFrequency("ONE_TIME");
            mockData.setMethods(Arrays.asList("PIX"));
            mockData.setCreatedAt(LocalDateTime.now());
            return mockResponse;
        } else {
            AbacateChargeResponseDTO getResponse = abacatePayClient.getBilling(billingId);
            return getResponse;
        }
    }
    
    public void processWebhook(String billingId, String event, String status) {
        try {
            log.info("Processando webhook do Abacatepay - Billing: {}, Event: {}, Status: {}", 
                billingId, event, status);

            Optional<Order> orderOpt = orderRepository.findByAbacateTransactionId(billingId);
            
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                OrderStatus newStatus = mapAbacateStatusToOrderStatus(status, event);
                
                if (newStatus != null && !newStatus.equals(order.getStatus())) {
                    order.setStatus(newStatus);
                    if (newStatus == OrderStatus.PAID) {
                        order.setPaidAt(LocalDateTime.now());
                    }
                    orderRepository.save(order);
                    
                    log.info("Pedido {} atualizado via webhook do Abacate Pay para status: {}", order.getId(), newStatus);
                }
            } else {
                log.warn("Pedido não encontrado para billing ID: {}", billingId);
            }
        } catch (Exception e) {
            log.error("Erro ao processar webhook do Abacatepay: {}", e.getMessage(), e);
        }
    }
    
    private OrderStatus mapAbacateStatusToOrderStatus(String status, String event) {
        if ("billing.paid".equals(event) || "PAID".equals(status)) {
            return OrderStatus.PAID;
        } else if ("billing.failed".equals(event) || "billing.cancelled".equals(event) || "FAILED".equals(status)) {
            return OrderStatus.FAILED;
        } else if ("PENDING".equals(status)) {
            return OrderStatus.PENDING_PAYMENT;
        }
        return null;
    }
}