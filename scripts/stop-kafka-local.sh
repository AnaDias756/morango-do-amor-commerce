#!/bin/bash

# OrderHub - Stop Kafka Local Development Environment
# This script stops the Kafka infrastructure for local development

set -e

echo "🛑 Stopping Kafka Local Development Environment for OrderHub"
echo "================================================="

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Error: docker-compose.yml not found. Please run this script from the project root."
    exit 1
fi

echo "✅ Found docker-compose.yml"

# Stop Kafka infrastructure
echo "🔄 Stopping Kafka infrastructure..."
docker-compose down

echo ""
echo "✅ Kafka Local Development Environment stopped!"
echo "================================================="
echo "All Kafka services have been stopped and containers removed."
echo ""
echo "To start again, run:"
echo "./scripts/start-kafka-local.sh"
echo ""
echo "To remove all data (topics, messages, etc.), run:"
echo "docker-compose down -v"