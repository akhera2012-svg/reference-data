# Kafka Integration Documentation

## Overview
The Reference Data application now includes Kafka consumer functionality. It listens to a Kafka topic for security data messages and automatically imports them into the database.

## Setup Requirements

### Prerequisites
- Apache Kafka running (default: localhost:9092)
- A Kafka topic for security data (default topic name: `security-data`)

### Starting Kafka Locally (Docker)

```bash
# Start Zookeeper
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:7.4.0 -e ZOOKEEPER_CLIENT_PORT=2181

# Start Kafka Broker
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.4.0

# Create the security-data topic
docker exec -it kafka kafka-topics --create \
  --topic security-data \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

## Configuration

Update `application.properties` with your Kafka settings:

```properties
# Kafka Consumer Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=security-data-consumer
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=true

# Kafka topic configuration
app.kafka.topic.security-data=security-data
```

## How It Works

1. **Message Format**: The Kafka consumer expects CSV-formatted messages. Messages should contain valid CSV data with headers matching the SecurityData entity fields:
   - cusip, isin, cins, issuer_code, issue_date, ticker, currency, country, security_desc, security_type

2. **Processing**: When a message is received:
   - A temporary CSV file is created in the configured directory
   - The existing CSV importer processes the file
   - Data is imported into the database following the same business logic as file-based imports
   - The temporary file is renamed with a `.done` extension after successful processing

3. **Error Handling**: If import fails, the error is logged and the message is not re-processed by default

## Message Examples

### Single Record
```csv
isin,cusip,ticker,currency,country,security_desc,security_type,issue_date
US0378331005,037833100,AAPL,USD,US,Apple Inc,COMMON,2023-01-01
```

### Multiple Records
```csv
isin,cusip,ticker,currency,country,security_desc,security_type,issue_date
US0378331005,037833100,AAPL,USD,US,Apple Inc,COMMON,2023-01-01
US5949181045,594918104,MSFT,USD,US,Microsoft Corporation,COMMON,2023-01-02
US0311001004,031100100,AMZN,USD,US,Amazon.com Inc,COMMON,2023-01-03
```

## Testing with Kafka CLI

### Produce a test message:
```bash
docker exec -it kafka kafka-console-producer \
  --topic security-data \
  --bootstrap-server localhost:9092
```

Then paste your CSV data and press Enter.

### Consume messages (verify):
```bash
docker exec -it kafka kafka-console-consumer \
  --topic security-data \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

## Using system-installed Kafka and Zookeeper

If you have Kafka and Zookeeper installed and running directly on your system (not via Docker), you can start them and manage topics using the included Kafka scripts.

Linux / macOS (from the Kafka distribution root):

```bash
# Start Zookeeper (in a separate terminal)
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka broker (in another terminal)
bin/kafka-server-start.sh config/server.properties

# Create the `security-data` topic (run after broker is up)
bin/kafka-topics.sh --create --topic security-data \
  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

Windows (from Kafka distribution root)

```powershell
# Start Zookeeper (in a separate PowerShell)
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

# Start Kafka broker (in another PowerShell)
.\bin\windows\kafka-server-start.bat .\config\server.properties

# Create the `security-data` topic (run after broker is up)
.\bin\windows\kafka-topics.bat --create --topic security-data \
  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

Note: Adjust `server.properties` and `zookeeper.properties` if your advertised listeners or ports differ from the defaults.

## Producing messages to a standalone Kafka (example)

Below are examples of producing CSV messages directly to a Kafka broker that is running on your system. The consumer expects CSV-formatted messages with a header row.

Single-record example (Linux/macOS):

```bash
echo "isin,cusip,ticker,currency,country,security_desc,security_type,issue_date" > /tmp/msg.csv
echo "US0378331005,037833100,AAPL,USD,US,Apple Inc,COMMON,2023-01-01" >> /tmp/msg.csv
cat /tmp/msg.csv | bin/kafka-console-producer.sh --topic security-data --bootstrap-server localhost:9092
```

Single-record example (Windows PowerShell):

