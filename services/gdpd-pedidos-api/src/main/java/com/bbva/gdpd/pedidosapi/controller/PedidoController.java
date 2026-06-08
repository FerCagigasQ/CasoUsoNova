package com.bbva.gdpd.pedidosapi.controller;

import com.bbva.gdpd.pedidosapi.dto.CreatePedidoRequest;
import com.bbva.gdpd.pedidosapi.dto.PedidoDTO;
import com.bbva.gdpd.pedidosapi.messaging.PedidoEvent;
import com.bbva.gdpd.pedidosapi.messaging.PedidoEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "CRUD de pedidos con publicacion de eventos asincronos")
public class PedidoController {

    private final PedidoEventPublisher eventPublisher;

    public PedidoController(PedidoEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    @Operation(summary = "Crear pedido y publicar evento pedidos.created")
    public ResponseEntity<PedidoDTO> create(@Valid @RequestBody CreatePedidoRequest request) {
        // Domain logic delegated to service layer (omitted for clarity)
        Long pedidoId = System.currentTimeMillis(); // placeholder id
        PedidoDTO dto = new PedidoDTO(pedidoId, request.getClienteId(), "PENDIENTE");

        eventPublisher.publishCreated(
            new PedidoEvent("pedidos.created", pedidoId, "PENDIENTE",
                            request.getClienteId(), UUID.randomUUID().toString()));

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del pedido y publicar evento pedidos.updated")
    public ResponseEntity<PedidoDTO> updateEstado(@PathVariable Long id,
                                                   @RequestParam String estado) {
        PedidoDTO dto = new PedidoDTO(id, "cliente-unknown", estado);

        eventPublisher.publishUpdated(
            new PedidoEvent("pedidos.updated", id, estado, "cliente-unknown",
                            UUID.randomUUID().toString()));

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pedido y publicar evento pedidos.deleted")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventPublisher.publishDeleted(
            new PedidoEvent("pedidos.deleted", id, "ELIMINADO", "cliente-unknown",
                            UUID.randomUUID().toString()));
        return ResponseEntity.noContent().build();
    }
}
