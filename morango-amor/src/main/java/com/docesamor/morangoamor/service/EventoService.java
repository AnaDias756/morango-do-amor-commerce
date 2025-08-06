package com.docesamor.morangoamor.service;

import com.docesamor.morangoamor.entity.Pedido;
import com.docesamor.morangoamor.entity.StatusPedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.pedido-criado:doces-pedido-criado}")
    private String topicoPedidoCriado;

    @Value("${app.kafka.topics.status-atualizado:doces-status-atualizado}")
    private String topicoStatusAtualizado;

    @Value("${app.kafka.topics.pagamento-confirmado:doces-pagamento-confirmado}")
    private String topicoPagamentoConfirmado;

    @Value("${app.kafka.topics.pedido-entregue:doces-pedido-entregue}")
    private String topicoPedidoEntregue;

    @Value("${app.kafka.topics.estoque-baixo:doces-estoque-baixo}")
    private String topicoEstoqueBaixo;

    public void publicarEventoPedidoCriado(Pedido pedido) {
        try {
            log.info("Publicando evento de pedido criado: {}", pedido.getId());
            
            Map<String, Object> evento = new HashMap<>();
            evento.put("eventType", "PEDIDO_CRIADO");
            evento.put("timestamp", LocalDateTime.now().toString());
            evento.put("pedidoId", pedido.getId());
            evento.put("clienteEmail", pedido.getCliente().getEmail());
            evento.put("clienteNome", pedido.getCliente().getNome());
            evento.put("valorTotal", pedido.getValorTotal());
            evento.put("valorFinal", pedido.getValorFinal());
            evento.put("formaPagamento", pedido.getFormaPagamento().name());
            evento.put("tipoEntrega", pedido.getTipoEntrega().name());
            evento.put("status", pedido.getStatus().name());
            evento.put("quantidadeItens", pedido.getItens().size());
            
            // Adicionar informações dos itens
            evento.put("itens", pedido.getItens().stream().map(item -> {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("doceId", item.getDoce().getId());
                itemInfo.put("doceNome", item.getDoce().getNome());
                itemInfo.put("quantidade", item.getQuantidade());
                itemInfo.put("precoUnitario", item.getPrecoUnitario());
                itemInfo.put("precoTotal", item.getPrecoTotal());
                return itemInfo;
            }).toList());
            
            String eventoJson = objectMapper.writeValueAsString(evento);
            
            kafkaTemplate.send(topicoPedidoCriado, pedido.getId().toString(), eventoJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento de pedido criado publicado com sucesso: {}", pedido.getId());
                    } else {
                        log.error("Erro ao publicar evento de pedido criado: {}", ex.getMessage(), ex);
                    }
                });
                
        } catch (Exception e) {
            log.error("Erro ao serializar evento de pedido criado: {}", e.getMessage(), e);
        }
    }

    public void publicarEventoStatusAtualizado(Pedido pedido, StatusPedido statusAnterior) {
        try {
            log.info("Publicando evento de status atualizado: {} - {} -> {}", 
                pedido.getId(), statusAnterior, pedido.getStatus());
            
            Map<String, Object> evento = new HashMap<>();
            evento.put("eventType", "STATUS_ATUALIZADO");
            evento.put("timestamp", LocalDateTime.now().toString());
            evento.put("pedidoId", pedido.getId());
            evento.put("clienteEmail", pedido.getCliente().getEmail());
            evento.put("statusAnterior", statusAnterior.name());
            evento.put("statusAtual", pedido.getStatus().name());
            evento.put("valorFinal", pedido.getValorFinal());
            
            // Adicionar timestamps específicos
            if (pedido.getPaidAt() != null) {
                evento.put("paidAt", pedido.getPaidAt().toString());
            }
            if (pedido.getPreparedAt() != null) {
                evento.put("preparedAt", pedido.getPreparedAt().toString());
            }
            if (pedido.getDeliveredAt() != null) {
                evento.put("deliveredAt", pedido.getDeliveredAt().toString());
            }
            
            String eventoJson = objectMapper.writeValueAsString(evento);
            
            kafkaTemplate.send(topicoStatusAtualizado, pedido.getId().toString(), eventoJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento de status atualizado publicado com sucesso: {}", pedido.getId());
                    } else {
                        log.error("Erro ao publicar evento de status atualizado: {}", ex.getMessage(), ex);
                    }
                });
            
            // Publicar eventos específicos para certos status
            if (pedido.getStatus() == StatusPedido.PAGO) {
                publicarEventoPagamentoConfirmado(pedido);
            } else if (pedido.getStatus() == StatusPedido.ENTREGUE) {
                publicarEventoPedidoEntregue(pedido);
            }
            
        } catch (Exception e) {
            log.error("Erro ao serializar evento de status atualizado: {}", e.getMessage(), e);
        }
    }

    public void publicarEventoPagamentoConfirmado(Pedido pedido) {
        try {
            log.info("Publicando evento de pagamento confirmado: {}", pedido.getId());
            
            Map<String, Object> evento = new HashMap<>();
            evento.put("eventType", "PAGAMENTO_CONFIRMADO");
            evento.put("timestamp", LocalDateTime.now().toString());
            evento.put("pedidoId", pedido.getId());
            evento.put("clienteEmail", pedido.getCliente().getEmail());
            evento.put("clienteNome", pedido.getCliente().getNome());
            evento.put("valorPago", pedido.getValorFinal());
            evento.put("formaPagamento", pedido.getFormaPagamento().name());
            evento.put("abacateTransactionId", pedido.getAbacateTransactionId());
            evento.put("paidAt", pedido.getPaidAt().toString());
            
            String eventoJson = objectMapper.writeValueAsString(evento);
            
            kafkaTemplate.send(topicoPagamentoConfirmado, pedido.getId().toString(), eventoJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento de pagamento confirmado publicado com sucesso: {}", pedido.getId());
                    } else {
                        log.error("Erro ao publicar evento de pagamento confirmado: {}", ex.getMessage(), ex);
                    }
                });
                
        } catch (Exception e) {
            log.error("Erro ao serializar evento de pagamento confirmado: {}", e.getMessage(), e);
        }
    }

    public void publicarEventoPedidoEntregue(Pedido pedido) {
        try {
            log.info("Publicando evento de pedido entregue: {}", pedido.getId());
            
            Map<String, Object> evento = new HashMap<>();
            evento.put("eventType", "PEDIDO_ENTREGUE");
            evento.put("timestamp", LocalDateTime.now().toString());
            evento.put("pedidoId", pedido.getId());
            evento.put("clienteEmail", pedido.getCliente().getEmail());
            evento.put("clienteNome", pedido.getCliente().getNome());
            evento.put("valorFinal", pedido.getValorFinal());
            evento.put("tipoEntrega", pedido.getTipoEntrega().name());
            evento.put("enderecoEntrega", pedido.getEnderecoEntrega());
            evento.put("deliveredAt", pedido.getDeliveredAt().toString());
            evento.put("tempoTotalPreparo", calcularTempoPreparo(pedido));
            
            // Verificar se cliente se tornou VIP
            if (pedido.getCliente().getClienteVip()) {
                evento.put("clienteVip", true);
                evento.put("totalPedidosCliente", pedido.getCliente().getTotalPedidos());
            }
            
            String eventoJson = objectMapper.writeValueAsString(evento);
            
            kafkaTemplate.send(topicoPedidoEntregue, pedido.getId().toString(), eventoJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento de pedido entregue publicado com sucesso: {}", pedido.getId());
                    } else {
                        log.error("Erro ao publicar evento de pedido entregue: {}", ex.getMessage(), ex);
                    }
                });
                
        } catch (Exception e) {
            log.error("Erro ao serializar evento de pedido entregue: {}", e.getMessage(), e);
        }
    }

    public void publicarEventoEstoqueBaixo(Long doceId, String doceNome, Integer estoqueAtual, Integer estoqueMinimo) {
        try {
            log.warn("Publicando evento de estoque baixo - Doce: {} (ID: {}), Estoque: {}, Mínimo: {}", 
                doceNome, doceId, estoqueAtual, estoqueMinimo);
            
            Map<String, Object> evento = new HashMap<>();
            evento.put("eventType", "ESTOQUE_BAIXO");
            evento.put("timestamp", LocalDateTime.now().toString());
            evento.put("doceId", doceId);
            evento.put("doceNome", doceNome);
            evento.put("estoqueAtual", estoqueAtual);
            evento.put("estoqueMinimo", estoqueMinimo);
            evento.put("nivelCriticidade", calcularNivelCriticidade(estoqueAtual, estoqueMinimo));
            
            String eventoJson = objectMapper.writeValueAsString(evento);
            
            kafkaTemplate.send(topicoEstoqueBaixo, doceId.toString(), eventoJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento de estoque baixo publicado com sucesso: {}", doceNome);
                    } else {
                        log.error("Erro ao publicar evento de estoque baixo: {}", ex.getMessage(), ex);
                    }
                });
                
        } catch (Exception e) {
            log.error("Erro ao serializar evento de estoque baixo: {}", e.getMessage(), e);
        }
    }

    public void publicarEventoWebhook(String eventType, Map<String, Object> dados) {
        try {
            log.info("Publicando evento de webhook: {}", eventType);
            
            Map<String, Object> evento = new HashMap<>();
            evento.put("eventType", eventType);
            evento.put("timestamp", LocalDateTime.now().toString());
            evento.putAll(dados);
            
            String eventoJson = objectMapper.writeValueAsString(evento);
            String topico = "doces-webhook-" + eventType.toLowerCase().replace("_", "-");
            
            kafkaTemplate.send(topico, eventoJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento de webhook publicado com sucesso: {}", eventType);
                    } else {
                        log.error("Erro ao publicar evento de webhook: {}", ex.getMessage(), ex);
                    }
                });
                
        } catch (Exception e) {
            log.error("Erro ao serializar evento de webhook: {}", e.getMessage(), e);
        }
    }

    private long calcularTempoPreparo(Pedido pedido) {
        if (pedido.getCreatedAt() != null && pedido.getDeliveredAt() != null) {
            return java.time.Duration.between(pedido.getCreatedAt(), pedido.getDeliveredAt()).toMinutes();
        }
        return 0;
    }

    private String calcularNivelCriticidade(Integer estoqueAtual, Integer estoqueMinimo) {
        if (estoqueAtual == 0) {
            return "CRITICO";
        } else if (estoqueAtual <= estoqueMinimo / 2) {
            return "ALTO";
        } else if (estoqueAtual <= estoqueMinimo) {
            return "MEDIO";
        } else {
            return "BAIXO";
        }
    }
}