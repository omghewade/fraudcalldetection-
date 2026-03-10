package com.Teleco.FraudCall.service;

import com.Teleco.FraudCall.config.TelcoGeneratorProperties;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.models.SendOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.Teleco.FraudCall.model.CDRRecord;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Service for sending CDR records to Azure Event Hub.
 */
@Service
public class EventHubService {

    private static final Logger log = LoggerFactory.getLogger(EventHubService.class);

    private final TelcoGeneratorProperties properties;
    private final ObjectMapper objectMapper;
    private EventHubProducerClient producerClient;
    private boolean initialized = false;

    public EventHubService(TelcoGeneratorProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initialize() {
        if (isEventHubConfigured()) {
            try {
                producerClient = new EventHubClientBuilder()
                        .connectionString(properties.getEventHubConnectionString(), properties.getEventHubName())
                        .buildProducerClient();
                initialized = true;
                log.info("Event Hub producer client initialized for hub: {}", properties.getEventHubName());
            } catch (Exception e) {
                log.warn("Failed to initialize Event Hub client: {}", e.getMessage());
                initialized = false;
            }
        } else {
            log.info("Event Hub not configured. Skipping Event Hub initialization.");
        }
    }

    @PreDestroy
    public void cleanup() {
        if (producerClient != null) {
            try {
                producerClient.close();
                log.info("Event Hub producer client closed.");
            } catch (Exception e) {
                log.warn("Error closing Event Hub client: {}", e.getMessage());
            }
        }
    }

    /**
     * Check if Event Hub is properly configured.
     */
    public boolean isEventHubConfigured() {
        return properties.getEventHubConnectionString() != null
                && !properties.getEventHubConnectionString().isBlank()
                && properties.getEventHubName() != null
                && !properties.getEventHubName().isBlank();
    }

    /**
     * Check if the service is initialized and ready to send.
     */
    public boolean isReady() {
        return initialized && producerClient != null;
    }

    /**
     * Send a CDR record to Event Hub.
     *
     * @param record The CDR record to send
     * @return true if sent successfully, false otherwise
     */
    public boolean sendRecord(CDRRecord record) {
        if (!isReady()) {
            log.debug("Event Hub not ready. Skipping send.");
            return false;
        }

        try {
            String serialized = objectMapper.writeValueAsString(record);
            EventData eventData = new EventData(serialized.getBytes(StandardCharsets.UTF_8));

            SendOptions sendOptions = new SendOptions()
                    .setPartitionKey(record.getCallingIMSI());

            producerClient.send(Collections.singletonList(eventData), sendOptions);
            return true;
        } catch (Exception e) {
            log.error("Error sending record to Event Hub: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Send a batch of CDR records to Event Hub.
     *
     * @param records The records to send
     * @return Number of records sent successfully
     */
    public int sendRecords(Iterable<CDRRecord> records) {
        int sent = 0;
        for (CDRRecord record : records) {
            if (sendRecord(record)) {
                sent++;
            }
        }
        return sent;
    }
}
