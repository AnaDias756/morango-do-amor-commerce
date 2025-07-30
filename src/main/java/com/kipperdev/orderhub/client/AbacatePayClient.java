package com.kipperdev.orderhub.client;

import com.kipperdev.orderhub.dto.abacate.AbacateChargeRequestDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateChargeResponseDTO;
import com.kipperdev.orderhub.dto.abacate.AbacateCustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "abacate-pay", url = "${abacate.api.url:https://api.abacatepay.com}")
public interface AbacatePayClient {

    @PostMapping("/customers")
    AbacateCustomerDTO createCustomer(@RequestBody AbacateCustomerDTO customer);

    @GetMapping("/customers/{id}")
    AbacateCustomerDTO getCustomer(@PathVariable("id") String customerId);

    @GetMapping("/customers/email/{email}")
    AbacateCustomerDTO getCustomerByEmail(@PathVariable("email") String email);

    @PostMapping("/charges")
    AbacateChargeResponseDTO createCharge(@RequestBody AbacateChargeRequestDTO chargeRequest);

    @GetMapping("/charges/{id}")
    AbacateChargeResponseDTO getCharge(@PathVariable("id") String chargeId);

    @PostMapping("/charges/{id}/cancel")
    AbacateChargeResponseDTO cancelCharge(@PathVariable("id") String chargeId);
}