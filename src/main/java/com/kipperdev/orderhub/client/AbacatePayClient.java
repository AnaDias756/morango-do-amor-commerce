package com.kipperdev.orderhub.client;

import com.kipperdev.orderhub.dto.abacate.AbacateChargeResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.kipperdev.orderhub.config.AbacatePayFeignConfig;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeRequestDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerResponseDTO;

@FeignClient(
    name = "abacate-pay", 
    url = "${abacate.api.base-url:https://api.abacatepay.com}",
    configuration = AbacatePayFeignConfig.class
)
public interface AbacatePayClient {

    @PostMapping("/v1/customer/create")
    AbacateCustomerResponseDTO createCustomer(@RequestBody AbacateCustomerDTO customer);

    @GetMapping("/v1/customers/{id}")
    AbacateCustomerResponseDTO getCustomer(@PathVariable("id") String customerId);

    @PostMapping("/v1/billing/create")
    AbacateChargeResponseDTO createBilling(@RequestBody AbacateChargeRequestDTO billingRequest);

    @GetMapping("/v1/billing/{id}")
    AbacateChargeResponseDTO getBilling(@PathVariable("id") String billingId);
}