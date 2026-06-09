package com.bbva.gdpd.gdpdpedidosapi.service;

import com.bbva.gdpd.gdpdpedidosapi.domain.EstadoPedido;
import com.bbva.gdpd.gdpdpedidosapi.domain.Pedido;
import com.bbva.gdpd.gdpdpedidosapi.dto.CreatePedidoRequest;
import com.bbva.gdpd.gdpdpedidosapi.dto.PedidoDTO;
import com.bbva.gdpd.gdpdpedidosapi.dto.PedidoEventPayload;
import com.bbva.gdpd.gdpdpedidosapi.dto.UpdatePedidoRequest;
import com.bbva.gdpd.gdpdpedidosapi.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoService {
    private static final Logger LOG = LoggerFactory.getLogger(PedidoService.class);
    private static final String BINDING = "pedidosEventsOut-out-0";

    private final PedidoRepository repo;
    private final StreamBridge streamBridge;

    public PedidoService(PedidoRepository repo, StreamBridge streamBridge) {
        this.repo = repo;
        this.streamBridge = streamBridge;
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> findAll() {
        return repo.findAll().stream().map(PedidoDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> findByEstado(EstadoPedido estado) {
        return repo.findByEstado(estado).stream().map(PedidoDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PedidoDTO> findById(Long id) {
        return repo.findById(id).map(PedidoDTO::from);
    }

    public PedidoDTO create(CreatePedidoRequest req) {
        Pedido p = new Pedido();
        p.setReferencia(req.getReferencia());
        p.setImporte(req.getImporte());
        Pedido saved = repo.save(p);
        LOG.info("Pedido creado id={}", saved.getId());
        streamBridge.send(BINDING, new PedidoEventPayload("PEDIDO_CREADO", saved.getId(),
                saved.getReferencia(), saved.getEstado(), saved.getImporte()));
        return PedidoDTO.from(saved);
    }

    public Optional<PedidoDTO> update(Long id, UpdatePedidoRequest req) {
        return repo.findById(id).map(p -> {
            if (req.getEstado() != null) p.setEstado(req.getEstado());
            if (req.getImporte() != null) p.setImporte(req.getImporte());
            Pedido saved = repo.save(p);
            LOG.info("Pedido actualizado id={} estado={}", saved.getId(), saved.getEstado());
            streamBridge.send(BINDING, new PedidoEventPayload("PEDIDO_ACTUALIZADO", saved.getId(),
                    saved.getReferencia(), saved.getEstado(), saved.getImporte()));
            return PedidoDTO.from(saved);
        });
    }

    public boolean deleteById(Long id) {
        if (repo.existsById(id)) { repo.deleteById(id); return true; }
        return false;
    }
}
