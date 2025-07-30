package com.kipperdev.orderhub.dto.abacate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacateCustomerDTO {

    private String id;
    private String name;
    private String email;
    private String phone;
}