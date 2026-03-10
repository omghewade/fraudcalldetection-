package com.Teleco.FraudCall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Telco Generator.
 */
@ConfigurationProperties(prefix = "telco.generator")
public class TelcoGeneratorProperties {

    // Event Hub settings
    private String eventHubName = "";
    private String eventHubConnectionString = "";

    // Generator settings
    private int cdrPerHour = 100;
    private float fraudProbability = 0.2f;
    private int durationHours = 1;
    private int phoneNumberPoolSize = 100000;
    private int sleepIntervalMs = 100;
    private boolean enabled = true;

    // Output settings
    private OutputMode outputMode = OutputMode.CONSOLE;

    public enum OutputMode {
        CONSOLE,
        EVENT_HUB,
        BOTH
    }

    // Getters and Setters
    public String getEventHubName() {
        return eventHubName;
    }

    public void setEventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
    }

    public String getEventHubConnectionString() {
        return eventHubConnectionString;
    }

    public void setEventHubConnectionString(String eventHubConnectionString) {
        this.eventHubConnectionString = eventHubConnectionString;
    }

    public int getCdrPerHour() {
        return cdrPerHour;
    }

    public void setCdrPerHour(int cdrPerHour) {
        this.cdrPerHour = cdrPerHour;
    }

    public float getFraudProbability() {
        return fraudProbability;
    }

    public void setFraudProbability(float fraudProbability) {
        this.fraudProbability = fraudProbability;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public int getPhoneNumberPoolSize() {
        return phoneNumberPoolSize;
    }

    public void setPhoneNumberPoolSize(int phoneNumberPoolSize) {
        this.phoneNumberPoolSize = phoneNumberPoolSize;
    }

    public int getSleepIntervalMs() {
        return sleepIntervalMs;
    }

    public void setSleepIntervalMs(int sleepIntervalMs) {
        this.sleepIntervalMs = sleepIntervalMs;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public OutputMode getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(OutputMode outputMode) {
        this.outputMode = outputMode;
    }
}
