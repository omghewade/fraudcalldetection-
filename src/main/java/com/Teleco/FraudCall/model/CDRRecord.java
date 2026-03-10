package com.Teleco.FraudCall.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

/**
 * Call Detail Record (CDR) model class.
 * Represents a telecommunication call record with fraud detection capabilities.
 */
@Data
@NoArgsConstructor
public class CDRRecord {

    private static final Random RANDOM = new Random();

    // Static lookup arrays for random data generation
    private static final String[] SERVICE_TYPE_LIST = {"a", "b", "S", "V"};
    private static final String[] TIME_TYPE_LIST = {"a", "d", "r", "s"};
    private static final String[] END_TYPE_LIST = {"0", "3", "4"};
    private static final String[] OUTGOING_TRUNK_LIST = {
            "F", "442", "623", "418", "425", "443", "426", "621", "614", "609",
            "419", "402", "411", "422", "420", "423", "421", "300", "400", "405", "409", "424"
    };
    private static final String[] IMSI_LIST = {
            "466923300507919", "466921602131264", "466923200348594", "466922002560205",
            "466922201102759", "466922702346260", "466920400352400", "466922202546859",
            "466923000886460", "466921302209862", "466923101048691", "466921200135361",
            "466922202613463", "466921402416657", "466921402237651", "466922202679249",
            "466923300236137", "466921602343040", "466920403025604", "262021390056324",
            "466920401237309", "466922000696024", "466923100098619", "466922702341485",
            "466922200432822", "466923000464324", "466923200779222", "466923100807296",
            "466923200408045"
    };
    private static final String[] MSRN_LIST = {
            "886932428687", "886932429021", "886932428306", "1415982715962", "886932429979",
            "1416916990491", "886937415371", "886932428876", "886932428688", "1412983121877",
            "886932429242", "1416955584542", "886932428258", "1412930064972", "886932429155",
            "886932423548", "1415980332015", "14290800303585", "14290800033338", "886932429626",
            "886932428112", "1417955696232", "1418986850453", "886932428927", "886932429827",
            "886932429507", "1416960750071", "886932428242", "886932428134", "886932429825", ""
    };

    private static final String[] COLUMNS = {
            "RecordType", "SystemIdentity", "FileNum", "SwitchNum", "CallingNum", "CallingIMSI",
            "CalledNum", "CalledIMSI", "Date", "Time", "TimeType", "CallPeriod", "CallingCellID",
            "CalledCellID", "ServiceType", "Transfer", "IMEI", "EndType", "IncomingTrunk",
            "OutgoingTrunk", "MSRN", "CalledNum2", "FCIFlag", "DateTime"
    };

    // Fields
    @JsonProperty("RecordType")
    private String recordType;

    @JsonProperty("SystemIdentity")
    private String systemIdentity;

    @JsonProperty("FileNum")
    private String fileNum;

    @JsonProperty("SwitchNum")
    private String switchNum;

    @JsonProperty("CallingNum")
    private String callingNum;

    @JsonProperty("CallingIMSI")
    private String callingIMSI;

    @JsonProperty("CalledNum")
    private String calledNum;

    @JsonProperty("CalledIMSI")
    private String calledIMSI;

    @JsonProperty("DateS")
    private String dateS;

    @JsonProperty("TimeS")
    private String timeS;

    @JsonProperty("TimeType")
    private int timeType;

    @JsonProperty("CallPeriod")
    private int callPeriod;

    @JsonProperty("CallingCellID")
    private String callingCellID;

    @JsonProperty("CalledCellID")
    private String calledCellID;

    @JsonProperty("ServiceType")
    private String serviceType;

    @JsonProperty("Transfer")
    private int transfer;

    @JsonProperty("IMEI")
    private String imei;

    @JsonProperty("EndType")
    private String endType;

    @JsonProperty("IncomingTrunk")
    private String incomingTrunk;

    @JsonProperty("OutgoingTrunk")
    private String outgoingTrunk;

    @JsonProperty("MSRN")
    private String msrn;

    @JsonProperty("CalledNum2")
    private String calledNum2;

    @JsonProperty("FCIFlag")
    private String fciFlag;

    @JsonProperty("callrecTime")
    private LocalDateTime callrecTime;

    // Internal data store for flexible field access
    private transient Map<String, Object> data = new HashMap<>();

