package com.example.guarantees.controller;

import com.example.guarantees.service.GuaranteeEventService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/guarantees")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
public class GuaranteeEventController {

    private final GuaranteeEventService guaranteeEventService;

    public GuaranteeEventController(GuaranteeEventService guaranteeEventService) {
        this.guaranteeEventService = guaranteeEventService;
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return guaranteeEventService.subscribe();
    }
}
