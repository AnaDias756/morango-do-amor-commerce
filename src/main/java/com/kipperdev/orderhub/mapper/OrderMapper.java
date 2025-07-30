package com.kipperdev.orderhub.mapper;

import com.kipperdev.orderhub.dto.*;
import com.kipperdev.orderhub.entity.Customer;
import com.kipperdev.orderhub.entity.Order;
import com.kipperdev.orderhub.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // Customer mappings
    CustomerDTO toCustomerDTO(Customer customer);
    Customer toCustomerEntity(CustomerDTO customerDTO);
    List<CustomerDTO> toCustomerDTOList(List<Customer> customers);

    // OrderItem mappings
    OrderItemDTO toOrderItemDTO(OrderItem orderItem);
    OrderItem toOrderItemEntity(OrderItemDTO orderItemDTO);
    List<OrderItemDTO> toOrderItemDTOList(List<OrderItem> orderItems);

    // Order mappings
    @Mapping(target = "statusUrl", source = "id", qualifiedByName = "generateStatusUrl")
    OrderResponseDTO toOrderResponseDTO(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "abacateTransactionId", ignore = true)
    @Mapping(target = "paymentLink", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    Order toOrderEntity(CreateOrderRequestDTO createOrderRequestDTO);

    @Mapping(target = "statusDescription", source = "status", qualifiedByName = "getStatusDescription")
    @Mapping(target = "customerEmail", source = "customer.email")
    OrderStatusDTO toOrderStatusDTO(Order order);

    List<OrderResponseDTO> toOrderResponseDTOList(List<Order> orders);

    @Named("generateStatusUrl")
    default String generateStatusUrl(Long orderId) {
        return "/public/orders/" + orderId + "/status";
    }

    @Named("getStatusDescription")
    default String getStatusDescription(com.kipperdev.orderhub.entity.OrderStatus status) {
        return status != null ? status.getDescription() : null;
    }
}