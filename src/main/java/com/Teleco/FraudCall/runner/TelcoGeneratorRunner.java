package com.Teleco.FraudCall.runner;

import com.Teleco.FraudCall.config.TelcoGeneratorProperties;
import com.Teleco.FraudCall.model.GeneratorConfig;
import com.Teleco.FraudCall.service.CDRGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Command-line runner for the Telco Generator.
 * Supports the same arguments as the original .NET application:
 * telcodatagen [#NumCDRsPerHour] [SIM Card Fraud Probability] [#DurationHours]
 */
@Component
public class TelcoGeneratorRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TelcoGeneratorRunner.class);

    private final CDRGeneratorService generatorService;
    private final TelcoGeneratorProperties properties;

    public TelcoGeneratorRunner(CDRGeneratorService generatorService, TelcoGeneratorProperties properties) {
        this.generatorService = generatorService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String[] sourceArgs = args.getSourceArgs();

        // If command-line arguments provided, use them to start generation
        if (sourceArgs.length >= 3) {
            try {
                int cdrPerHour = Integer.parseInt(sourceArgs[0]);
                float fraudProbability = Float.parseFloat(sourceArgs[1]);
                int durationHours = Integer.parseInt(sourceArgs[2]);

                log.info("Starting generation with CLI arguments:");
                log.info("  CDR per hour: {}", cdrPerHour);
                log.info("  Fraud probability: {}", fraudProbability);
                log.info("  Duration hours: {}", durationHours);

                GeneratorConfig config = new GeneratorConfig(cdrPerHour, fraudProbability, durationHours);

                // Run generation in a separate thread to not block Spring Boot startup
                new Thread(() -> {
                    generatorService.startGeneration(config, null);
                }).start();

            } catch (NumberFormatException e) {
                printUsage();
            }
        } else if (sourceArgs.length > 0 && sourceArgs.length < 3) {
            printUsage();
        } else {
            log.info("No CLI arguments provided. Use REST API or provide arguments:");
            log.info("  [#NumCDRsPerHour] [SIM Card Fraud Probability] [#DurationHours]");
            log.info("REST API available at: http://localhost:8080/api/telco");

            // If auto-start is enabled in properties, start generation
            if (properties.isEnabled() && properties.getCdrPerHour() > 0) {
                log.info("Auto-start is enabled. Starting generation with configured properties...");

                GeneratorConfig config = new GeneratorConfig(
                        properties.getCdrPerHour(),
                        properties.getFraudProbability(),
                        properties.getDurationHours()
                );

                // Commented out auto-start by default - uncomment to enable
                // new Thread(() -> generatorService.startGeneration(config, null)).start();
            }
        }
    }

    private void printUsage() {
        log.error("Usage: java -jar FraudCall.jar [#NumCDRsPerHour] [SIM Card Fraud Probability] [#DurationHours]");
        log.error("Example: java -jar FraudCall.jar 100 0.2 1");
        log.error("         100 CDR records per hour");
        log.error("         20% fraud probability");
        log.error("         Run for 1 hour");
    }
}
