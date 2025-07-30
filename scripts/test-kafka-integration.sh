#!/bin/bash

# OrderHub - Test Kafka Integration
# This script tests the Kafka integration by creating sample orders and monitoring events

set -e

echo "🧪 Testing OrderHub Kafka Integration"
echo "===================================="

# Check if application is running
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "❌ Application is not running on port 8080"
    echo "Please start the application with: mvn spring-boot:run -Dspring-boot.run.profiles=local-kafka"
    exit 1
fi

echo "✅ Application is running"

# Check if Kafka UI is accessible
if ! curl -s http://localhost:8090 > /dev/null; then
    echo "⚠️  Kafka UI is not accessible on port 8090"
    echo "You can still test the integration, but won't be able to see events in the UI"
else
    echo "✅ Kafka UI is accessible at http://localhost:8090"
fi

echo ""
echo "🔄 Testing Order Creation and Event Publishing..."
echo ""

# Create a customer first
echo "📝 Creating test customer..."
CUSTOMER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Customer",
    "email": "test@example.com",
    "phone": "+1234567890"
  }')

CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -z "$CUSTOMER_ID" ]; then
    echo "❌ Failed to create customer"
    echo "Response: $CUSTOMER_RESPONSE"
    exit 1
fi

echo "✅ Customer created with ID: $CUSTOMER_ID"

# Create an order
echo "📦 Creating test order..."
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": $CUSTOMER_ID,
    \"items\": [
      {
        \"productName\": \"Test Product\",
        \"productSku\": \"TEST-001\",
        \"quantity\": 2,
        \"unitPrice\": 29.99
      }
    ],
    \"paymentMethod\": \"PIX\"
  }")

ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -z "$ORDER_ID" ]; then
    echo "❌ Failed to create order"
    echo "Response: $ORDER_RESPONSE"
    exit 1
fi

echo "✅ Order created with ID: $ORDER_ID"

# Wait a moment for events to be processed
echo "⏳ Waiting for events to be processed..."
sleep 3

# Check order status
echo "🔍 Checking order status..."
ORDER_STATUS_RESPONSE=$(curl -s http://localhost:8080/api/orders/$ORDER_ID)
echo "Order Status Response: $ORDER_STATUS_RESPONSE"

echo ""
echo "🎉 Test completed successfully!"
echo "===================================="
echo "📊 Check the following to verify Kafka integration:"
echo ""
echo "1. Application logs should show:"
echo "   - 'Evento OrderCreated publicado com sucesso para pedido $ORDER_ID'"
echo "   - Kafka connection and topic creation messages"
echo ""
echo "2. Kafka UI (http://localhost:8090):"
echo "   - Check 'orders.created' topic for the published event"
echo "   - Verify consumer groups are active"
echo ""
echo "3. Order details:"
echo "   - Customer ID: $CUSTOMER_ID"
echo "   - Order ID: $ORDER_ID"
echo "   - Order Status: Check the response above"
echo ""
echo "To monitor events in real-time, you can also use:"
echo "curl -N http://localhost:8080/public/orders/$ORDER_ID/status/stream"
echo ""
echo "For more detailed testing, see: docs/KAFKA_LOCAL_SETUP.md"