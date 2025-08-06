package com.docesamor.morangoamor.service;

import com.docesamor.morangoamor.dto.DoceDTO;
import com.docesamor.morangoamor.entity.Doce;
import com.docesamor.morangoamor.entity.SaborMorango;
import com.docesamor.morangoamor.entity.TipoDoce;
import com.docesamor.morangoamor.mapper.DoceMapper;
import com.docesamor.morangoamor.repository.DoceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DoceService {

    private final DoceRepository doceRepository;
    private final DoceMapper doceMapper;

    public Page<DoceDTO> listarDoces(
            TipoDoce tipo,
            SaborMorango sabor,
            BigDecimal precoMinimo,
            BigDecimal precoMaximo,
            Boolean disponivel,
            Pageable pageable) {
        
        log.debug("Listando doces com filtros - tipo: {}, sabor: {}, preço: {} a {}, disponível: {}", 
            tipo, sabor, precoMinimo, precoMaximo, disponivel);
        
        Page<Doce> doces = doceRepository.findWithFilters(
            tipo, sabor, precoMinimo, precoMaximo, disponivel, pageable);
        
        return doces.map(doceMapper::toDTO);
    }

    public Optional<DoceDTO> buscarDocePorId(Long id) {
        log.debug("Buscando doce por ID: {}", id);
        
        return doceRepository.findById(id)
            .map(doceMapper::toDTO);
    }

    public List<DoceDTO> listarDocesDisponiveis() {
        log.debug("Listando doces disponíveis");
        
        return doceRepository.findByDisponivelTrue()
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> listarDocesPorTipo(TipoDoce tipo) {
        log.debug("Listando doces por tipo: {}", tipo);
        
        return doceRepository.findByTipoAndDisponivelTrue(tipo)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> listarDocesPorSabor(SaborMorango sabor) {
        log.debug("Listando doces por sabor: {}", sabor);
        
        return doceRepository.findBySaborAndDisponivelTrue(sabor)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> listarDocesPromocao() {
        log.debug("Listando doces em promoção");
        
        // Considera doces com preço abaixo de R$ 15,00 como promoção
        BigDecimal precoPromocao = new BigDecimal("15.00");
        
        return doceRepository.findDocesEmPromocao(precoPromocao)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> listarDocesPopulares() {
        log.debug("Listando doces populares");
        
        // Retorna os 10 doces mais recentes como "populares"
        // Em uma implementação real, seria baseado em vendas
        Pageable pageable = PageRequest.of(0, 10);
        
        return doceRepository.findDocesPopulares(pageable)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> listarDocesEstoqueBaixo() {
        log.debug("Listando doces com estoque baixo");
        
        return doceRepository.findDocesComEstoqueBaixo()
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> buscarDocesPorNome(String nome) {
        log.debug("Buscando doces por nome: {}", nome);
        
        return doceRepository.findByNomeContainingIgnoreCaseAndDisponivelTrue(nome)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> buscarDocesPorIngrediente(String ingrediente) {
        log.debug("Buscando doces por ingrediente: {}", ingrediente);
        
        return doceRepository.findByIngredientesEspeciaisContainingIgnoreCaseAndDisponivelTrue(ingrediente)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> listarDocesPorFaixaPreco(BigDecimal precoMinimo, BigDecimal precoMaximo) {
        log.debug("Listando doces por faixa de preço: {} a {}", precoMinimo, precoMaximo);
        
        return doceRepository.findByPrecoBetweenAndDisponivelTrue(precoMinimo, precoMaximo)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> listarDocesPorTempoPreparoMaximo(Integer tempoMaximo) {
        log.debug("Listando doces com tempo de preparo máximo: {} minutos", tempoMaximo);
        
        return doceRepository.findByTempoPreparoMinutosLessThanEqualAndDisponivelTrue(tempoMaximo)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> listarDocesPorFaixaCalorias(Integer caloriaMinima, Integer caloriaMaxima) {
        log.debug("Listando doces por faixa de calorias: {} a {}", caloriaMinima, caloriaMaxima);
        
        return doceRepository.findByCaloriasPorUnidadeBetweenAndDisponivelTrue(caloriaMinima, caloriaMaxima)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<DoceDTO> listarDocesPorFaixaPeso(Integer pesoMinimo, Integer pesoMaximo) {
        log.debug("Listando doces por faixa de peso: {}g a {}g", pesoMinimo, pesoMaximo);
        
        return doceRepository.findByPesoGramasBetweenAndDisponivelTrue(pesoMinimo, pesoMaximo)
            .stream()
            .map(doceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public boolean verificarDisponibilidade(Long doceId, Integer quantidade) {
        log.debug("Verificando disponibilidade do doce {} para quantidade: {}", doceId, quantidade);
        
        Optional<Doce> doceOpt = doceRepository.findById(doceId);
        
        if (doceOpt.isEmpty()) {
            log.warn("Doce não encontrado: {}", doceId);
            return false;
        }
        
        Doce doce = doceOpt.get();
        
        if (!doce.getDisponivel()) {
            log.warn("Doce {} não está disponível", doceId);
            return false;
        }
        
        if (doce.getEstoqueAtual() < quantidade) {
            log.warn("Estoque insuficiente para doce {}. Disponível: {}, Solicitado: {}", 
                doceId, doce.getEstoqueAtual(), quantidade);
            return false;
        }
        
        return true;
    }

    @Transactional
    public void reduzirEstoque(Long doceId, Integer quantidade) {
        log.debug("Reduzindo estoque do doce {} em {} unidades", doceId, quantidade);
        
        Doce doce = doceRepository.findById(doceId)
            .orElseThrow(() -> new RuntimeException("Doce não encontrado: " + doceId));
        
        if (!verificarDisponibilidade(doceId, quantidade)) {
            throw new RuntimeException("Estoque insuficiente para o doce: " + doce.getNome());
        }
        
        doce.setEstoqueAtual(doce.getEstoqueAtual() - quantidade);
        doceRepository.save(doce);
        
        log.info("Estoque do doce {} reduzido em {} unidades. Estoque atual: {}", 
            doce.getNome(), quantidade, doce.getEstoqueAtual());
    }

    @Transactional
    public void aumentarEstoque(Long doceId, Integer quantidade) {
        log.debug("Aumentando estoque do doce {} em {} unidades", doceId, quantidade);
        
        Doce doce = doceRepository.findById(doceId)
            .orElseThrow(() -> new RuntimeException("Doce não encontrado: " + doceId));
        
        doce.setEstoqueAtual(doce.getEstoqueAtual() + quantidade);
        doceRepository.save(doce);
        
        log.info("Estoque do doce {} aumentado em {} unidades. Estoque atual: {}", 
            doce.getNome(), quantidade, doce.getEstoqueAtual());
    }

    public Optional<DoceDTO> buscarDocePorNomeExato(String nome) {
        log.debug("Buscando doce por nome exato: {}", nome);
        
        return doceRepository.findByNomeIgnoreCaseAndDisponivelTrue(nome)
            .map(doceMapper::toDTO);
    }

    public boolean existeDocePorNome(String nome) {
        log.debug("Verificando se existe doce com nome: {}", nome);
        
        return doceRepository.existsByNomeIgnoreCase(nome);
    }
}