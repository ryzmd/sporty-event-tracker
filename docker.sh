#!/bin/bash

# Colors for pretty output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🏆 Sporty Project - Docker Startup${NC}"
echo "======================================="

# 1. Check Prerequisites
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed.${NC}"
    exit 1
fi

# 2. Clean old state
echo -e "${BLUE}🧹 Cleaning up old containers...${NC}"
docker-compose down --remove-orphans > /dev/null 2>&1

# 3. Build & Start
echo -e "${BLUE}🚀 Building and starting services...${NC}"
# We use --build to ensure code changes are picked up
docker-compose up -d --build

# 4. Wait for Health
echo -e "${BLUE}⏳ Waiting for application to be healthy...${NC}"
attempt=1
max_attempts=120

while [ $attempt -le $max_attempts ]; do
    # Check if the app is responding on port 8080
    if curl -s http://localhost:8080/ > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Service is UP and READY!${NC}"
        echo ""
        echo "📊 Dashboard:"
        echo "   - API URL:     http://localhost:8080"
        echo "   - Kafka:       localhost:9092"
        echo ""
        echo "📋 Tailing logs (Ctrl+C to exit)..."
        # Follow the logs of the 'app' service defined in docker-compose
        docker-compose logs -f app
        exit 0
    fi
    
    # Simple spinner effect
    echo -ne "."
    sleep 1
    attempt=$((attempt + 1))
done

echo -e "\n${RED}❌ Service failed to start within $max_attempts seconds.${NC}"
echo "Check logs with: docker-compose logs app"
exit 1