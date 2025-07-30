package com.kipperdev.orderhub.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.kipperdev.orderhub.config.AbacatePayFeignConfig;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeRequestDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeResponseDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeCreateResponseDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeGetResponseDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerCreateRequestDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerCreateResponseDTO;

@FeignClient(
    name = "abacate-pay", 
    url = "${abacate.api.base-url:https://api.abacatepay.com}",
    configuration = AbacatePayFeignConfig.class
)
public interface AbacatePayClient {

    @PostMapping("/v1/customer/create")
    AbacateCustomerCreateResponseDTO createCustomer(@RequestBody AbacateCustomerCreateRequestDTO customer);

    @GetMapping("/v1/customers/{id}")
    AbacateCustomerDTO getCustomer(@PathVariable("id") String customerId);

    @PostMapping("/v1/billing/create")
    AbacateChargeCreateResponseDTO createBilling(@RequestBody AbacateChargeRequestDTO billingRequest);

    @GetMapping("/v1/billing/{id}")
    AbacateChargeGetResponseDTO getBilling(@PathVariable("id") String billingId);

    @PostMapping("/v1/billing/{id}/cancel")
    AbacateChargeGetResponseDTO cancelBilling(@PathVariable("id") String billingId);
}