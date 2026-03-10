package com.Teleco.FraudCall.controller;

import com.Teleco.FraudCall.config.TelcoGeneratorProperties;
import com.Teleco.FraudCall.model.CDRRecord;
import com.Teleco.FraudCall.model.GeneratorConfig;
import com.Teleco.FraudCall.service.CDRGeneratorService;
import com.Teleco.FraudCall.service.EventHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for Telco CDR Generator.
 * Provides endpoints to generate call records with fraud simulation.
 */
@RestController
@RequestMapping("/api/telco")
public class TelcoGeneratorController {

    private static final Logger log = LoggerFactory.getLogger(TelcoGeneratorController.class);

    private final CDRGeneratorService generatorService;
    private final EventHubService eventHubService;
    private final TelcoGeneratorProperties properties;

    public TelcoGeneratorController(CDRGeneratorService generatorService,
                                    EventHubService eventHubService,
                                    TelcoGeneratorProperties properties) {
        this.generatorService = generatorService;
        this.eventHubService = eventHubService;
        this.properties = properties;
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("generatorRunning", generatorService.isRunning());
        status.put("eventHubConfigured", eventHubService.isEventHubConfigured());
        status.put("eventHubReady", eventHubService.isReady());
        return ResponseEntity.ok(status);
    }

    /**
     * Generate a single CDR record.
     */
    @GetMapping("/record")
    public ResponseEntity<CDRRecord> generateSingleRecord() {
        CDRRecord record = generatorService.generateSingleRecord();
        return ResponseEntity.ok(record);
    }

    /**
     * Generate multiple CDR records.
     */
    @GetMapping("/records")
    public ResponseEntity<List<CDRRecord>> generateRecords(
            @RequestParam(defaultValue = "10") int count) {
        List<CDRRecord> records = generatorService.generateRecords(Math.min(count, 1000));
        return ResponseEntity.ok(records);
    }

    /**
     * Start continuous generation with custom configuration.
     *
     * @param cdrPerHour        Number of CDR records per hour
     * @param fraudProbability  Probability of SIM card fraud (0.0 to 1.0)
     * @param durationHours     Duration of generation in hours
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startGeneration(
            @RequestParam(defaultValue = "100") int cdrPerHour,
            @RequestParam(defaultValue = "0.2") float fraudProbability,
            @RequestParam(defaultValue = "1") int durationHours) {

        if (generatorService.isRunning()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Generation is already running"));
        }

        GeneratorConfig config = new GeneratorConfig(cdrPerHour, fraudProbability, durationHours);

        // Start generation asynchronously
        CompletableFuture.runAsync(() -> {
            generatorService.startGeneration(config, null);
        });

        log.info("Started CDR generation with config: {}", config);

        return ResponseEntity.ok(Map.of(
                "message", "Generation started",
                "config", config.toString()
        ));
    }

    /**
     * Start generation with request body configuration.
     */
    @PostMapping("/start-advanced")
    public ResponseEntity<Map<String, String>> startAdvancedGeneration(
            @RequestBody GeneratorConfig config) {

        if (generatorService.isRunning()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Generation is already running"));
        }

        // Start generation asynchronously
        CompletableFuture.runAsync(() -> {
            generatorService.startGeneration(config, null);
        });

        log.info("Started CDR generation with config: {}", config);

        return ResponseEntity.ok(Map.of(
                "message", "Generation started",
                "config", config.toString()
        ));
    }

    /**
     * Stop the current generation.
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopGeneration() {
        if (!generatorService.isRunning()) {
            return ResponseEntity.ok(Map.of("message", "Generation was not running"));
        }

        generatorService.stopGeneration();
        return ResponseEntity.ok(Map.of("message", "Generation stop requested"));
    }

    /**
     * Get current generator status.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", generatorService.isRunning());
        status.put("outputMode", properties.getOutputMode().name());
        status.put("cdrPerHour", properties.getCdrPerHour());
        status.put("fraudProbability", properties.getFraudProbability());
        status.put("durationHours", properties.getDurationHours());
        status.put("eventHubConfigured", eventHubService.isEventHubConfigured());
        return ResponseEntity.ok(status);
    }

    /**
     * Generate and send records to Event Hub (batch operation).
     */
    @PostMapping("/send-batch")
    public ResponseEntity<Map<String, Object>> sendBatch(
            @RequestParam(defaultValue = "10") int count) {

        if (!eventHubService.isReady()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Event Hub is not configured or not ready"));
        }

        List<CDRRecord> records = generatorService.generateRecords(Math.min(count, 100));
        int sent = eventHubService.sendRecords(records);

        return ResponseEntity.ok(Map.of(
                "generated", records.size(),
                "sent", sent,
                "message", "Batch completed"
        ));
    }

    /**
     * Stream records with Server-Sent Events (SSE).
     */
    @GetMapping("/stream")
    public ResponseEntity<List<CDRRecord>> streamRecords(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "100") int delayMs) {

        List<CDRRecord> records = new ArrayList<>();

        for (int i = 0; i < Math.min(count, 100); i++) {
            records.add(generatorService.generateSingleRecord());
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return ResponseEntity.ok(records);
    }

    /**
     * Update configuration dynamically.
     */
    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @RequestParam(required = false) Integer phonePoolSize,
            @RequestParam(required = false) TelcoGeneratorProperties.OutputMode outputMode) {

        Map<String, Object> updated = new HashMap<>();

        if (phonePoolSize != null && phonePoolSize > 0) {
            generatorService.reinitializeCallStore(phonePoolSize);
            updated.put("phonePoolSize", phonePoolSize);
        }

        if (outputMode != null) {
            properties.setOutputMode(outputMode);
            updated.put("outputMode", outputMode.name());
        }

        if (updated.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No configuration parameters provided"));
        }

        updated.put("message", "Configuration updated");
        return ResponseEntity.ok(updated);
    }

    /**
     * Usage information.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> usage() {
        Map<String, Object> info = new HashMap<>();
        info.put("description", "Telco CDR Generator API - Generates Call Detail Records with fraud simulation");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/telco/health", "Health check");
        endpoints.put("GET /api/telco/record", "Generate single CDR record");
        endpoints.put("GET /api/telco/records?count=N", "Generate N CDR records");
        endpoints.put("POST /api/telco/start?cdrPerHour=N&fraudProbability=F&durationHours=H", "Start continuous generation");
        endpoints.put("POST /api/telco/stop", "Stop generation");
        endpoints.put("GET /api/telco/status", "Get generator status");
        endpoints.put("POST /api/telco/send-batch?count=N", "Generate and send N records to Event Hub");
        endpoints.put("GET /api/telco/stream?count=N&delayMs=D", "Stream N records with D ms delay");

        info.put("endpoints", endpoints);
        info.put("usage", "telcodatagen [#NumCDRsPerHour] [SIM Card Fraud Probability] [#DurationHours]");

        return ResponseEntity.ok(info);
    }
}
