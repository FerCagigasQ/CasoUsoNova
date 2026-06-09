package com.bbva.gdpd.pedidos.service;

import com.bbva.gdpd.pedidos.controller.PedidoSseController;
import com.bbva.gdpd.pedidos.messaging.PedidoProducer;
import com.bbva.gdpd.pedidos.model.Pedido;
import com.bbva.gdpd.pedidos.model.PedidoEvent;
import com.bbva.gdpd.pedidos.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final PedidoProducer pedidoProducer;
    private final PedidoSseController sseController;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoProducer pedidoProducer,
                         PedidoSseController sseController) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoProducer = pedidoProducer;
        this.sseController = sseController;
    }

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido create(Pedido pedido) {
        Pedido saved = pedidoRepository.save(pedido);
        PedidoEvent evento = new PedidoEvent(
            String.valueOf(saved.getId()), "CREACION",
            saved.getEstado().name(), saved.getCodigoCliente(), saved.getImporte());
        pedidoProducer.publicarEvento(evento);
        sseController.broadcast(evento);
        log.info("Pedido creado id={}", saved.getId());
        return saved;
    }

    public Optional<Pedido> update(Long id, Pedido pedido) {
        return pedidoRepository.findById(id).map(existing -> {
            existing.setDescripcion(pedido.getDescripcion());
            existing.setImporte(pedido.getImporte());
            existing.setEstado(pedido.getEstado());
            Pedido updated = pedidoRepository.save(existing);
            PedidoEvent evento = new PedidoEvent(
                String.valueOf(updated.getId()), "ACTUALIZACION_ESTADO",
                updated.getEstado().name(), updated.getCodigoCliente(), updated.getImporte());
            pedidoProducer.publicarEvento(evento);
            sseController.broadcast(evento);
            log.info("Pedido actualizado id={}", updated.getId());
            return updated;
        });
    }

    public boolean delete(Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            PedidoEvent evento = new PedidoEvent(String.valueOf(id), "ELIMINACION", "CANCELADO", null, null);
            pedidoProducer.publicarEvento(evento);
            sseController.broadcast(evento);
            log.info("Pedido eliminado id={}", id);
            return true;
        }
        return false;
    }
}
