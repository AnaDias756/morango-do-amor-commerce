package com.docesamor.morangoamor.mapper;

import com.docesamor.morangoamor.dto.DoceDTO;
import com.docesamor.morangoamor.entity.Doce;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DoceMapper {

    @Mapping(target = "tipo", source = "tipo", qualifiedByName = "tipoComDescricao")
    @Mapping(target = "sabor", source = "sabor", qualifiedByName = "saborComDescricao")
    @Mapping(target = "estoqueBaixo", expression = "java(doce.isEstoqueBaixo())")
    DoceDTO toDTO(Doce doce);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Doce toEntity(DoceDTO doceDTO);

    @Named("tipoComDescricao")
    default DoceDTO.TipoDoceDTO mapTipoComDescricao(com.docesamor.morangoamor.entity.TipoDoce tipo) {
        if (tipo == null) {
            return null;
        }
        DoceDTO.TipoDoceDTO tipoDTO = new DoceDTO.TipoDoceDTO();
        tipoDTO.setCodigo(tipo.name());
        tipoDTO.setDescricao(tipo.getDescricao());
        return tipoDTO;
    }

    @Named("saborComDescricao")
    default DoceDTO.SaborMorangoDTO mapSaborComDescricao(com.docesamor.morangoamor.entity.SaborMorango sabor) {
        if (sabor == null) {
            return null;
        }
        DoceDTO.SaborMorangoDTO saborDTO = new DoceDTO.SaborMorangoDTO();
        saborDTO.setCodigo(sabor.name());
        saborDTO.setDescricao(sabor.getDescricao());
        return saborDTO;
    }
}