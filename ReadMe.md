# 🏆 Live Sports Event Tracker Service

A high-concurrency backend microservice designed to track live sports events. It utilizes a fully containerized architecture to schedule periodic polling jobs, fetch real-time scores, and publish updates to Kafka.

![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.2-green?style=flat-square)
![Kafka](https://img.shields.io/badge/Kafka-KRaft-black?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=flat-square)

---

## 📖 Overview

This application demonstrates a production-grade approach to monitoring concurrent live events. It features:
1.  **Zero-Dependency Runtime:** The entire stack (App + Kafka) runs in Docker. You do not need Java installed to run this project.
2.  **Advanced Scheduling:** Uses a `ScheduledFuture` per event for O(1) latency control.
3.  **Resource Efficiency:** Configured with strict memory limits (`-Xmx256m`) to run reliably on constrained cloud VMs.
4.  **Self-Contained Mocking:** Includes an internal Mock API for generating random scores, making the project fully executable offline.

---

## 🏗 Architecture & Design



### 1. Concurrency Strategy: Dynamic vs. Global
The system supports two scheduling modes via `app.scheduling.mode`. We default to **Dynamic Mode**.
* **Mechanism:** Uses a `ConcurrentHashMap` to manage a unique `ScheduledFuture` for each event.
* **Benefit:** Allows precise start/stop control for specific events without iterating through a global list.
* **Safety:** Implemented **Atomic Locking** (`computeIfAbsent`) to prevent race conditions.

### 2. Resilience & Resource Management
* **Memory Safety:** The Docker container is capped at **256MB RAM** via `JAVA_TOOL_OPTIONS` to prevent Out-Of-Memory (OOM) kills on small VMs.
* **Timeouts:** The `RestTemplate` uses strict **5-second timeouts** to prevent thread starvation if the external provider hangs.

### 3. Observability
* **Distributed Tracing:** Implemented `MdcInterceptor` and manual context propagation.
* **Outcome:** Logs are tagged with `[EventId: match-123]`, even inside background threads.

---

## 🛠 Prerequisites

* **Docker & Docker Compose** (The only strict requirement)
* **Make** (Recommended for automation)
* **curl** & **jq** (Optional, for manual testing)

---

## 🚀 Getting Started

We provide a `Makefile` to automate the entire lifecycle.

### Option A: Using Make (Recommended)

1.  **Start the System:**
    ```bash
    make start
    ```
    *This builds the Docker image, starts Kafka (with memory limits), and waits for the application to be healthy.*

2.  **Watch the Logs:**
    ```bash
    make logs
    ```

3.  **Run an End-to-End Test:**
    ```bash
    make quick-test
    ```
    *This runs a script that starts an event, waits 15s for polling logs, and then stops the event.*

### Option B: Without Make (Windows / Manual)

1.  **Make script executable:**
    ```bash
    chmod +x docker.sh
    ```

2.  **Start the System:**
    ```bash
    ./docker.sh
    ```

3.  **Manual Testing:**
    Use the `curl` commands in the **API Usage** section below.

---

## 🔌 API Usage

### 1. Start Tracking an Event

```bash
curl -X POST http://localhost:8080/events/status \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "match-001",
    "status": "LIVE"
  }'
```

### 2. Stop Tracking

```bash
curl -X POST http://localhost:8080/events/status \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "match-001",
    "status": "NOT_LIVE"
  }'
```

### 3. Mock Scores API (Debug)

Check the random score generator directly:

```bash
curl -X GET http://localhost:8080/mock-api/score/match-001
```

-----

## ⚙️ Configuration

Configuration is managed via `src/main/resources/application.yml` and `docker-compose.yml`.

| Property | Value | Description |
|----------|---------|-------------|
| `app.scheduling.mode` | `dynamic` | Strategy selection (per-event vs batch) |
| `app.scheduling.fixed-rate` | `10000` | Polling interval (10s) |
| `JAVA_TOOL_OPTIONS` | `-Xmx256m` | Hard memory limit for the Java Container |
| `KAFKA_HEAP_OPTS` | `-Xmx512M` | Hard memory limit for the Kafka Broker |

-----

## 📂 Project Structure

```text
home-assignment/
├── src/
│   ├── main/
│   │   ├── java/com/example/home_assignment/
│   │   │   ├── config/
│   │   │   │   ├── MdcInterceptor.java      # Distributed tracing context
│   │   │   │   ├── RestConfig.java          # RestTemplate with timeouts
│   │   │   │   ├── SchedulerConfig.java     # ThreadPool settings
│   │   │   │   └── WebMvcConfig.java        # Interceptor registration
│   │   │   │
│   │   │   ├── controllers/
│   │   │   │   ├── EventController.java     # Main API endpoint
│   │   │   │   ├── HomeController.java      # Basic health check
│   │   │   │   └── MockScoreApiController.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── EventResponse.java       # Standardized API response
│   │   │   │   ├── EventStatus.java         # Enum (LIVE/NOT_LIVE)
│   │   │   │   └── EventStatusUpdate.java   # Request Payload
│   │   │   │
│   │   │   ├── interfaces/
│   │   │   │   └── EventScheduler.java      # Strategy Interface
│   │   │   │
│   │   │   ├── services/
│   │   │   │   ├── schedulers/
│   │   │   │   │   ├── DynamicEventScheduler.java # ConcurrentHashMap Strategy
│   │   │   │   │   └── GlobalEventScheduler.java  # Batch Strategy
│   │   │   │   ├── ScorePollingService.java       # Core Business Logic
│   │   │   │   └── ScoreUpdateProducer.java       # Kafka Producer
│   │   │   │
│   │   │   └── HomeAssignmentApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.yml              # App configuration
│   │
│   └── test/                                # Unit & Integration Tests
│       └── java/com/example/home_assignment/
│           ├── controllers/EventControllerTest.java
│           ├── services/ScorePollingServiceTest.java
│           └── services/schedulers/
│               ├── DynamicEventSchedulerTest.java
│               └── GlobalEventSchedulerTest.java
│
├── docker-compose.yml                       # Kafka Infrastructure (KRaft)
├── Dockerfile                               # Multi-stage Gradle build
├── Makefile                                 # Automation commands
├── docker.sh                                # Smart startup script
├── build.gradle                             # Gradle build configuration
└── settings.gradle                          # Gradle settings
```

-----

## ❓ Troubleshooting

**"Service failed to start within 60 seconds"**

  * Your VM might be slow. Edit `docker.sh` and increase `max_attempts` to 120.

**"Exit Code 137" (OOM)**

  * The memory limits (`-Xmx`) in `docker-compose.yml` might be too tight for your specific VM. Try increasing them slightly (e.g., to 384m) if you have available RAM.

**"Connection Refused"**

  * Kafka takes \~10 seconds to elect a controller. The startup script handles this wait, but if running manually, give it time.
