package com.docesamor.morangoamor.repository;

import com.docesamor.morangoamor.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar cliente por email
    Optional<Cliente> findByEmailIgnoreCase(String email);

    // Verificar se existe cliente com email
    boolean existsByEmailIgnoreCase(String email);

    // Buscar cliente por telefone
    Optional<Cliente> findByTelefone(String telefone);

    // Buscar clientes por nome (busca parcial)
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Cliente> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    // Buscar clientes VIP
    List<Cliente> findByClienteVipTrue();

    // Buscar clientes por cidade
    List<Cliente> findByCidadeIgnoreCase(String cidade);

    // Buscar clientes por estado
    List<Cliente> findByEstadoIgnoreCase(String estado);

    // Buscar clientes por CEP
    List<Cliente> findByCep(String cep);

    // Buscar clientes aniversariantes do mês
    @Query("SELECT c FROM Cliente c WHERE MONTH(c.dataNascimento) = :mes")
    List<Cliente> findAniversariantesDoMes(@Param("mes") int mes);

    // Buscar clientes aniversariantes de hoje
    @Query("SELECT c FROM Cliente c WHERE MONTH(c.dataNascimento) = MONTH(CURRENT_DATE) AND DAY(c.dataNascimento) = DAY(CURRENT_DATE)")
    List<Cliente> findAniversariantesHoje();

    // Buscar clientes por faixa de idade
    @Query("SELECT c FROM Cliente c WHERE YEAR(CURRENT_DATE) - YEAR(c.dataNascimento) BETWEEN :idadeMinima AND :idadeMaxima")
    List<Cliente> findByIdadeBetween(@Param("idadeMinima") int idadeMinima, @Param("idadeMaxima") int idadeMaxima);

    // Buscar clientes com alergias específicas
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.alergias) LIKE LOWER(CONCAT('%', :alergia, '%'))")
    List<Cliente> findByAlergiasContainingIgnoreCase(@Param("alergia") String alergia);

    // Buscar clientes com preferências específicas
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.preferenciasDoces) LIKE LOWER(CONCAT('%', :preferencia, '%'))")
    List<Cliente> findByPreferenciasDocesContainingIgnoreCase(@Param("preferencia") String preferencia);

    // Buscar clientes por número mínimo de pedidos
    List<Cliente> findByTotalPedidosGreaterThanEqual(Integer totalMinimo);

    // Buscar clientes cadastrados em um período
    List<Cliente> findByCreatedAtBetween(LocalDate dataInicio, LocalDate dataFim);

    // Buscar clientes mais ativos (com mais pedidos)
    @Query("SELECT c FROM Cliente c ORDER BY c.totalPedidos DESC")
    List<Cliente> findClientesMaisAtivos(Pageable pageable);

    // Buscar clientes novos (cadastrados recentemente)
    @Query("SELECT c FROM Cliente c WHERE c.createdAt >= :dataLimite ORDER BY c.createdAt DESC")
    List<Cliente> findClientesNovos(@Param("dataLimite") LocalDate dataLimite);

    // Buscar clientes inativos (sem pedidos há muito tempo)
    @Query("SELECT c FROM Cliente c WHERE c.totalPedidos = 0 OR c.updatedAt < :dataLimite")
    List<Cliente> findClientesInativos(@Param("dataLimite") LocalDate dataLimite);

    // Contar clientes por cidade
    @Query("SELECT c.cidade, COUNT(c) FROM Cliente c GROUP BY c.cidade ORDER BY COUNT(c) DESC")
    List<Object[]> countClientesByCidade();

    // Contar clientes por estado
    @Query("SELECT c.estado, COUNT(c) FROM Cliente c GROUP BY c.estado ORDER BY COUNT(c) DESC")
    List<Object[]> countClientesByEstado();

    // Buscar clientes com filtros combinados
    @Query("SELECT c FROM Cliente c WHERE " +
           "(:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:cidade IS NULL OR LOWER(c.cidade) = LOWER(:cidade)) AND " +
           "(:estado IS NULL OR LOWER(c.estado) = LOWER(:estado)) AND " +
           "(:clienteVip IS NULL OR c.clienteVip = :clienteVip)")
    Page<Cliente> findWithFilters(
        @Param("nome") String nome,
        @Param("email") String email,
        @Param("cidade") String cidade,
        @Param("estado") String estado,
        @Param("clienteVip") Boolean clienteVip,
        Pageable pageable
    );

    // Estatísticas de clientes
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.clienteVip = true")
    Long countClientesVip();

    @Query("SELECT AVG(c.totalPedidos) FROM Cliente c")
    Double getMediaPedidosPorCliente();

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.createdAt >= :dataInicio")
    Long countClientesNovos(@Param("dataInicio") LocalDate dataInicio);
}