    /**
     * Initialize CDR record with default random values.
     */
    public void initialize() {
        data = new HashMap<>();

        // Initialize default values
        data.put("SystemIdentity", "d0");
        data.put("RecordType", "MO");
        this.systemIdentity = "d0";
        this.recordType = "MO";

        int idx = RANDOM.nextInt(TIME_TYPE_LIST.length);
        data.put("TimeType", idx);
        this.timeType = idx;

        idx = RANDOM.nextInt(SERVICE_TYPE_LIST.length);
        data.put("ServiceType", SERVICE_TYPE_LIST[idx]);
        this.serviceType = SERVICE_TYPE_LIST[idx];

        idx = RANDOM.nextInt(END_TYPE_LIST.length);
        data.put("EndType", END_TYPE_LIST[idx]);
        this.endType = END_TYPE_LIST[idx];

        idx = RANDOM.nextInt(OUTGOING_TRUNK_LIST.length);
        data.put("OutgoingTrunk", OUTGOING_TRUNK_LIST[idx]);
        this.outgoingTrunk = OUTGOING_TRUNK_LIST[idx];

        int transferVal = RANDOM.nextInt(2);
        data.put("Transfer", transferVal);
        this.transfer = transferVal;

        idx = RANDOM.nextInt(IMSI_LIST.length);
        data.put("CallingIMSI", IMSI_LIST[idx]);
        this.callingIMSI = IMSI_LIST[idx];

        idx = RANDOM.nextInt(IMSI_LIST.length);
        data.put("CalledIMSI", IMSI_LIST[idx]);
        this.calledIMSI = IMSI_LIST[idx];

        idx = RANDOM.nextInt(MSRN_LIST.length);
        data.put("MSRN", MSRN_LIST[idx]);
        this.msrn = MSRN_LIST[idx];
    }

    /**
     * Set data for a specific field.
     */
    public void setData(String key, String value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);

        switch (key) {
            case "RecordType" -> this.recordType = value;
            case "SystemIdentity" -> this.systemIdentity = value;
            case "FileNum" -> this.fileNum = value;
            case "SwitchNum" -> this.switchNum = value;
            case "CallingNum" -> this.callingNum = value;
            case "CallingIMSI" -> this.callingIMSI = value;
            case "CalledNum" -> this.calledNum = value;
            case "CalledIMSI" -> this.calledIMSI = value;
            case "Date" -> this.dateS = value;
            case "Time" -> this.timeS = value;
            case "TimeType" -> this.timeType = Integer.parseInt(value);
            case "CallPeriod" -> this.callPeriod = Integer.parseInt(value);
            case "CallingCellID" -> this.callingCellID = value;
            case "CalledCellID" -> this.calledCellID = value;
            case "ServiceType" -> this.serviceType = value;
            case "Transfer" -> this.transfer = Integer.parseInt(value);
            case "IncomingTrunk" -> this.incomingTrunk = value;
            case "OutgoingTrunk" -> this.outgoingTrunk = value;
            case "MSRN" -> this.msrn = value;
            case "CalledNum2" -> this.calledNum2 = value;
            case "FCIFlag" -> this.fciFlag = value;
            case "DateTime" -> {
                if (value != null && value.length() > 13) {
                    try {
                        int year = Integer.parseInt(value.substring(0, 4));
                        int month = Integer.parseInt(value.substring(4, 6));
                        int day = Integer.parseInt(value.substring(6, 8));
                        int hour = Integer.parseInt(value.substring(9, 11));
                        int min = Integer.parseInt(value.substring(11, 13));
                        int secs = Integer.parseInt(value.substring(13, 15));
                        this.callrecTime = LocalDateTime.of(year, month, day, hour, min, secs)
                                .atOffset(ZoneOffset.UTC).toLocalDateTime();
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
            }
        }
    }

    /**
     * Get data for a specific field.
     */
    public Object getData(String key) {
        return data != null ? data.get(key) : null;
    }

    /**
     * Convert record to CSV format.
     */
    public String toCsv() {
        StringJoiner joiner = new StringJoiner(",");
        for (String column : COLUMNS) {
            Object value = data != null ? data.get(column) : null;
            joiner.add(value != null ? value.toString() : "");
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        return toCsv();
    }

    /**
     * Create a new initialized CDR record.
     */
    public static CDRRecord createNew() {
        CDRRecord record = new CDRRecord();
        record.initialize();
        return record;
    }
}