```powershell
"isin,cusip,ticker,currency,country,security_desc,security_type,issue_date" | Out-File -FilePath C:\temp\msg.csv -Encoding utf8
"US0378331005,037833100,AAPL,USD,US,Apple Inc,COMMON,2023-01-01" | Add-Content -Path C:\temp\msg.csv
Get-Content C:\temp\msg.csv | .\bin\windows\kafka-console-producer.bat --topic security-data --bootstrap-server localhost:9092
```

Multi-record example (here-doc, Linux/macOS):

```bash
cat <<'EOF' | bin/kafka-console-producer.sh --topic security-data --bootstrap-server localhost:9092
isin,cusip,ticker,currency,country,security_desc,security_type,issue_date
US0378331005,037833100,AAPL,USD,US,Apple Inc,COMMON,2023-01-01
US5949181045,594918104,MSFT,USD,US,Microsoft Corporation,COMMON,2023-01-02
EOF
```

After producing a message, the running application (with the Kafka consumer enabled) will log receipt and import the data into the database.

## Producing messages via Kafka REST Proxy (curl)

If you have a Kafka REST Proxy (for example Confluent REST Proxy) running, you can produce messages over HTTP using `curl`. Below are concise examples showing how to send CSV payloads to the `security-data` topic.

Note: REST Proxy typically expects JSON payloads describing one or more `records`. In these examples we send the CSV text as the record `value` (plain string).

Example (single CSV message):

```bash
# Replace the URL/port if your REST Proxy runs elsewhere
REST_PROXY=http://localhost:8082
TOPIC=security-data

curl -X POST "$REST_PROXY/topics/$TOPIC" \
  -H "Content-Type: application/vnd.kafka.json.v2+json" \
  -d '{"records":[{"value":"isin,cusip,ticker,currency,country,security_desc,security_type,issue_date\nUS0378331005,037833100,AAPL,USD,US,Apple Inc,COMMON,2023-01-01"}]}'
```

Example (multiple CSV rows in one request):

```bash
REST_PROXY=http://localhost:8082
TOPIC=security-data

curl -X POST "$REST_PROXY/topics/$TOPIC" \
  -H "Content-Type: application/vnd.kafka.json.v2+json" \
  -d '{"records":[
        {"value":"isin,cusip,ticker,currency,country,security_desc,security_type,issue_date\nUS0378331005,037833100,AAPL,USD,US,Apple Inc,COMMON,2023-01-01"},
        {"value":"US5949181045,594918104,MSFT,USD,US,Microsoft Corporation,COMMON,2023-01-02"}
    ]}'
```

Windows PowerShell (single message):

```powershell
$restProxy = 'http://localhost:8082'
$topic = 'security-data'
$payload = '{"records":[{"value":"isin,cusip,ticker,currency,country,security_desc,security_type,issue_date\nUS0378331005,037833100,AAPL,USD,US,Apple Inc,COMMON,2023-01-01"}]}'
Invoke-RestMethod -Method Post -Uri "$restProxy/topics/$topic" -Body $payload -ContentType 'application/vnd.kafka.json.v2+json'
```

If the REST Proxy is configured with authentication or TLS, add the necessary headers/options (`-u` for basic auth, `--cacert`/`--cert`/`--key`, etc.). After a successful POST the proxy will respond with metadata about the produced records; the application will log receipt and import the CSV contents.

## Dual Import Mechanisms

The application now supports two import methods running simultaneously:

1. **File-based**: `CsvDirectoryWatcher` monitors a directory for CSV files (configurable path: `/data`)
2. **Kafka-based**: `SecurityDataKafkaConsumer` listens to Kafka topic for messages

Both mechanisms feed into the same `CsvImporter` class, ensuring consistent data processing and database updates.

## Monitoring

Application logs will show:
- `*** Received message from Kafka: ...` - When messages are received
- `*** Created temporary CSV file: ...` - When temporary files are created
- `*** Successfully imported security data from Kafka message` - On successful import
- `*** Failed to process Kafka message: ...` - On import failures

## Disabling Kafka (Optional)

To disable the Kafka consumer while keeping the application running:
1. Remove or comment out the `spring.kafka.*` properties in `application.properties`
2. The file watcher will continue to work for file-based imports
