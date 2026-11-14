package com.azentio.sleeper.controller;

import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azentio.sleeper.dto.AlertResponse;
import com.azentio.sleeper.service.FraudDetectionService;

@RestController
@RequestMapping("/api")
public class AlertController {

    private final FraudDetectionService fraudDetectionService;

    public AlertController(
            FraudDetectionService fraudDetectionService) {

        this.fraudDetectionService = fraudDetectionService;
    }

    @GetMapping("/alerts")
    public ResponseEntity<Collection<AlertResponse>> getAlerts() {

        Collection<AlertResponse> alerts =
                fraudDetectionService.getAllAlerts()
                        .stream()
                        .map(AlertResponse::from)
                        .toList();

        return ResponseEntity.ok(alerts);
    }
}