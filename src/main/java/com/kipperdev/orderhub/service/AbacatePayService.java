package com.kipperdev.orderhub.service;

import com.kipperdev.orderhub.client.AbacatePayClient;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeRequestDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeResponseDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerDTO;
import com.kipperdev.orderhub.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbacatePayService {

    private final AbacatePayClient abacatePayClient;
    
    @Value("${abacate.mock.enabled:true}")
    private boolean mockEnabled;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public String createPayment(Order order) {
        try {
            // Criar ou obter cliente no Abacate Pay
            AbacateCustomerDTO abacateCustomer = getOrCreateAbacateCustomer(order);
            
            // Criar cobrança
            AbacateChargeRequestDTO chargeRequest = new AbacateChargeRequestDTO();
            chargeRequest.setCustomerId(abacateCustomer.getId());
            chargeRequest.setAmount(order.getTotalAmount());
            chargeRequest.setDescription("Pedido #" + order.getId());
            chargeRequest.setOrderId(order.getId().toString());
            chargeRequest.setPaymentMethod(order.getPaymentMethod());
            
            AbacateChargeResponseDTO chargeResponse;
            
            if (mockEnabled) {
                // Implementação mockada
                chargeResponse = createMockCharge(chargeRequest);
                log.info("Cobrança mockada criada para pedido {}: {}", order.getId(), chargeResponse.getId());
            } else {
                // Implementação real (será implementada posteriormente)
                chargeResponse = abacatePayClient.createCharge(chargeRequest);
                log.info("Cobrança real criada para pedido {}: {}", order.getId(), chargeResponse.getId());
            }
            
            // Atualizar pedido com ID da transação
            order.setAbacateTransactionId(chargeResponse.getId());
            
            return chargeResponse.getPaymentLink();
            
        } catch (Exception e) {
            log.error("Erro ao criar pagamento no Abacate Pay para pedido {}: {}", order.getId(), e.getMessage());
            throw new RuntimeException("Falha na integração com gateway de pagamento", e);
        }
    }
    
    private AbacateCustomerDTO getOrCreateAbacateCustomer(Order order) {
        if (mockEnabled) {
            // Cliente mockado
            AbacateCustomerDTO mockCustomer = new AbacateCustomerDTO();
            mockCustomer.setId("cust_" + UUID.randomUUID().toString().substring(0, 8));
            mockCustomer.setName(order.getCustomer().getName());
            mockCustomer.setEmail(order.getCustomer().getEmail());
            mockCustomer.setPhone(order.getCustomer().getPhone());
            return mockCustomer;
        } else {
            // Implementação real
            try {
                return abacatePayClient.getCustomerByEmail(order.getCustomer().getEmail());
            } catch (Exception e) {
                // Cliente não existe, criar novo
                AbacateCustomerDTO newCustomer = new AbacateCustomerDTO();
                newCustomer.setName(order.getCustomer().getName());
                newCustomer.setEmail(order.getCustomer().getEmail());
                newCustomer.setPhone(order.getCustomer().getPhone());
                return abacatePayClient.createCustomer(newCustomer);
            }
        }
    }
    
    private AbacateChargeResponseDTO createMockCharge(AbacateChargeRequestDTO request) {
        AbacateChargeResponseDTO response = new AbacateChargeResponseDTO();
        response.setId("charge_" + UUID.randomUUID().toString().substring(0, 8));
        response.setCustomerId(request.getCustomerId());
        response.setAmount(request.getAmount());
        response.setStatus("PENDING");
        response.setOrderId(request.getOrderId());
        response.setCreatedAt(LocalDateTime.now());
        
        // Link de pagamento mockado
        String paymentLink = baseUrl + "/mock-payment/" + response.getId();
        response.setPaymentLink(paymentLink);
        
        return response;
    }
    
    public AbacateChargeResponseDTO getCharge(String chargeId) {
        if (mockEnabled) {
            // Retorno mockado
            AbacateChargeResponseDTO mockResponse = new AbacateChargeResponseDTO();
            mockResponse.setId(chargeId);
            mockResponse.setStatus("PENDING");
            mockResponse.setCreatedAt(LocalDateTime.now());
            return mockResponse;
        } else {
            return abacatePayClient.getCharge(chargeId);
        }
    }
}