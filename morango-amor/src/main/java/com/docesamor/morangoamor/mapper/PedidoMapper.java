package com.docesamor.morangoamor.mapper;

import com.docesamor.morangoamor.dto.ItemPedidoDTO;
import com.docesamor.morangoamor.dto.PedidoResponseDTO;
import com.docesamor.morangoamor.entity.ItemPedido;
import com.docesamor.morangoamor.entity.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ClienteMapper.class})
public interface PedidoMapper {

    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "itens", source = "itens", qualifiedByName = "mapItens")
    @Mapping(target = "statusDescricao", expression = "java(pedido.getStatus().getDescricao())")
    PedidoResponseDTO toResponseDTO(Pedido pedido);

    @Named("mapItens")
    default List<ItemPedidoDTO> mapItens(List<ItemPedido> itens) {
        if (itens == null) {
            return null;
        }
        
        return itens.stream().map(item -> {
            ItemPedidoDTO dto = new ItemPedidoDTO();
            dto.setDoceId(item.getDoce().getId());
            dto.setNomeDoce(item.getDoce().getNome());
            dto.setQuantidade(item.getQuantidade());
            dto.setPrecoUnitario(item.getPrecoUnitario());
            dto.setPrecoTotal(item.getPrecoTotal());
            dto.setObservacoesItem(item.getObservacoesItem());
            dto.setPersonalizacao(item.getPersonalizacao());
            return dto;
        }).collect(Collectors.toList());
    }
}