package com.bbva.gdpd.gdpdpedidosapi.controller;

import com.bbva.gdpd.gdpdpedidosapi.dto.PedidoEventPayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos SSE", description = "Server-Sent Events para notificaciones en tiempo real")
public class PedidoSseController {
    private static final Logger LOG = LoggerFactory.getLogger(PedidoSseController.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Suscribirse a eventos de pedidos via SSE")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        LOG.debug("Nuevo suscriptor SSE. Total: {}", emitters.size());
        return emitter;
    }

    public void broadcastEvent(PedidoEventPayload event) {
        List<SseEmitter> dead = new CopyOnWriteArrayList<>();
        emitters.forEach(em -> {
            try { em.send(SseEmitter.event().name(event.getEventType()).data(event)); }
            catch (IOException e) { dead.add(em); }
        });
        emitters.removeAll(dead);
    }
}
