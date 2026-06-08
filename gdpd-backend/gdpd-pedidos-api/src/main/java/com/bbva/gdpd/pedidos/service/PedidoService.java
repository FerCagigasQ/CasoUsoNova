package com.bbva.gdpd.pedidos.service;

import com.bbva.gdpd.pedidos.model.Pedido;
import com.bbva.gdpd.pedidos.repository.PedidoRepository;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final JmsTemplate jmsTemplate;

    public PedidoService(PedidoRepository pedidoRepository, JmsTemplate jmsTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.jmsTemplate = jmsTemplate;
    }

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido create(Pedido pedido) {
        Pedido saved = pedidoRepository.save(pedido);
        jmsTemplate.convertAndSend("gdpd.pedidos.eventos", "PEDIDO_CREADO:" + saved.getId());
        return saved;
    }

    public Optional<Pedido> update(Long id, Pedido pedido) {
        return pedidoRepository.findById(id).map(existing -> {
            existing.setDescripcion(pedido.getDescripcion());
            existing.setImporte(pedido.getImporte());
            existing.setEstado(pedido.getEstado());
            Pedido updated = pedidoRepository.save(existing);
            jmsTemplate.convertAndSend("gdpd.pedidos.eventos", "PEDIDO_ACTUALIZADO:" + updated.getId());
            return updated;
        });
    }

    public boolean delete(Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            jmsTemplate.convertAndSend("gdpd.pedidos.eventos", "PEDIDO_ELIMINADO:" + id);
            return true;
        }
        return false;
    }
}
