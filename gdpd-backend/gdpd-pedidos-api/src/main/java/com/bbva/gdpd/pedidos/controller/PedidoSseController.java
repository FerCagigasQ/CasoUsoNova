package com.bbva.gdpd.pedidos.controller;

import com.bbva.gdpd.pedidos.model.PedidoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoSseController {

    private static final Logger log = LoggerFactory.getLogger(PedidoSseController.class);
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        String key = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(key, emitter);
        emitter.onCompletion(() -> emitters.remove(key));
        emitter.onTimeout(() -> { emitters.remove(key); emitter.complete(); });
        emitter.onError(e -> emitters.remove(key));
        try {
            emitter.send(SseEmitter.event().comment("connected").reconnectTime(5000));
        } catch (IOException e) {
            emitters.remove(key);
        }
        log.info("SSE suscripcion registrada key={}", key);
        return emitter;
    }

    public void broadcast(PedidoEvent evento) {
        emitters.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("pedido-evento")
                        .data(evento, MediaType.APPLICATION_JSON)
                        .id(UUID.randomUUID().toString())
                        .reconnectTime(5000));
            } catch (IOException e) {
                log.warn("SSE envio fallido key={}", key);
                emitters.remove(key);
            }
        });
    }
}
