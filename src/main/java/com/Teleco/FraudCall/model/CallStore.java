package com.Teleco.FraudCall.model;

import java.util.Random;

/**
 * Store for generated phone numbers and switch information.
 */
public class CallStore {

    private static final String[] NUM_PREFIX = {"0123", "1234", "2345", "3456", "4567", "5678", "6789", "7890"};
    private static final Random RANDOM = new Random();

    private final String[] callNos;
    private final String[] switchCountries = {"US", "China", "UK", "Germany", "Australia"};

    /**
     * Create a call store with generated phone numbers.
     *
     * @param size Number of phone numbers to generate
     */
    public CallStore(int size) {
        callNos = new String[size];

        // Generate phone numbers
        for (int i = 0; i < size; i++) {
            int prefixIdx = RANDOM.nextInt(NUM_PREFIX.length);
            String prefix = NUM_PREFIX[prefixIdx];
            callNos[i] = prefix + String.format("%05d", i);
        }
    }

    public String[] getCallNos() {
        return callNos;
    }

    public String[] getSwitchCountries() {
        return switchCountries;
    }

    /**
     * Get a random phone number from the store.
     */
    public String getRandomCallNo() {
        return callNos[RANDOM.nextInt(callNos.length)];
    }

    /**
     * Get a random switch country.
     */
    public String getRandomSwitch() {
        return switchCountries[RANDOM.nextInt(switchCountries.length)];
    }

    /**
     * Get phone number at specific index.
     */
    public String getCallNo(int index) {
        return callNos[index % callNos.length];
    }

    /**
     * Get switch country at specific index.
     */
    public String getSwitch(int index) {
        return switchCountries[index % switchCountries.length];
    }
}
