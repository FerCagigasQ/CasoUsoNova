package com.bbva.gdpd.gdpdpedidosapi.controller;

import com.bbva.gdpd.gdpdpedidosapi.domain.EstadoPedido;
import com.bbva.gdpd.gdpdpedidosapi.dto.CreatePedidoRequest;
import com.bbva.gdpd.gdpdpedidosapi.dto.PedidoDTO;
import com.bbva.gdpd.gdpdpedidosapi.dto.UpdatePedidoRequest;
import com.bbva.gdpd.gdpdpedidosapi.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "CRUD de pedidos GDPD")
public class PedidoController {
    private final PedidoService svc;
    public PedidoController(PedidoService svc) { this.svc = svc; }

    @GetMapping
    @Operation(summary = "Listar pedidos")
    public ResponseEntity<List<PedidoDTO>> getPedidos(@RequestParam(required = false) EstadoPedido estado) {
        return ResponseEntity.ok(estado != null ? svc.findByEstado(estado) : svc.findAll());
    }

    @PostMapping
    @Operation(summary = "Crear pedido")
    public ResponseEntity<PedidoDTO> createPedido(@Valid @RequestBody CreatePedidoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido por ID")
    public ResponseEntity<PedidoDTO> getPedidoById(@PathVariable Long id) {
        return svc.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pedido")
    public ResponseEntity<PedidoDTO> updatePedido(@PathVariable Long id, @RequestBody UpdatePedidoRequest req) {
        return svc.update(id, req).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pedido")
    public ResponseEntity<Void> deletePedido(@PathVariable Long id) {
        return svc.deleteById(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
