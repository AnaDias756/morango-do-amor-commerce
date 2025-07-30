package com.kipperdev.orderhub.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kipperdev.orderhub.client.AbacatePayClient;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeRequestDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeResponseDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeCreateResponseDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeGetResponseDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerCreateRequestDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerCreateResponseDTO;
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
    private boolean mockEnabled;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public String createPayment(Order order) {
        try {
            AbacateCustomerDTO abacateCustomer = getOrCreateAbacateCustomer(order);

            AbacateChargeRequestDTO billingRequest = getAbacateChargeRequestDTO(order, abacateCustomer);

            AbacateChargeResponseDTO billingResponse;
            
            if (mockEnabled) {
                billingResponse = createMockBilling(billingRequest);
                log.info("Billing mockado criado para pedido {}: {}", order.getId(), billingResponse.getId());
            } else {
                AbacateChargeCreateResponseDTO createResponse = abacatePayClient.createBilling(billingRequest);
                billingResponse = convertToChargeResponseDTO(createResponse);
                log.info("Billing real criado para pedido {}: {}", order.getId(), billingResponse.getId());
            }

            order.setAbacateTransactionId(billingResponse.getId());
            
            return billingResponse.getPaymentLink();
            
        } catch (Exception e) {
            log.error("Erro ao criar pagamento no Abacate Pay para pedido {}: {}", order.getId(), e.getMessage());
            throw new RuntimeException("Falha na integração com gateway de pagamento", e);
        }
    }

    private AbacateChargeRequestDTO getAbacateChargeRequestDTO(Order order, AbacateCustomerDTO abacateCustomer) {
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

    private AbacateCustomerDTO getOrCreateAbacateCustomer(Order order) {
        if (mockEnabled) {
            AbacateCustomerDTO mockCustomer = new AbacateCustomerDTO();
            mockCustomer.setId("cust_" + UUID.randomUUID().toString().substring(0, 8));
            
            AbacateCustomerDTO.AbacateCustomerMetadataDTO metadata = new AbacateCustomerDTO.AbacateCustomerMetadataDTO();
            metadata.setName(order.getCustomer().getName());
            metadata.setEmail(order.getCustomer().getEmail());
            metadata.setCellphone(order.getCustomer().getPhone());
            metadata.setTaxId("");
            
            mockCustomer.setMetadata(metadata);
            return mockCustomer;
        } else {
            try {
                // Buscar cliente existente (implementação futura)
                // return abacatePayClient.getCustomerByEmail(order.getCustomer().getEmail());
                
                // Por enquanto, sempre criar novo cliente
                AbacateCustomerCreateRequestDTO customerRequest = new AbacateCustomerCreateRequestDTO();
                customerRequest.setName(order.getCustomer().getName());
                customerRequest.setEmail(order.getCustomer().getEmail());
                customerRequest.setCellphone(order.getCustomer().getPhone());
                
                // Garantir que o taxId não seja nulo ou vazio
                String taxId = order.getCustomer().getDocument();
                if (taxId == null || taxId.trim().isEmpty()) {
                    log.warn("TaxId vazio para cliente {}, usando valor padrão", order.getCustomer().getEmail());
                    taxId = "000.000.000-00"; // Valor padrão para testes
                }
                customerRequest.setTaxId(taxId);
                
                log.info("Criando cliente no Abacate Pay: name={}, email={}, cellphone={}, taxId={}", 
                    customerRequest.getName(), customerRequest.getEmail(), 
                    customerRequest.getCellphone(), customerRequest.getTaxId());
                
                AbacateCustomerCreateResponseDTO response = abacatePayClient.createCustomer(customerRequest);
                
                // Converter a resposta para AbacateCustomerDTO
                if (response != null && response.getData() != null) {
                    AbacateCustomerDTO customerDTO = new AbacateCustomerDTO();
                    customerDTO.setId(response.getData().getId());
                    customerDTO.setMetadata(convertToMetadata(response.getData().getMetadata()));
                    
                    log.info("Cliente criado com sucesso no Abacate Pay: id={}", customerDTO.getId());
                    return customerDTO;
                } else {
                    log.error("Resposta inválida do Abacate Pay: {}", response);
                    throw new RuntimeException("Resposta inválida do gateway de pagamento");
                }
            } catch (Exception e) {
                log.error("Erro ao criar/buscar cliente no Abacatepay: {}", e.getMessage());
                throw new RuntimeException("Falha ao processar cliente no gateway de pagamento", e);
            }
        }
    }
    
    private AbacateCustomerDTO.AbacateCustomerMetadataDTO convertToMetadata(
            AbacateCustomerCreateResponseDTO.AbacateCustomerDataDTO.AbacateCustomerMetadataDTO responseMetadata) {
        if (responseMetadata == null) {
            return null;
        }
        
        AbacateCustomerDTO.AbacateCustomerMetadataDTO metadata = new AbacateCustomerDTO.AbacateCustomerMetadataDTO();
        metadata.setName(responseMetadata.getName());
        metadata.setEmail(responseMetadata.getEmail());
        metadata.setCellphone(responseMetadata.getCellphone());
        metadata.setTaxId(responseMetadata.getTaxId());
        
        return metadata;
    }
    
    private AbacateChargeResponseDTO convertToChargeResponseDTO(AbacateChargeCreateResponseDTO createResponse) {
        if (createResponse == null || createResponse.getData() == null) {
            log.error("Resposta inválida do Abacate Pay: {}", createResponse != null ? createResponse.getError() : "null");
            throw new RuntimeException("Resposta inválida da API do Abacate Pay");
        }
        
        AbacateChargeCreateResponseDTO.AbacateChargeDataDTO data = createResponse.getData();
        
        AbacateChargeResponseDTO response = new AbacateChargeResponseDTO();
        response.setId(data.getId());
        response.setAmount(data.getAmount());
        response.setStatus(data.getStatus());
        response.setFrequency(data.getFrequency());
        response.setKind(data.getMethods());
        response.setPaymentLink(data.getUrl());
        response.setCreatedAt(data.getCreatedAt());
        response.setCustomer(data.getCustomer());
        
        // Mapear metadata para campos individuais
        if (data.getMetadata() != null) {
            response.setReturnUrl(data.getMetadata().getReturnUrl());
            response.setCompletionUrl(data.getMetadata().getCompletionUrl());
        }
        
        // Converter produtos
        if (data.getProducts() != null && !data.getProducts().isEmpty()) {
            // Para compatibilidade, vamos criar produtos básicos
            // Nota: A estrutura de produtos na resposta é diferente da requisição
            log.info("Billing criado com {} produtos", data.getProducts().size());
        }
        
        log.info("Billing convertido com sucesso: id={}, status={}, url={}", 
            response.getId(), response.getStatus(), response.getPaymentLink());
        
        return response;
    }
    
    private AbacateChargeResponseDTO convertGetResponseToChargeResponseDTO(AbacateChargeGetResponseDTO getResponse) {
        if (getResponse == null || getResponse.getData() == null) {
            log.error("Resposta inválida do getBilling Abacate Pay: {}", getResponse != null ? getResponse.getError() : "null");
            throw new RuntimeException("Resposta inválida da API do Abacate Pay");
        }
        
        AbacateChargeGetResponseDTO.AbacateChargeDataDTO data = getResponse.getData();
        
        AbacateChargeResponseDTO response = new AbacateChargeResponseDTO();
        response.setId(data.getId());
        response.setAmount(data.getAmount());
        response.setStatus(data.getStatus());
        response.setFrequency(data.getFrequency());
        response.setKind(data.getMethods());
        response.setPaymentLink(data.getUrl());
        response.setCreatedAt(data.getCreatedAt());
        response.setPaidAt(data.getPaidAt());
        response.setCustomer(data.getCustomer());
        
        // Mapear metadata para campos individuais
        if (data.getMetadata() != null) {
            response.setReturnUrl(data.getMetadata().getReturnUrl());
            response.setCompletionUrl(data.getMetadata().getCompletionUrl());
        }
        
        log.info("Billing obtido e convertido com sucesso: id={}, status={}", 
            response.getId(), response.getStatus());
        
        return response;
    }
    
    private AbacateChargeResponseDTO createMockBilling(AbacateChargeRequestDTO request) {
        AbacateChargeResponseDTO response = new AbacateChargeResponseDTO();
        response.setId("bill_" + UUID.randomUUID().toString().substring(0, 8));
        response.setAmount(request.getProducts().get(0).getPrice());
        response.setStatus("PENDING");
        response.setFrequency(request.getFrequency());
        response.setKind(request.getMethods());
        response.setReturnUrl(request.getReturnUrl());
        response.setCompletionUrl(request.getCompletionUrl());
        response.setCreatedAt(LocalDateTime.now());
        response.setCustomer(new AbacateCustomerDTO("cust_13455", request.getCustomer()));
        response.setProducts(request.getProducts());

        String paymentLink = baseUrl + "/mock-payment/" + response.getId();
        response.setPaymentLink(paymentLink);
        
        return response;
    }
    
    public AbacateChargeResponseDTO getBilling(String billingId) {
        if (mockEnabled) {
            AbacateChargeResponseDTO mockResponse = new AbacateChargeResponseDTO();
            mockResponse.setId(billingId);
            mockResponse.setStatus("PENDING");
            mockResponse.setFrequency("ONE_TIME");
            mockResponse.setKind(Arrays.asList("PIX"));
            mockResponse.setCreatedAt(LocalDateTime.now());
            return mockResponse;
        } else {
            AbacateChargeGetResponseDTO getResponse = abacatePayClient.getBilling(billingId);
            return convertGetResponseToChargeResponseDTO(getResponse);
        }
    }
    
    public void processWebhook(String billingId, String event, String status) {
        try {
            log.info("Processando webhook do Abacatepay - Billing: {}, Event: {}, Status: {}", 
                billingId, event, status);
            
            // Buscar o pedido pelo ID da transação
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