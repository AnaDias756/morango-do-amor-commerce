package com.docesamor.morangoamor.mapper;

import com.docesamor.morangoamor.dto.ClienteDTO;
import com.docesamor.morangoamor.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteDTO toDTO(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteVip", ignore = true)
    @Mapping(target = "totalPedidos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cliente toEntity(ClienteDTO clienteDTO);
}