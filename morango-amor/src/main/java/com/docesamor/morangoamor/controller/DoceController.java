package com.docesamor.morangoamor.controller;

import com.docesamor.morangoamor.dto.DoceDTO;
import com.docesamor.morangoamor.entity.SaborMorango;
import com.docesamor.morangoamor.entity.TipoDoce;
import com.docesamor.morangoamor.service.DoceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/doces")
@RequiredArgsConstructor
@Slf4j
public class DoceController {

    private final DoceService doceService;

    @GetMapping
    public ResponseEntity<Page<DoceDTO>> listarDoces(
            @RequestParam(required = false) TipoDoce tipo,
            @RequestParam(required = false) SaborMorango sabor,
            @RequestParam(required = false) BigDecimal precoMinimo,
            @RequestParam(required = false) BigDecimal precoMaximo,
            @RequestParam(required = false, defaultValue = "true") Boolean disponivel,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Listando doces - tipo: {}, sabor: {}, preço: {} a {}, disponível: {}", 
            tipo, sabor, precoMinimo, precoMaximo, disponivel);
        
        Page<DoceDTO> doces = doceService.listarDoces(tipo, sabor, precoMinimo, precoMaximo, disponivel, pageable);
        
        log.info("Retornando {} doces de {} total", doces.getNumberOfElements(), doces.getTotalElements());
        
        return ResponseEntity.ok(doces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoceDTO> buscarDoce(@PathVariable Long id) {
        log.info("Consultando doce: {}", id);
        
        return doceService.buscarDocePorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tipos")
    public ResponseEntity<TipoDoce[]> listarTipos() {
        return ResponseEntity.ok(TipoDoce.values());
    }

    @GetMapping("/sabores")
    public ResponseEntity<SaborMorango[]> listarSabores() {
        return ResponseEntity.ok(SaborMorango.values());
    }

    @GetMapping("/promocoes")
    public ResponseEntity<List<DoceDTO>> listarPromocoes() {
        log.info("Consultando doces em promoção");
        
        List<DoceDTO> promocoes = doceService.listarDocesPromocao();
        
        log.info("Encontrados {} doces em promoção", promocoes.size());
        
        return ResponseEntity.ok(promocoes);
    }

    @GetMapping("/populares")
    public ResponseEntity<List<DoceDTO>> listarPopulares() {
        log.info("Consultando doces mais populares");
        
        List<DoceDTO> populares = doceService.listarDocesPopulares();
        
        log.info("Encontrados {} doces populares", populares.size());
        
        return ResponseEntity.ok(populares);
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<DoceDTO>> listarEstoqueBaixo() {
        log.info("Consultando doces com estoque baixo");
        
        List<DoceDTO> estoqueBaixo = doceService.listarDocesEstoqueBaixo();
        
        log.info("Encontrados {} doces com estoque baixo", estoqueBaixo.size());
        
        return ResponseEntity.ok(estoqueBaixo);
    }
}