package com.example.guarantees.service;

import com.example.guarantees.dto.GuaranteeChangeEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class GuaranteeEventService {

    private static final Logger log = LoggerFactory.getLogger(GuaranteeEventService.class);
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError(error -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void publish(Long guaranteeId, String action, String reference, String status) {
        GuaranteeChangeEventDTO event = new GuaranteeChangeEventDTO(
                guaranteeId,
                action,
                reference,
                status,
                Instant.now()
        );

        log.info("Guarantee change event action={} id={} reference={}", action, guaranteeId, reference);
        emitters.forEach(emitter -> send(emitter, event));
    }

    private void send(SseEmitter emitter, GuaranteeChangeEventDTO event) {
        try {
            emitter.send(SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name("guarantee-change")
                    .data(event, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            emitters.remove(emitter);
        }
    }
}
