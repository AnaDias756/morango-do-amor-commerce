package com.docesamor.morangoamor.service;

import com.docesamor.morangoamor.dto.ClienteDTO;
import com.docesamor.morangoamor.entity.Cliente;
import com.docesamor.morangoamor.mapper.ClienteMapper;
import com.docesamor.morangoamor.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional
    public Cliente criarOuAtualizarCliente(ClienteDTO clienteDTO) {
        log.debug("Criando ou atualizando cliente: {}", clienteDTO.getEmail());
        
        Optional<Cliente> clienteExistente = clienteRepository.findByEmailIgnoreCase(clienteDTO.getEmail());
        
        Cliente cliente;
        if (clienteExistente.isPresent()) {
            log.debug("Cliente existente encontrado, atualizando dados: {}", clienteDTO.getEmail());
            cliente = clienteExistente.get();
            
            // Atualiza apenas campos não nulos
            if (clienteDTO.getNome() != null) {
                cliente.setNome(clienteDTO.getNome());
            }
            if (clienteDTO.getTelefone() != null) {
                cliente.setTelefone(clienteDTO.getTelefone());
            }
            if (clienteDTO.getDataNascimento() != null) {
                cliente.setDataNascimento(clienteDTO.getDataNascimento());
            }
            if (clienteDTO.getEnderecoEntrega() != null) {
                cliente.setEnderecoEntrega(clienteDTO.getEnderecoEntrega());
            }
            if (clienteDTO.getCep() != null) {
                cliente.setCep(clienteDTO.getCep());
            }
            if (clienteDTO.getCidade() != null) {
                cliente.setCidade(clienteDTO.getCidade());
            }
            if (clienteDTO.getEstado() != null) {
                cliente.setEstado(clienteDTO.getEstado());
            }
            if (clienteDTO.getPreferenciasDoces() != null) {
                cliente.setPreferenciasDoces(clienteDTO.getPreferenciasDoces());
            }
            if (clienteDTO.getAlergias() != null) {
                cliente.setAlergias(clienteDTO.getAlergias());
            }
        } else {
            log.debug("Criando novo cliente: {}", clienteDTO.getEmail());
            cliente = clienteMapper.toEntity(clienteDTO);
        }
        
        Cliente clienteSalvo = clienteRepository.save(cliente);
        log.info("Cliente {} salvo com sucesso. ID: {}", clienteSalvo.getEmail(), clienteSalvo.getId());
        
        return clienteSalvo;
    }

    public Optional<Cliente> buscarClientePorEmail(String email) {
        log.debug("Buscando cliente por email: {}", email);
        
        return clienteRepository.findByEmailIgnoreCase(email);
    }

    public Optional<Cliente> buscarClientePorId(Long id) {
        log.debug("Buscando cliente por ID: {}", id);
        
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> buscarClientePorTelefone(String telefone) {
        log.debug("Buscando cliente por telefone: {}", telefone);
        
        return clienteRepository.findByTelefone(telefone);
    }

    public List<Cliente> buscarClientesPorNome(String nome) {
        log.debug("Buscando clientes por nome: {}", nome);
        
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Cliente> listarClientesVip() {
        log.debug("Listando clientes VIP");
        
        return clienteRepository.findByClienteVipTrue();
    }

    public List<Cliente> buscarClientesPorCidade(String cidade) {
        log.debug("Buscando clientes por cidade: {}", cidade);
        
        return clienteRepository.findByCidadeIgnoreCase(cidade);
    }

    public List<Cliente> buscarClientesPorEstado(String estado) {
        log.debug("Buscando clientes por estado: {}", estado);
        
        return clienteRepository.findByEstadoIgnoreCase(estado);
    }

    public List<Cliente> buscarAniversariantesDoMes(int mes) {
        log.debug("Buscando aniversariantes do mês: {}", mes);
        
        return clienteRepository.findAniversariantesDoMes(mes);
    }

    public List<Cliente> buscarAniversariantesHoje() {
        log.debug("Buscando aniversariantes de hoje");
        
        return clienteRepository.findAniversariantesHoje();
    }

    public List<Cliente> buscarClientesPorIdade(int idadeMinima, int idadeMaxima) {
        log.debug("Buscando clientes por faixa de idade: {} a {}", idadeMinima, idadeMaxima);
        
        return clienteRepository.findByIdadeBetween(idadeMinima, idadeMaxima);
    }

    public List<Cliente> buscarClientesComAlergia(String alergia) {
        log.debug("Buscando clientes com alergia: {}", alergia);
        
        return clienteRepository.findByAlergiasContainingIgnoreCase(alergia);
    }

    public List<Cliente> buscarClientesComPreferencia(String preferencia) {
        log.debug("Buscando clientes com preferência: {}", preferencia);
        
        return clienteRepository.findByPreferenciasDocesContainingIgnoreCase(preferencia);
    }

    public Page<Cliente> filtrarClientes(
            String nome,
            String email,
            String cidade,
            String estado,
            Boolean clienteVip,
            Pageable pageable) {
        
        log.debug("Filtrando clientes - nome: {}, email: {}, cidade: {}, estado: {}, VIP: {}", 
            nome, email, cidade, estado, clienteVip);
        
        return clienteRepository.findWithFilters(nome, email, cidade, estado, clienteVip, pageable);
    }

    @Transactional
    public void incrementarPedidosCliente(String email) {
        log.debug("Incrementando contador de pedidos para cliente: {}", email);
        
        Cliente cliente = clienteRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + email));
        
        cliente.incrementarPedidos();
        clienteRepository.save(cliente);
        
        log.info("Contador de pedidos incrementado para cliente {}. Total: {}, VIP: {}", 
            email, cliente.getTotalPedidos(), cliente.getClienteVip());
    }

    public boolean existeClientePorEmail(String email) {
        log.debug("Verificando se existe cliente com email: {}", email);
        
        return clienteRepository.existsByEmailIgnoreCase(email);
    }

    public List<Cliente> listarClientesMaisAtivos(int limite) {
        log.debug("Listando {} clientes mais ativos", limite);
        
        return clienteRepository.findClientesMaisAtivos(
            org.springframework.data.domain.PageRequest.of(0, limite));
    }

    public List<Cliente> listarClientesNovos(int diasAtras) {
        log.debug("Listando clientes novos dos últimos {} dias", diasAtras);
        
        LocalDate dataLimite = LocalDate.now().minusDays(diasAtras);
        return clienteRepository.findClientesNovos(dataLimite);
    }

    public List<Cliente> listarClientesInativos(int diasSemAtividade) {
        log.debug("Listando clientes inativos há mais de {} dias", diasSemAtividade);
        
        LocalDate dataLimite = LocalDate.now().minusDays(diasSemAtividade);
        return clienteRepository.findClientesInativos(dataLimite);
    }

    public List<Object[]> obterEstatisticasPorCidade() {
        log.debug("Obtendo estatísticas de clientes por cidade");
        
        return clienteRepository.countClientesByCidade();
    }

    public List<Object[]> obterEstatisticasPorEstado() {
        log.debug("Obtendo estatísticas de clientes por estado");
        
        return clienteRepository.countClientesByEstado();
    }

    public Long contarClientesVip() {
        log.debug("Contando clientes VIP");
        
        return clienteRepository.countClientesVip();
    }

    public Double obterMediaPedidosPorCliente() {
        log.debug("Obtendo média de pedidos por cliente");
        
        return clienteRepository.getMediaPedidosPorCliente();
    }

    public Long contarClientesNovos(int diasAtras) {
        log.debug("Contando clientes novos dos últimos {} dias", diasAtras);
        
        LocalDate dataInicio = LocalDate.now().minusDays(diasAtras);
        return clienteRepository.countClientesNovos(dataInicio);
    }

    @Transactional
    public Cliente atualizarCliente(Long id, ClienteDTO clienteDTO) {
        log.debug("Atualizando cliente ID: {}", id);
        
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + id));
        
        // Atualiza campos não nulos
        if (clienteDTO.getNome() != null) {
            cliente.setNome(clienteDTO.getNome());
        }
        if (clienteDTO.getTelefone() != null) {
            cliente.setTelefone(clienteDTO.getTelefone());
        }
        if (clienteDTO.getDataNascimento() != null) {
            cliente.setDataNascimento(clienteDTO.getDataNascimento());
        }
        if (clienteDTO.getEnderecoEntrega() != null) {
            cliente.setEnderecoEntrega(clienteDTO.getEnderecoEntrega());
        }
        if (clienteDTO.getCep() != null) {
            cliente.setCep(clienteDTO.getCep());
        }
        if (clienteDTO.getCidade() != null) {
            cliente.setCidade(clienteDTO.getCidade());
        }
        if (clienteDTO.getEstado() != null) {
            cliente.setEstado(clienteDTO.getEstado());
        }
        if (clienteDTO.getPreferenciasDoces() != null) {
            cliente.setPreferenciasDoces(clienteDTO.getPreferenciasDoces());
        }
        if (clienteDTO.getAlergias() != null) {
            cliente.setAlergias(clienteDTO.getAlergias());
        }
        
        Cliente clienteAtualizado = clienteRepository.save(cliente);
        log.info("Cliente {} atualizado com sucesso", clienteAtualizado.getEmail());
        
        return clienteAtualizado;
    }
}