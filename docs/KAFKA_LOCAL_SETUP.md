# Kafka Local Development Setup

This guide explains how to set up and run Apache Kafka locally for OrderHub development, ensuring proper integration with the application.

## Prerequisites

- Docker and Docker Compose installed
- Java 21+ installed
- Maven 3.6+ installed

## Quick Start

### 1. Start Kafka Infrastructure

The project includes a `docker-compose.yml` file with all necessary Kafka infrastructure. To start only the Kafka services:

```bash
# Start Kafka, Zookeeper, and Kafka UI
docker-compose up -d zookeeper kafka kafka-ui
```

This will start:
- **Zookeeper** on port `2181`
- **Kafka** on port `9092`
- **Kafka UI** on port `8090` (http://localhost:8090)

### 2. Run the Application with Kafka

Use the `local-kafka` profile to enable Kafka integration:

```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local-kafka

# Or using Java directly
java -jar target/orderhub-0.0.1-SNAPSHOT.jar --spring.profiles.active=local-kafka
```

## Configuration Details

### Application Profiles

The application has different profiles for Kafka:

- **`local`**: Kafka disabled (default for local development)
- **`local-kafka`**: Kafka enabled with local configuration
- **`prod`**: Kafka enabled for production
- **`docker`**: Kafka enabled for Docker environment

### Kafka Configuration (local-kafka profile)

```yaml
spring:
  kafka:
    enabled: true
    bootstrap-servers: localhost:9092
    consumer:
      group-id: orderhub-local-group
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

## Topics and Events

The application automatically creates the following topics:

### Main Topics
- `orders.created` - Order creation events
- `payments.confirmed` - Payment confirmation events
- `stock.reserved` - Stock reservation events
- `invoice.generated` - Invoice generation events

### Dead Letter Topics (DLT)
- `orders.created.dlt` - Failed order creation events
- `payments.confirmed.dlt` - Failed payment confirmation events
- `stock.reserved.dlt` - Failed stock reservation events
- `invoice.generated.dlt` - Failed invoice generation events

## Testing the Integration

### Quick Test Script

Use the provided test script to quickly validate the entire setup:

```bash
# Make sure Kafka is running and application is started with local-kafka profile
./scripts/start-kafka-local.sh
mvn spring-boot:run -Dspring-boot.run.profiles=local-kafka

# In another terminal, run the test
./scripts/test-kafka-integration.sh
```

This script will:
- Create a test customer and order
- Verify event publishing
- Check application logs
- Provide guidance on monitoring events

### Manual Testing

#### 1. Verify Kafka is Running

```bash
# Check if Kafka container is running
docker ps | grep kafka

# Check Kafka logs
docker logs orderhub-kafka
```

### 2. Access Kafka UI

Open http://localhost:8090 in your browser to:
- View topics
- Monitor messages
- Check consumer groups
- Inspect topic configurations

### 3. Test Event Publishing

Create an order through the API to test event publishing:

```bash
# Create a customer first
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+1234567890"
  }'

# Create an order (replace {customerId} with actual ID)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "{customerId}",
    "items": [
      {
        "productName": "Test Product",
        "quantity": 2,
        "price": 29.99
      }
    ]
  }'
```

### 4. Monitor Events

After creating an order, you should see:
1. An `OrderCreatedEvent` in the `orders.created` topic
2. Application logs showing event publishing
3. Events visible in Kafka UI

### 5. Check Application Logs

Look for these log messages indicating successful Kafka integration:

```
# Event publishing
Publishing OrderCreatedEvent for order: {orderId}
Event published successfully to topic: orders.created

# Event consumption (if applicable)
Received event from topic: stock.reserved
Processing StockReservedEvent for order: {orderId}
```

## Troubleshooting

### Common Issues

#### 1. Connection Refused Error
```
org.apache.kafka.common.errors.TimeoutException: Failed to update metadata
```

**Solution**: Ensure Kafka is running and accessible on localhost:9092
```bash
docker-compose up -d kafka
```

#### 2. Topic Creation Issues
```
org.apache.kafka.common.errors.TopicExistsException
```

**Solution**: This is normal - topics are created automatically if they don't exist.

#### 3. Serialization Errors
```
com.fasterxml.jackson.core.JsonProcessingException
```

**Solution**: Check that event classes have proper JSON annotations and are in the trusted packages.

#### 4. Consumer Group Issues
```
org.apache.kafka.clients.consumer.CommitFailedException
```

**Solution**: Restart the application to reset the consumer group.

### Useful Commands

```bash
# Stop all Kafka services
docker-compose down

# View Kafka logs
docker logs orderhub-kafka -f

# Access Kafka container
docker exec -it orderhub-kafka bash

# List topics (from inside Kafka container)
kafka-topics --bootstrap-server localhost:9092 --list

# Consume messages from a topic (from inside Kafka container)
kafka-console-consumer --bootstrap-server localhost:9092 --topic orders.created --from-beginning
```

## Development Workflow

### Starting Development
1. Start Kafka infrastructure: `docker-compose up -d zookeeper kafka kafka-ui`
2. Run application: `mvn spring-boot:run -Dspring-boot.run.profiles=local-kafka`
3. Open Kafka UI: http://localhost:8090
4. Test API endpoints and monitor events

### Stopping Development
1. Stop application: `Ctrl+C`
2. Stop Kafka: `docker-compose down`

## Event Classes

The application uses these event classes for Kafka messaging:

- `OrderCreatedEvent` - Published when an order is created
- `PaymentConfirmedEvent` - Published when payment is confirmed
- `StockReservedEvent` - Consumed when stock is reserved
- `InvoiceGeneratedEvent` - Consumed when invoice is generated

All events are located in the `com.kipperdev.orderhub.event` package.

## Configuration Reference

### Environment Variables (Optional)

You can override default configurations using environment variables:

```bash
# Kafka bootstrap servers
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Consumer group ID
export KAFKA_CONSUMER_GROUP_ID=orderhub-local-group

# Enable/disable Kafka
export SPRING_KAFKA_ENABLED=true
```

### Application Properties Override

Create `application-local-kafka.properties` for custom local settings:

```properties
# Custom Kafka settings
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=my-custom-group
spring.kafka.consumer.auto-offset-reset=latest

# Enable debug logging
logging.level.org.apache.kafka=DEBUG
logging.level.org.springframework.kafka=DEBUG
```

## Next Steps

- Explore the Kafka UI to understand message flow
- Implement custom event handlers
- Add monitoring and alerting for production use
- Consider using Kafka Streams for complex event processing

For more information about the OrderHub application, see the main [README.md](../README.md).