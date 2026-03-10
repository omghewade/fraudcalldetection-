package com.Teleco.FraudCall.service;

import com.Teleco.FraudCall.config.TelcoGeneratorProperties;
import com.Teleco.FraudCall.model.CDRRecord;
import com.Teleco.FraudCall.model.CallStore;
import com.Teleco.FraudCall.model.GeneratorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Service for generating Call Detail Records (CDR) with fraud simulation.
 */
@Service
public class CDRGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(CDRGeneratorService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmmss");

    private final TelcoGeneratorProperties properties;
    private final EventHubService eventHubService;
    private final Random random = new Random();

    private CallStore callStore;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CDRGeneratorService(TelcoGeneratorProperties properties, EventHubService eventHubService) {
        this.properties = properties;
        this.eventHubService = eventHubService;
        this.callStore = new CallStore(properties.getPhoneNumberPoolSize());
    }

    /**
     * Generate a single CDR record.
     */
    public CDRRecord generateSingleRecord() {
        CDRRecord record = CDRRecord.createNew();

        LocalDateTime currentTime = LocalDateTime.now();

        String callDate = currentTime.format(DATE_FORMAT);
        String callTime = currentTime.format(TIME_FORMAT);

        int calledIdx = random.nextInt(callStore.getCallNos().length);
        int callingIdx = random.nextInt(callStore.getCallNos().length);
        int switchIdx = random.nextInt(callStore.getSwitchCountries().length);

        record.setData("SwitchNum", callStore.getSwitch(switchIdx));
        record.setData("Date", callDate);
        record.setData("Time", callTime);
        record.setData("DateTime", callDate + " " + callTime);
        record.setData("CalledNum", callStore.getCallNo(calledIdx));
        record.setData("CallingNum", callStore.getCallNo(callingIdx));
        record.setData("CallPeriod", String.valueOf(random.nextInt(800) + 1));

        return record;
    }

    /**
     * Generate multiple CDR records.
     */
    public List<CDRRecord> generateRecords(int count) {
        List<CDRRecord> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            records.add(generateSingleRecord());
        }
        return records;
    }

    /**
     * Start continuous data generation based on configuration.
     *
     * @param config            Generator configuration
     * @param recordConsumer    Consumer for generated records
     */
    public void startGeneration(GeneratorConfig config, Consumer<CDRRecord> recordConsumer) {
        if (running.get()) {
            log.warn("Generation is already running");
            return;
        }

        running.set(true);
        log.info("Starting CDR generation: {}", config);

        Queue<CDRRecord> callBackQueue = new LinkedList<>();

        OffsetDateTime currentTime = OffsetDateTime.now();
        OffsetDateTime endTime = currentTime.plusHours(config.getDurationHours());

        double timeAdvancementPerSet = (double) config.getDurationHours() / config.getSets();
        log.info("Time Increment Per Set: {}", timeAdvancementPerSet);

        LocalDateTime simulationTime = LocalDateTime.now();

        while (running.get() && OffsetDateTime.now().isBefore(endTime)) {
            OffsetDateTime setStartTime = OffsetDateTime.now();

            log.debug("Simulation time: {}", simulationTime.format(DateTimeFormatter.ofPattern("yyyyMMdd HHmmss")));

            for (int cdr = 0; cdr < config.getCdrPerFile() && running.get(); cdr++) {
                LocalDateTime recordTime = LocalDateTime.now();

                // Determine whether to generate an invalid CDR record
                double pvalue = random.nextDouble();
                boolean invalidRec = pvalue < 0.1;

                // Determine whether there will be a callback (fraud simulation)
                pvalue = random.nextDouble();
                boolean genCallback = pvalue >= config.getCallBackPercent();

                // Generate indices for phone numbers and switches
                int calledIdx = random.nextInt(callStore.getCallNos().length);
                int callingIdx = random.nextInt(callStore.getCallNos().length);
                int switchIdx = random.nextInt(callStore.getSwitchCountries().length);
                int switchAltIdx = random.nextInt(callStore.getSwitchCountries().length);

                // Find an alternate switch
                while (switchAltIdx == switchIdx) {
                    switchAltIdx = random.nextInt(callStore.getSwitchCountries().length);
                }

                // Create CDR record
                CDRRecord record = CDRRecord.createNew();
                record.setData("FileNum", String.valueOf(cdr));
                record.setData("SwitchNum", callStore.getSwitch(switchIdx));

                if (invalidRec) {
                    // Invalid record
                    record.setData("Date", "F");
                    record.setData("Time", "F");
                    record.setData("DateTime", "F F");
                } else {
                    String callDate = recordTime.format(DATE_FORMAT);
                    String callTime = recordTime.format(TIME_FORMAT);

                    record.setData("Date", callDate);
                    record.setData("Time", callTime);
                    record.setData("DateTime", callDate + " " + callTime);
                    record.setData("CalledNum", callStore.getCallNo(calledIdx));
                    record.setData("CallingNum", callStore.getCallNo(callingIdx));

                    // SIM card fraud simulation (callback)
                    if (genCallback) {
                        // For callback, the A->B leg has duration 0
                        record.setData("CallPeriod", "0");

                        // Generate callback record
                        calledIdx = callingIdx;
                        callingIdx = random.nextInt(callStore.getCallNos().length);

                        CDRRecord callbackRec = CDRRecord.createNew();
                        callbackRec.setData("FileNum", String.valueOf(cdr));
                        callbackRec.setData("SwitchNum", callStore.getSwitch(switchAltIdx));

                        // Perturb seconds
                        int perturbSecs = random.nextInt(30);
                        LocalDateTime callbackTime = recordTime.plusMinutes(perturbSecs);

                        callDate = callbackTime.format(DATE_FORMAT);
                        callTime = callbackTime.format(TIME_FORMAT);

                        callbackRec.setData("Date", callDate);
                        callbackRec.setData("Time", callTime);
                        callbackRec.setData("DateTime", callDate + " " + callTime);

                        // Set same calling IMSI (fraud indicator)
                        callbackRec.setData("CallingIMSI", record.getCallingIMSI());
                        callbackRec.setData("CalledNum", callStore.getCallNo(calledIdx));
                        callbackRec.setData("CallingNum", callStore.getCallNo(callingIdx));

                        // Duration of callback
                        int callPeriod = random.nextInt(1000) + 1;
                        callbackRec.setData("CallPeriod", String.valueOf(callPeriod));

                        // Queue the callback record
                        callBackQueue.add(callbackRec);
                        cdr++;
                    } else {
                        int callPeriod = random.nextInt(800) + 1;
                        record.setData("CallPeriod", String.valueOf(callPeriod));
                    }
                }

                // Output the record
                outputRecord(record, recordConsumer);

                // Periodically drain callback queue
                if (!callBackQueue.isEmpty() && (cdr % 7 == 0)) {
                    CDRRecord drec = callBackQueue.poll();
                    if (drec != null) {
                        outputRecord(drec, recordConsumer);
                    }
                }

                // Sleep between records
                try {
                    Thread.sleep(properties.getSleepIntervalMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // Drain remaining callback queue entries
            while (!callBackQueue.isEmpty()) {
                CDRRecord drec = callBackQueue.poll();
                if (drec != null) {
                    outputRecord(drec, recordConsumer);
                }
            }

            // Advance simulation time
            if (timeAdvancementPerSet < 1.0) {
                simulationTime = simulationTime.plusMinutes((long) (timeAdvancementPerSet * 60));
            } else {
                simulationTime = simulationTime.plusHours((long) timeAdvancementPerSet);
            }

            // Sleep between sets
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        running.set(false);
        log.info("CDR generation completed");
    }

    /**
     * Output a record to the configured destinations.
     */
    private void outputRecord(CDRRecord record, Consumer<CDRRecord> recordConsumer) {
        TelcoGeneratorProperties.OutputMode mode = properties.getOutputMode();

        if (mode == TelcoGeneratorProperties.OutputMode.CONSOLE ||
                mode == TelcoGeneratorProperties.OutputMode.BOTH) {
            log.info("{}", record);
        }

        if (mode == TelcoGeneratorProperties.OutputMode.EVENT_HUB ||
                mode == TelcoGeneratorProperties.OutputMode.BOTH) {
            eventHubService.sendRecord(record);
        }

        if (recordConsumer != null) {
            recordConsumer.accept(record);
        }
    }

    /**
     * Stop the current generation.
     */
    public void stopGeneration() {
        running.set(false);
        log.info("CDR generation stop requested");
    }

    /**
     * Check if generation is currently running.
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Reinitialize call store with a new size.
     */
    public void reinitializeCallStore(int size) {
        this.callStore = new CallStore(size);
        log.info("Call store reinitialized with {} phone numbers", size);
    }
}
