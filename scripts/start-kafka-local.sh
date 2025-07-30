#!/bin/bash

# OrderHub - Start Kafka Local Development Environment
# This script starts the necessary Kafka infrastructure for local development

set -e

echo "🚀 Starting Kafka Local Development Environment for OrderHub"
echo "================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running. Please start Docker first."
    exit 1
fi

echo "✅ Docker is running"

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Error: docker-compose.yml not found. Please run this script from the project root."
    exit 1
fi

echo "✅ Found docker-compose.yml"

# Start Kafka infrastructure
echo "🔄 Starting Kafka infrastructure..."
docker-compose up -d zookeeper kafka kafka-ui

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 10

# Check if services are running
echo "🔍 Checking service status..."

if docker ps | grep -q "orderhub-zookeeper"; then
    echo "✅ Zookeeper is running on port 2181"
else
    echo "❌ Zookeeper failed to start"
    exit 1
fi

if docker ps | grep -q "orderhub-kafka"; then
    echo "✅ Kafka is running on port 9092"
else
    echo "❌ Kafka failed to start"
    exit 1
fi

if docker ps | grep -q "orderhub-kafka-ui"; then
    echo "✅ Kafka UI is running on port 8090"
else
    echo "❌ Kafka UI failed to start"
    exit 1
fi

echo ""
echo "🎉 Kafka Local Development Environment is ready!"
echo "================================================="
echo "📊 Kafka UI: http://localhost:8090"
echo "🔌 Kafka Bootstrap Server: localhost:9092"
echo "🐘 Zookeeper: localhost:2181"
echo ""
echo "To start the application with Kafka enabled, run:"
echo "mvn spring-boot:run -Dspring-boot.run.profiles=local-kafka"
echo ""
echo "To stop the services, run:"
echo "docker-compose down"
echo ""
echo "For more information, see: docs/KAFKA_LOCAL_SETUP.md"