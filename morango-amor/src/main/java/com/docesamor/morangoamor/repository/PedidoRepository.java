package com.docesamor.morangoamor.repository;

import com.docesamor.morangoamor.entity.Cliente;
import com.docesamor.morangoamor.entity.FormaPagamento;
import com.docesamor.morangoamor.entity.Pedido;
import com.docesamor.morangoamor.entity.StatusPedido;
import com.docesamor.morangoamor.entity.TipoEntrega;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Buscar pedidos por cliente
    List<Pedido> findByClienteOrderByCreatedAtDesc(Cliente cliente);

    // Buscar pedidos por email do cliente
    @Query("SELECT p FROM Pedido p WHERE p.cliente.email = :email ORDER BY p.createdAt DESC")
    List<Pedido> findByClienteEmailOrderByCreatedAtDesc(@Param("email") String email);

    // Buscar pedidos por status
    List<Pedido> findByStatusOrderByCreatedAtDesc(StatusPedido status);

    // Buscar pedidos por status e cliente
    List<Pedido> findByStatusAndClienteOrderByCreatedAtDesc(StatusPedido status, Cliente cliente);

    // Buscar pedidos por período
    List<Pedido> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime inicio, LocalDateTime fim);

    // Buscar pedidos por forma de pagamento
    List<Pedido> findByFormaPagamentoOrderByCreatedAtDesc(FormaPagamento formaPagamento);

    // Buscar pedidos por tipo de entrega
    List<Pedido> findByTipoEntregaOrderByCreatedAtDesc(TipoEntrega tipoEntrega);

    // Buscar pedidos por transaction ID do Abacate Pay
    Optional<Pedido> findByAbacateTransactionId(String transactionId);

    // Buscar pedidos por faixa de valor
    List<Pedido> findByValorFinalBetweenOrderByCreatedAtDesc(BigDecimal valorMinimo, BigDecimal valorMaximo);

    // Buscar pedidos com filtros combinados
    @Query("SELECT p FROM Pedido p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:clienteEmail IS NULL OR LOWER(p.cliente.email) LIKE LOWER(CONCAT('%', :clienteEmail, '%'))) AND " +
           "(:clienteNome IS NULL OR LOWER(p.cliente.nome) LIKE LOWER(CONCAT('%', :clienteNome, '%'))) AND " +
           "(:dataInicio IS NULL OR p.createdAt >= :dataInicio) AND " +
           "(:dataFim IS NULL OR p.createdAt <= :dataFim) " +
           "ORDER BY p.createdAt DESC")
    Page<Pedido> findWithFilters(
        @Param("status") StatusPedido status,
        @Param("clienteEmail") String clienteEmail,
        @Param("clienteNome") String clienteNome,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        Pageable pageable
    );

    // Estatísticas de pedidos
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.status = :status")
    Long countByStatus(@Param("status") StatusPedido status);

    @Query("SELECT SUM(p.valorFinal) FROM Pedido p WHERE p.status = :status")
    BigDecimal sumValorFinalByStatus(@Param("status") StatusPedido status);

    @Query("SELECT SUM(p.valorFinal) FROM Pedido p WHERE p.createdAt BETWEEN :inicio AND :fim AND p.status = :status")
    BigDecimal sumValorFinalByPeriodoAndStatus(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("status") StatusPedido status
    );

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.createdAt BETWEEN :inicio AND :fim")
    Long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT AVG(p.valorFinal) FROM Pedido p WHERE p.status = :status")
    BigDecimal avgValorFinalByStatus(@Param("status") StatusPedido status);

    // Pedidos por forma de pagamento (estatísticas)
    @Query("SELECT p.formaPagamento, COUNT(p) FROM Pedido p GROUP BY p.formaPagamento ORDER BY COUNT(p) DESC")
    List<Object[]> countByFormaPagamento();

    // Pedidos por tipo de entrega (estatísticas)
    @Query("SELECT p.tipoEntrega, COUNT(p) FROM Pedido p GROUP BY p.tipoEntrega ORDER BY COUNT(p) DESC")
    List<Object[]> countByTipoEntrega();

    // Pedidos por status (estatísticas)
    @Query("SELECT p.status, COUNT(p) FROM Pedido p GROUP BY p.status ORDER BY COUNT(p) DESC")
    List<Object[]> countByStatusGroup();

    // Vendas por período (diárias)
    @Query("SELECT DATE(p.createdAt), COUNT(p), SUM(p.valorFinal) FROM Pedido p " +
           "WHERE p.createdAt BETWEEN :inicio AND :fim AND p.status = :status " +
           "GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt)")
    List<Object[]> getVendasDiarias(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("status") StatusPedido status
    );

    // Top clientes por valor gasto
    @Query("SELECT p.cliente, SUM(p.valorFinal), COUNT(p) FROM Pedido p " +
           "WHERE p.status = :status " +
           "GROUP BY p.cliente ORDER BY SUM(p.valorFinal) DESC")
    List<Object[]> getTopClientesPorValor(@Param("status") StatusPedido status, Pageable pageable);

    // Pedidos pendentes de pagamento há mais de X horas
    @Query("SELECT p FROM Pedido p WHERE p.status = :status AND p.createdAt < :dataLimite")
    List<Pedido> findPedidosPendentesAntigos(
        @Param("status") StatusPedido status,
        @Param("dataLimite") LocalDateTime dataLimite
    );

    // Pedidos para entrega hoje
    @Query("SELECT p FROM Pedido p WHERE DATE(p.dataEntregaPrevista) = CURRENT_DATE AND p.status IN :statusValidos")
    List<Pedido> findPedidosParaEntregaHoje(@Param("statusValidos") List<StatusPedido> statusValidos);

    // Pedidos em preparação
    @Query("SELECT p FROM Pedido p WHERE p.status = :status ORDER BY p.createdAt ASC")
    List<Pedido> findPedidosEmPreparacao(@Param("status") StatusPedido status);

    // Tempo médio de preparo por tipo de entrega
    @Query("SELECT p.tipoEntrega, AVG(p.tempoPreparoEstimado) FROM Pedido p " +
           "WHERE p.status = :status " +
           "GROUP BY p.tipoEntrega")
    List<Object[]> getTempoMedioPreparoPorTipoEntrega(@Param("status") StatusPedido status);

    // Buscar últimos pedidos do cliente
    @Query("SELECT p FROM Pedido p WHERE p.cliente = :cliente ORDER BY p.createdAt DESC")
    List<Pedido> findUltimosPedidosCliente(@Param("cliente") Cliente cliente, Pageable pageable);
}