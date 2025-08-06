package com.docesamor.morangoamor.repository;

import com.docesamor.morangoamor.entity.Doce;
import com.docesamor.morangoamor.entity.SaborMorango;
import com.docesamor.morangoamor.entity.TipoDoce;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoceRepository extends JpaRepository<Doce, Long> {

    // Buscar doces disponíveis
    List<Doce> findByDisponivelTrue();

    // Buscar por tipo
    List<Doce> findByTipoAndDisponivelTrue(TipoDoce tipo);

    // Buscar por sabor
    List<Doce> findBySaborAndDisponivelTrue(SaborMorango sabor);

    // Buscar por faixa de preço
    List<Doce> findByPrecoBetweenAndDisponivelTrue(BigDecimal precoMinimo, BigDecimal precoMaximo);

    // Buscar doces com estoque baixo
    @Query("SELECT d FROM Doce d WHERE d.estoqueAtual <= d.estoqueMinimo AND d.disponivel = true")
    List<Doce> findDocesComEstoqueBaixo();

    // Buscar doces em promoção (exemplo: preço menor que um valor específico)
    @Query("SELECT d FROM Doce d WHERE d.preco < :precoPromocao AND d.disponivel = true ORDER BY d.preco ASC")
    List<Doce> findDocesEmPromocao(@Param("precoPromocao") BigDecimal precoPromocao);

    // Buscar doces mais populares (baseado em vendas - seria necessário implementar lógica de vendas)
    @Query("SELECT d FROM Doce d WHERE d.disponivel = true ORDER BY d.createdAt DESC")
    List<Doce> findDocesPopulares(Pageable pageable);

    // Buscar com filtros combinados
    @Query("SELECT d FROM Doce d WHERE " +
           "(:tipo IS NULL OR d.tipo = :tipo) AND " +
           "(:sabor IS NULL OR d.sabor = :sabor) AND " +
           "(:precoMinimo IS NULL OR d.preco >= :precoMinimo) AND " +
           "(:precoMaximo IS NULL OR d.preco <= :precoMaximo) AND " +
           "(:disponivel IS NULL OR d.disponivel = :disponivel)")
    Page<Doce> findWithFilters(
        @Param("tipo") TipoDoce tipo,
        @Param("sabor") SaborMorango sabor,
        @Param("precoMinimo") BigDecimal precoMinimo,
        @Param("precoMaximo") BigDecimal precoMaximo,
        @Param("disponivel") Boolean disponivel,
        Pageable pageable
    );

    // Buscar por nome (para busca textual)
    @Query("SELECT d FROM Doce d WHERE LOWER(d.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND d.disponivel = true")
    List<Doce> findByNomeContainingIgnoreCaseAndDisponivelTrue(@Param("nome") String nome);

    // Buscar por ingredientes especiais
    @Query("SELECT d FROM Doce d WHERE LOWER(d.ingredientesEspeciais) LIKE LOWER(CONCAT('%', :ingrediente, '%')) AND d.disponivel = true")
    List<Doce> findByIngredientesEspeciaisContainingIgnoreCaseAndDisponivelTrue(@Param("ingrediente") String ingrediente);

    // Buscar doces por tempo de preparo
    List<Doce> findByTempoPreparoMinutosLessThanEqualAndDisponivelTrue(Integer tempoMaximo);

    // Buscar doces por faixa de calorias
    List<Doce> findByCaloriasPorUnidadeBetweenAndDisponivelTrue(Integer caloriaMinima, Integer caloriaMaxima);

    // Verificar se existe doce com nome específico
    boolean existsByNomeIgnoreCase(String nome);

    // Buscar doce por nome exato
    Optional<Doce> findByNomeIgnoreCaseAndDisponivelTrue(String nome);

    // Contar doces por tipo
    @Query("SELECT d.tipo, COUNT(d) FROM Doce d WHERE d.disponivel = true GROUP BY d.tipo")
    List<Object[]> countDocesByTipo();

    // Contar doces por sabor
    @Query("SELECT d.sabor, COUNT(d) FROM Doce d WHERE d.disponivel = true GROUP BY d.sabor")
    List<Object[]> countDocesBySabor();

    // Buscar doces com maior estoque
    @Query("SELECT d FROM Doce d WHERE d.disponivel = true ORDER BY d.estoqueAtual DESC")
    List<Doce> findDocesComMaiorEstoque(Pageable pageable);

    // Buscar doces por peso
    List<Doce> findByPesoGramasBetweenAndDisponivelTrue(Integer pesoMinimo, Integer pesoMaximo);
}