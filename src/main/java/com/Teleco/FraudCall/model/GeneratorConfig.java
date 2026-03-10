package com.Teleco.FraudCall.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for CDR data generation.
 */
@Data
@NoArgsConstructor
public class GeneratorConfig {

    private int sets = 1;
    private int filesPerDump = 1;
    private int cdrPerFile = 100;
    private float callBackPercent = 0.2f;
    private int durationHours = 1;

    /**
     * Full constructor.
     */
    public GeneratorConfig(int sets, int filesPerDump, int cdrPerFile, float callBackPercent, int durationHours) {
        this.sets = sets;
        this.filesPerDump = filesPerDump;
        this.cdrPerFile = cdrPerFile;
        this.callBackPercent = callBackPercent;
        this.durationHours = durationHours;
    }

    /**
     * Simplified constructor (assumes 1 file and 1 set).
     */
    public GeneratorConfig(int cdrPerFile, float callBackPercent, int durationHours) {
        this.sets = 1;
        this.filesPerDump = 1;
        this.cdrPerFile = cdrPerFile;
        this.callBackPercent = callBackPercent;
        this.durationHours = durationHours;
    }

    @Override
    public String toString() {
        return String.format("#Sets: %d, #FilesDump: %d, #CDRPerFile: %d, %%CallBack: %.2f, #DurationHours: %d",
                sets, filesPerDump, cdrPerFile, callBackPercent, durationHours);
    }
}
