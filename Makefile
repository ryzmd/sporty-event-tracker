# Live Sports Event Tracker - Makefile
# ==========================================

.PHONY: help build start stop restart logs logs-kafka test clean status quick-test dev-shell

# Check for optional tools
HAS_JQ := $(shell command -v jq 2> /dev/null)

# Default target
help: ## Show this help message
	@echo "🏆 Sporty Project - Automation Control Panel"
	@echo "============================================"
	@echo "Prerequisites: Docker, Java 17+"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "Note: 'make quick-test' requires 'curl'. 'jq' is recommended for pretty output."

# --- Build & Run ---

build: ## Clean and build the project (Gradle + Docker)
	@echo "🔨 Building application (skipping tests for speed)..."
	@./gradlew clean bootJar -x test
	@echo "🐳 Building Docker image..."
	@docker-compose build

start: ## Start all services (App + Kafka) using the smart script
	@echo "🚀 Starting services..."
	@chmod +x docker.sh
	@./docker.sh

stop: ## Stop all services
	@echo "🛑 Stopping services..."
	@docker-compose down

restart: ## Restart all services
	@echo "🔄 Restarting..."
	@make stop
	@make start

# --- Logging & Monitoring ---

status: ## Show container status
	@echo "📊 Service status:"
	@docker-compose ps

logs: ## Tail application logs
	@echo "📋 Tailing App logs..."
	@docker-compose logs -f app

logs-kafka: ## Tail Kafka logs (to see raw messages)
	@echo "📋 Tailing Kafka logs..."
	@docker-compose logs -f kafka

# --- Testing ---

test: ## Run Unit & Integration tests via Gradle
	@echo "🧪 Running JUnit tests..."
	@./gradlew test

quick-test: ## Run a live end-to-end API test (Start -> Wait -> Stop)
	@echo "🔥 Running Quick API Test..."
ifndef HAS_JQ
	@echo "⚠️  'jq' is not installed. Output will be raw JSON."
endif
	@echo "--------------------------------"
	@echo "1️⃣  Triggering 'LIVE' status for match-demo..."
	@curl -s -X POST http://localhost:8080/events/status \
		-H "Content-Type: application/json" \
		-d '{"eventId": "match-demo", "status": "LIVE"}' | jq . || echo ""
	@echo "\n⏳ Waiting 15 seconds for scheduler to fire (Interval is 10s)..."
	@sleep 15
	@echo "2️⃣  Triggering 'NOT_LIVE' to stop tracking..."
	@curl -s -X POST http://localhost:8080/events/status \
		-H "Content-Type: application/json" \
		-d '{"eventId": "match-demo", "status": "NOT_LIVE"}' | jq . || echo ""
	@echo "\n✅ Test sequence complete. Check 'make logs' to see the data flow."

# --- Maintenance ---

clean: ## Deep clean (Gradle artifacts + Docker containers/volumes)
	@echo "🧹 Cleaning up..."
	@./gradlew clean
	@docker-compose down -v --remove-orphans
	@echo "✨ Clean complete."

dev-shell: ## Access the application container shell
	@echo "🐚 Entering container shell..."
	@docker-compose exec app /bin/sh