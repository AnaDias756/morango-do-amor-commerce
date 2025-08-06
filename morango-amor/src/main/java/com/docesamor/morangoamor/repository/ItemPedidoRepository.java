package com.docesamor.morangoamor.repository;

import com.docesamor.morangoamor.entity.Doce;
import com.docesamor.morangoamor.entity.ItemPedido;
import com.docesamor.morangoamor.entity.Pedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    // Buscar itens por pedido
    List<ItemPedido> findByPedidoOrderByIdAsc(Pedido pedido);

    // Buscar itens por doce
    List<ItemPedido> findByDoceOrderByPedido_CreatedAtDesc(Doce doce);

    // Buscar itens por pedido ID
    List<ItemPedido> findByPedido_IdOrderByIdAsc(Long pedidoId);

    // Buscar itens por doce ID
    List<ItemPedido> findByDoce_IdOrderByPedido_CreatedAtDesc(Long doceId);

    // Estatísticas de vendas por doce
    @Query("SELECT i.doce, SUM(i.quantidade), COUNT(DISTINCT i.pedido) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "GROUP BY i.doce ORDER BY SUM(i.quantidade) DESC")
    List<Object[]> getEstatisticasVendasPorDoce();

    // Doces mais vendidos
    @Query("SELECT i.doce, SUM(i.quantidade) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "GROUP BY i.doce ORDER BY SUM(i.quantidade) DESC")
    List<Object[]> getDocesMaisVendidos(Pageable pageable);

    // Doces mais vendidos por período
    @Query("SELECT i.doce, SUM(i.quantidade) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "AND i.pedido.createdAt BETWEEN :inicio AND :fim " +
           "GROUP BY i.doce ORDER BY SUM(i.quantidade) DESC")
    List<Object[]> getDocesMaisVendidosPorPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        Pageable pageable
    );

    // Receita por doce
    @Query("SELECT i.doce, SUM(i.precoTotal) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "GROUP BY i.doce ORDER BY SUM(i.precoTotal) DESC")
    List<Object[]> getReceitaPorDoce();

    // Receita por doce em período
    @Query("SELECT i.doce, SUM(i.precoTotal) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "AND i.pedido.createdAt BETWEEN :inicio AND :fim " +
           "GROUP BY i.doce ORDER BY SUM(i.precoTotal) DESC")
    List<Object[]> getReceitaPorDocePorPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    // Quantidade total vendida por doce
    @Query("SELECT SUM(i.quantidade) FROM ItemPedido i " +
           "WHERE i.doce = :doce AND i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE")
    Long getTotalVendidoPorDoce(@Param("doce") Doce doce);

    // Quantidade vendida por doce em período
    @Query("SELECT SUM(i.quantidade) FROM ItemPedido i " +
           "WHERE i.doce = :doce " +
           "AND i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "AND i.pedido.createdAt BETWEEN :inicio AND :fim")
    Long getTotalVendidoPorDocePorPeriodo(
        @Param("doce") Doce doce,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    // Itens com personalizações
    @Query("SELECT i FROM ItemPedido i WHERE i.personalizacao IS NOT NULL AND i.personalizacao != ''")
    List<ItemPedido> findItensComPersonalizacao();

    // Itens com observações especiais
    @Query("SELECT i FROM ItemPedido i WHERE i.observacoesItem IS NOT NULL AND i.observacoesItem != ''")
    List<ItemPedido> findItensComObservacoes();

    // Média de itens por pedido
    @Query("SELECT AVG(subquery.totalItens) FROM (" +
           "SELECT COUNT(i) as totalItens FROM ItemPedido i GROUP BY i.pedido" +
           ") subquery")
    Double getMediaItensPorPedido();

    // Ticket médio por item
    @Query("SELECT AVG(i.precoTotal) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE")
    Double getTicketMedioPorItem();

    // Doces vendidos por tipo
    @Query("SELECT i.doce.tipo, SUM(i.quantidade) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "GROUP BY i.doce.tipo ORDER BY SUM(i.quantidade) DESC")
    List<Object[]> getVendasPorTipoDoce();

    // Doces vendidos por sabor
    @Query("SELECT i.doce.sabor, SUM(i.quantidade) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "GROUP BY i.doce.sabor ORDER BY SUM(i.quantidade) DESC")
    List<Object[]> getVendasPorSaborMorango();

    // Itens mais personalizados
    @Query("SELECT i.doce, COUNT(i) FROM ItemPedido i " +
           "WHERE i.personalizacao IS NOT NULL AND i.personalizacao != '' " +
           "GROUP BY i.doce ORDER BY COUNT(i) DESC")
    List<Object[]> getDocesMaisPersonalizados(Pageable pageable);

    // Vendas por hora do dia
    @Query("SELECT HOUR(i.pedido.createdAt), SUM(i.quantidade) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "GROUP BY HOUR(i.pedido.createdAt) ORDER BY HOUR(i.pedido.createdAt)")
    List<Object[]> getVendasPorHora();

    // Vendas por dia da semana
    @Query("SELECT DAYOFWEEK(i.pedido.createdAt), SUM(i.quantidade) FROM ItemPedido i " +
           "WHERE i.pedido.status = com.docesamor.morangoamor.entity.StatusPedido.ENTREGUE " +
           "GROUP BY DAYOFWEEK(i.pedido.createdAt) ORDER BY DAYOFWEEK(i.pedido.createdAt)")
    List<Object[]> getVendasPorDiaSemana();

    // Histórico de preços por doce
    @Query("SELECT i.doce, i.precoUnitario, i.pedido.createdAt FROM ItemPedido i " +
           "WHERE i.doce = :doce " +
           "ORDER BY i.pedido.createdAt DESC")
    List<Object[]> getHistoricoPrecosPorDoce(@Param("doce") Doce doce);
}