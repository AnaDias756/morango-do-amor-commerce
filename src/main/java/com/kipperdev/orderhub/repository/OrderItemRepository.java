package com.kipperdev.orderhub.repository;

import com.kipperdev.orderhub.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.productSku = :sku")
    List<OrderItem> findByProductSku(@Param("sku") String sku);
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productSku = :sku")
    Integer getTotalQuantityByProductSku(@Param("sku") String sku);
}