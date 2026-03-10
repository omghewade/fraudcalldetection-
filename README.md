# FraudCall - Telco CDR Generator

A Spring Boot application that generates telecom Call Detail Records (CDR) with fraud simulation capabilities. This is a Java port of the [TelcoGeneratorCore](https://github.com/Azure/azure-stream-analytics/tree/master/DataGenerators/TelcoGeneratorCore) .NET application.

## Overview

This application simulates a telecommunications system generating call records. It includes:
- **CDR Generation**: Generate realistic Call Detail Records
- **Fraud Simulation**: Simulate SIM card fraud with configurable probability  
- **Azure Event Hub Integration**: Send generated records to Azure Event Hubs for stream processing
- **REST API**: HTTP endpoints for on-demand record generation
- **CLI Mode**: Command-line arguments for batch generation (same as original .NET app)

## Features

- Generate CDR records with realistic telecom data (IMSI, MSRN, switch info, etc.)
- SIM card fraud simulation via callback pattern detection
- Configurable fraud probability
- Support for Azure Event Hubs streaming
- REST API for interactive use
- Command-line interface for batch processing

## Building

```bash
mvn clean package
```

## Running

### REST API Mode (default)

```bash
java -jar target/FraudCall-0.0.1-SNAPSHOT.jar
```

The application starts on port 8080. Access the API at `http://localhost:8080/api/telco`

### CLI Mode (compatible with original .NET app)

```bash
java -jar target/FraudCall-0.0.1-SNAPSHOT.jar [NumCDRsPerHour] [FraudProbability] [DurationHours]
```

Example:
```bash
java -jar target/FraudCall-0.0.1-SNAPSHOT.jar 100 0.2 1
```
- `100` - Generate 100 CDR records per hour
- `0.2` - 20% probability of SIM card fraud
- `1` - Run for 1 hour

## REST API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/telco` | GET | Usage information |
| `/api/telco/health` | GET | Health check |
| `/api/telco/record` | GET | Generate single CDR record |
| `/api/telco/records?count=N` | GET | Generate N CDR records |
| `/api/telco/start` | POST | Start continuous generation |
| `/api/telco/stop` | POST | Stop generation |
| `/api/telco/status` | GET | Get generator status |
| `/api/telco/send-batch?count=N` | POST | Generate and send N records to Event Hub |
| `/api/telco/stream?count=N&delayMs=D` | GET | Stream N records with D ms delay |

### Examples

Generate single record:
```bash
curl http://localhost:8080/api/telco/record
```

Generate 10 records:
```bash
curl http://localhost:8080/api/telco/records?count=10
```

Start generation:
```bash
curl -X POST "http://localhost:8080/api/telco/start?cdrPerHour=100&fraudProbability=0.2&durationHours=1"
```

Stop generation:
```bash
curl -X POST http://localhost:8080/api/telco/stop
```

## Configuration

Configure via `application.properties` or environment variables:

```properties
# Server configuration
server.port=8080

# Event Hub Configuration (optional)
telco.generator.event-hub-name=your-event-hub-name
telco.generator.event-hub-connection-string=Endpoint=sb://...

# Generator Settings
telco.generator.cdr-per-hour=100
telco.generator.fraud-probability=0.2
telco.generator.duration-hours=1
telco.generator.phone-number-pool-size=100000
telco.generator.sleep-interval-ms=100
telco.generator.enabled=true

# Output Mode: CONSOLE, EVENT_HUB, or BOTH
telco.generator.output-mode=CONSOLE
```

### Environment Variables

All properties can be set via environment variables:
```bash
export TELCO_GENERATOR_EVENT_HUB_NAME=your-hub
export TELCO_GENERATOR_EVENT_HUB_CONNECTION_STRING="Endpoint=sb://..."
export TELCO_GENERATOR_OUTPUT_MODE=EVENT_HUB
```

## CDR Record Structure

Each generated record contains:

| Field | Description |
|-------|-------------|
| RecordType | Call record type (MO = Mobile Originated) |
| SystemIdentity | System identifier |
| SwitchNum | Switch country (US, China, UK, Germany, Australia) |
| CallingNum | Caller phone number |
| CallingIMSI | Caller IMSI |
| CalledNum | Called phone number |
| CalledIMSI | Called party IMSI |
| DateS | Call date (yyyyMMdd) |
| TimeS | Call time (HHmmss) |
| CallPeriod | Call duration in seconds |
| ServiceType | Service type code |
| Transfer | Transfer flag |
| OutgoingTrunk | Outgoing trunk code |
| MSRN | Mobile Station Roaming Number |

## Fraud Simulation

The generator simulates SIM card fraud using a callback pattern:
1. An initial call A→B is made with 0 duration
2. Shortly after, a callback C→A occurs from a different switch
3. Both records share the same CallingIMSI (fraud indicator)

This pattern can be detected by stream processing systems like Azure Stream Analytics.

## Project Structure

```
src/main/java/com/Teleco/FraudCall/
├── FraudCallApplication.java       # Main Spring Boot application
├── config/
│   └── TelcoGeneratorProperties.java  # Configuration properties
├── controller/
│   └── TelcoGeneratorController.java  # REST API controller
├── model/
│   ├── CDRRecord.java              # CDR data model
│   ├── CallStore.java              # Phone number store
│   └── GeneratorConfig.java        # Generation config
├── runner/
│   └── TelcoGeneratorRunner.java   # CLI runner
└── service/
    ├── CDRGeneratorService.java    # Core generation logic
    └── EventHubService.java        # Azure Event Hub integration
```

## License

Based on Microsoft's TelcoGeneratorCore, licensed under the Microsoft Public License.

## Original Source

Ported from: https://github.com/Azure/azure-stream-analytics/tree/master/DataGenerators/TelcoGeneratorCore
