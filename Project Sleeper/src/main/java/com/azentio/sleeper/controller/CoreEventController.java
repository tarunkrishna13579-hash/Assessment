package com.azentio.sleeper.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.azentio.sleeper.dto.CoreEventRequest;
import com.azentio.sleeper.dto.CoreEventResponse;
import com.azentio.sleeper.service.EventIngestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CoreEventController {

    private final EventIngestionService eventIngestionService;

    public CoreEventController(
            EventIngestionService eventIngestionService) {

        this.eventIngestionService = eventIngestionService;
    }

    @PostMapping("/core-events")
    public ResponseEntity<CoreEventResponse> ingestEvent(
            @Valid @RequestBody CoreEventRequest event) {

        boolean duplicate =
            eventIngestionService.ingestEvent(event);

        if (duplicate) {
            return ResponseEntity.ok(
                new CoreEventResponse("Duplicate event", true)
            );
        }

        return ResponseEntity.ok(
            new CoreEventResponse(
                "Event processed successfully", false
            )
        );
    }
}