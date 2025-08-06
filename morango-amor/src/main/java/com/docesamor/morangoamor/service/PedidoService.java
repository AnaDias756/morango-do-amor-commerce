package com.docesamor.morangoamor.service;

import com.docesamor.morangoamor.dto.CriarPedidoRequestDTO;
import com.docesamor.morangoamor.dto.ItemPedidoDTO;
import com.docesamor.morangoamor.dto.PedidoResponseDTO;
import com.docesamor.morangoamor.entity.*;
import com.docesamor.morangoamor.mapper.PedidoMapper;
import com.docesamor.morangoamor.repository.DoceRepository;
import com.docesamor.morangoamor.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DoceRepository doceRepository;
    private final ClienteService clienteService;
    private final DoceService doceService;
    private final PedidoMapper pedidoMapper;
    private final EventoService eventoService;
    private final AbacatePayService abacatePayService;

    @Transactional
    public PedidoResponseDTO criarPedido(CriarPedidoRequestDTO request) {
        log.info("Iniciando criação de pedido para cliente: {}", request.getCliente().getEmail());
        
        try {
            // 1. Criar ou atualizar cliente
            Cliente cliente = clienteService.criarOuAtualizarCliente(request.getCliente());
            
            // 2. Validar disponibilidade dos doces
            validarDisponibilidadeDoces(request.getItens());
            
            // 3. Criar pedido
            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setFormaPagamento(request.getFormaPagamento());
            pedido.setTipoEntrega(request.getTipoEntrega());
            pedido.setEnderecoEntrega(request.getEnderecoEntrega());
            pedido.setObservacoes(request.getObservacoes());
            
            // 4. Criar itens do pedido
            List<ItemPedido> itens = criarItensPedido(request.getItens(), pedido);
            pedido.setItens(itens);
            
            // 5. Aplicar desconto se houver cupom
            if (request.getCupomDesconto() != null && !request.getCupomDesconto().trim().isEmpty()) {
                aplicarDesconto(pedido, request.getCupomDesconto());
            }
            
            // 6. Salvar pedido
            Pedido pedidoSalvo = pedidoRepository.save(pedido);
            
            // 7. Reduzir estoque dos doces
            reduzirEstoqueDoces(request.getItens());
            
            // 8. Criar link de pagamento se necessário
            if (precisaLinkPagamento(request.getFormaPagamento())) {
                String paymentLink = abacatePayService.criarLinkPagamento(pedidoSalvo);
                pedidoSalvo.setPaymentLink(paymentLink);
                pedidoSalvo = pedidoRepository.save(pedidoSalvo);
            }
            
            // 9. Publicar evento de pedido criado
            eventoService.publicarEventoPedidoCriado(pedidoSalvo);
            
            log.info("Pedido {} criado com sucesso para cliente {}", 
                pedidoSalvo.getId(), cliente.getEmail());
            
            return pedidoMapper.toResponseDTO(pedidoSalvo);
            
        } catch (Exception e) {
            log.error("Erro ao criar pedido para cliente {}: {}", 
                request.getCliente().getEmail(), e.getMessage(), e);
            throw new RuntimeException("Erro ao criar pedido: " + e.getMessage(), e);
        }
    }

    private void validarDisponibilidadeDoces(List<ItemPedidoDTO> itens) {
        log.debug("Validando disponibilidade de {} itens", itens.size());
        
        for (ItemPedidoDTO item : itens) {
            if (!doceService.verificarDisponibilidade(item.getDoceId(), item.getQuantidade())) {
                throw new RuntimeException("Doce indisponível ou estoque insuficiente: " + item.getNomeDoce());
            }
        }
    }

    private List<ItemPedido> criarItensPedido(List<ItemPedidoDTO> itensDTO, Pedido pedido) {
        log.debug("Criando {} itens do pedido", itensDTO.size());
        
        return itensDTO.stream().map(itemDTO -> {
            Doce doce = doceRepository.findById(itemDTO.getDoceId())
                .orElseThrow(() -> new RuntimeException("Doce não encontrado: " + itemDTO.getDoceId()));
            
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setDoce(doce);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(doce.getPreco());
            item.setObservacoesItem(itemDTO.getObservacoesItem());
            item.setPersonalizacao(itemDTO.getPersonalizacao());
            
            return item;
        }).collect(Collectors.toList());
    }

    private void aplicarDesconto(Pedido pedido, String cupomDesconto) {
        log.debug("Aplicando cupom de desconto: {}", cupomDesconto);
        
        // Lógica simples de desconto - em produção seria mais complexa
        BigDecimal desconto = BigDecimal.ZERO;
        
        switch (cupomDesconto.toUpperCase()) {
            case "PRIMEIRA_COMPRA":
                desconto = pedido.getValorTotal().multiply(new BigDecimal("0.10")); // 10%
                break;
            case "CLIENTE_VIP":
                if (pedido.getCliente().getClienteVip()) {
                    desconto = pedido.getValorTotal().multiply(new BigDecimal("0.15")); // 15%
                }
                break;
            case "DOCE_AMOR":
                desconto = new BigDecimal("5.00"); // R$ 5,00 fixo
                break;
            default:
                log.warn("Cupom de desconto inválido: {}", cupomDesconto);
                return;
        }
        
        if (desconto.compareTo(BigDecimal.ZERO) > 0) {
            pedido.aplicarDesconto(desconto);
            log.info("Desconto de R$ {} aplicado com cupom: {}", desconto, cupomDesconto);
        }
    }

    private void reduzirEstoqueDoces(List<ItemPedidoDTO> itens) {
        log.debug("Reduzindo estoque de {} tipos de doces", itens.size());
        
        for (ItemPedidoDTO item : itens) {
            doceService.reduzirEstoque(item.getDoceId(), item.getQuantidade());
        }
    }

    private boolean precisaLinkPagamento(FormaPagamento formaPagamento) {
        return formaPagamento == FormaPagamento.PIX || 
               formaPagamento == FormaPagamento.CARTAO_CREDITO || 
               formaPagamento == FormaPagamento.CARTAO_DEBITO;
    }

    public Optional<PedidoResponseDTO> buscarPedidoPorId(Long id) {
        log.debug("Buscando pedido por ID: {}", id);
        
        return pedidoRepository.findById(id)
            .map(pedidoMapper::toResponseDTO);
    }

    public List<PedidoResponseDTO> buscarPedidosPorEmail(String email) {
        log.debug("Buscando pedidos por email: {}", email);
        
        return pedidoRepository.findByClienteEmailOrderByCreatedAtDesc(email)
            .stream()
            .map(pedidoMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    public Page<PedidoResponseDTO> filtrarPedidos(
            StatusPedido status,
            String clienteEmail,
            String clienteNome,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Pageable pageable) {
        
        log.debug("Filtrando pedidos - status: {}, email: {}, nome: {}, período: {} a {}", 
            status, clienteEmail, clienteNome, dataInicio, dataFim);
        
        Page<Pedido> pedidos = pedidoRepository.findWithFilters(
            status, clienteEmail, clienteNome, dataInicio, dataFim, pageable);
        
        return pedidos.map(pedidoMapper::toResponseDTO);
    }

    @Transactional
    public PedidoResponseDTO atualizarStatusPedido(Long pedidoId, StatusPedido novoStatus) {
        log.info("Atualizando status do pedido {} para: {}", pedidoId, novoStatus);
        
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + pedidoId));
        
        StatusPedido statusAnterior = pedido.getStatus();
        
        // Validar transição de status
        validarTransicaoStatus(statusAnterior, novoStatus);
        
        // Atualizar status e timestamps
        pedido.setStatus(novoStatus);
        atualizarTimestampsPorStatus(pedido, novoStatus);
        
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        
        // Incrementar contador de pedidos do cliente se entregue
        if (novoStatus == StatusPedido.ENTREGUE && statusAnterior != StatusPedido.ENTREGUE) {
            clienteService.incrementarPedidosCliente(pedido.getCliente().getEmail());
        }
        
        // Publicar evento de status atualizado
        eventoService.publicarEventoStatusAtualizado(pedidoAtualizado, statusAnterior);
        
        log.info("Status do pedido {} atualizado de {} para {}", 
            pedidoId, statusAnterior, novoStatus);
        
        return pedidoMapper.toResponseDTO(pedidoAtualizado);
    }

    private void validarTransicaoStatus(StatusPedido statusAtual, StatusPedido novoStatus) {
        // Regras de transição de status
        Map<StatusPedido, Set<StatusPedido>> transicoesValidas = Map.of(
            StatusPedido.AGUARDANDO_PAGAMENTO, Set.of(StatusPedido.PAGO, StatusPedido.CANCELADO),
            StatusPedido.PAGO, Set.of(StatusPedido.PREPARANDO, StatusPedido.CANCELADO),
            StatusPedido.PREPARANDO, Set.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO),
            StatusPedido.ENTREGUE, Set.of(), // Status final
            StatusPedido.CANCELADO, Set.of() // Status final
        );
        
        Set<StatusPedido> statusPermitidos = transicoesValidas.get(statusAtual);
        if (statusPermitidos == null || !statusPermitidos.contains(novoStatus)) {
            throw new RuntimeException(
                String.format("Transição de status inválida: %s -> %s", statusAtual, novoStatus));
        }
    }

    private void atualizarTimestampsPorStatus(Pedido pedido, StatusPedido status) {
        LocalDateTime agora = LocalDateTime.now();
        
        switch (status) {
            case PAGO:
                pedido.setPaidAt(agora);
                break;
            case PREPARANDO:
                pedido.setPreparedAt(agora);
                break;
            case ENTREGUE:
                pedido.setDeliveredAt(agora);
                break;
        }
    }

    public Map<String, Object> obterEstatisticasPedidos(LocalDateTime dataInicio, LocalDateTime dataFim) {
        log.debug("Obtendo estatísticas de pedidos - período: {} a {}", dataInicio, dataFim);
        
        Map<String, Object> estatisticas = new HashMap<>();
        
        // Definir período padrão se não informado
        if (dataInicio == null) {
            dataInicio = LocalDateTime.now().minusDays(30);
        }
        if (dataFim == null) {
            dataFim = LocalDateTime.now();
        }
        
        // Estatísticas básicas
        Long totalPedidos = pedidoRepository.countByPeriodo(dataInicio, dataFim);
        BigDecimal receitaTotal = pedidoRepository.sumValorFinalByPeriodoAndStatus(
            dataInicio, dataFim, StatusPedido.ENTREGUE) ?? BigDecimal.ZERO;
        
        estatisticas.put("totalPedidos", totalPedidos);
        estatisticas.put("receitaTotal", receitaTotal);
        estatisticas.put("ticketMedio", totalPedidos > 0 ? 
            receitaTotal.divide(BigDecimal.valueOf(totalPedidos), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
        
        // Estatísticas por status
        Map<String, Long> pedidosPorStatus = new HashMap<>();
        for (StatusPedido status : StatusPedido.values()) {
            Long count = pedidoRepository.countByStatus(status);
            pedidosPorStatus.put(status.name(), count);
        }
        estatisticas.put("pedidosPorStatus", pedidosPorStatus);
        
        // Estatísticas por forma de pagamento
        List<Object[]> pagamentos = pedidoRepository.countByFormaPagamento();
        Map<String, Long> pedidosPorPagamento = pagamentos.stream()
            .collect(Collectors.toMap(
                arr -> ((FormaPagamento) arr[0]).name(),
                arr -> (Long) arr[1]
            ));
        estatisticas.put("pedidosPorFormaPagamento", pedidosPorPagamento);
        
        // Estatísticas por tipo de entrega
        List<Object[]> entregas = pedidoRepository.countByTipoEntrega();
        Map<String, Long> pedidosPorEntrega = entregas.stream()
            .collect(Collectors.toMap(
                arr -> ((TipoEntrega) arr[0]).name(),
                arr -> (Long) arr[1]
            ));
        estatisticas.put("pedidosPorTipoEntrega", pedidosPorEntrega);
        
        return estatisticas;
    }

    public Map<String, Object> obterDadosDashboard() {
        log.debug("Obtendo dados do dashboard");
        
        Map<String, Object> dashboard = new HashMap<>();
        
        // Pedidos de hoje
        LocalDateTime inicioHoje = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fimHoje = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        Long pedidosHoje = pedidoRepository.countByPeriodo(inicioHoje, fimHoje);
        BigDecimal receitaHoje = pedidoRepository.sumValorFinalByPeriodoAndStatus(
            inicioHoje, fimHoje, StatusPedido.ENTREGUE) ?? BigDecimal.ZERO;
        
        dashboard.put("pedidosHoje", pedidosHoje);
        dashboard.put("receitaHoje", receitaHoje);
        
        // Pedidos pendentes
        Long pedidosPendentes = pedidoRepository.countByStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        Long pedidosPreparando = pedidoRepository.countByStatus(StatusPedido.PREPARANDO);
        
        dashboard.put("pedidosPendentes", pedidosPendentes);
        dashboard.put("pedidosPreparando", pedidosPreparando);
        
        // Top clientes
        List<Object[]> topClientes = pedidoRepository.getTopClientesPorValor(
            StatusPedido.ENTREGUE, PageRequest.of(0, 5));
        dashboard.put("topClientes", topClientes);
        
        return dashboard;
    }

    public String exportarPedidos(
            StatusPedido status,
            String clienteEmail,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String formato) {
        
        log.debug("Exportando pedidos - formato: {}", formato);
        
        Page<Pedido> pedidos = pedidoRepository.findWithFilters(
            status, clienteEmail, null, dataInicio, dataFim, 
            PageRequest.of(0, 1000)); // Limite de 1000 registros
        
        if ("csv".equalsIgnoreCase(formato)) {
            return exportarCSV(pedidos.getContent());
        } else {
            return exportarJSON(pedidos.getContent());
        }
    }

    private String exportarCSV(List<Pedido> pedidos) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Cliente,Email,Status,Valor Total,Forma Pagamento,Tipo Entrega,Data Criação\n");
        
        for (Pedido pedido : pedidos) {
            csv.append(String.format("%d,%s,%s,%s,%.2f,%s,%s,%s\n",
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getCliente().getEmail(),
                pedido.getStatus(),
                pedido.getValorFinal(),
                pedido.getFormaPagamento(),
                pedido.getTipoEntrega(),
                pedido.getCreatedAt()
            ));
        }
        
        return csv.toString();
    }

    private String exportarJSON(List<Pedido> pedidos) {
        return pedidos.stream()
            .map(pedidoMapper::toResponseDTO)
            .collect(Collectors.toList())
            .toString(); // Simplificado - em produção usaria Jackson
    }
}