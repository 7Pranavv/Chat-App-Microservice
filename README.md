# Chat App — Microservices Architecture

Converted from a **Spring Boot monolith** into **4 independent microservices**.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                          Frontend                               │
│                   React + Vite  (port 5173)                     │
│    REST calls ──────────────────┐                               │
│    WebSocket ───────────────────┤                               │
└────────────────────────────────┼────────────────────────────────┘
                                 │ All traffic
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                              │
│              Spring Cloud Gateway  (port 8080)                  │
│                                                                 │
│  /api/v1/rooms/**     ──────────► Room Service    :8081         │
│  /api/v1/messages/**  ──────────► Message Service :8082         │
│  /chat/**  (WebSocket)──────────► Chat Service    :8083         │
└─────────────────────────────────────────────────────────────────┘
          │                │                  │
          ▼                ▼                  ▼
  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐
  │Room Service │  │Msg Service   │  │  Chat Service    │
  │  port 8081  │  │  port 8082   │  │   port 8083      │
  │             │  │              │  │  WebSocket/STOMP │
  │ POST /rooms │  │ POST /messages│  │ /app/sendMessage │
  │ GET  /rooms │  │ GET  /messages│  │ /topic/room/{id} │
  │ GET  /exists│  │              │  │                  │
  └──────┬──────┘  └──────┬───────┘  └───────┬──────────┘
         │                │                  │    │
         │                │         calls ───┘    │
         │                │◄── save message ───────┘
         │                │
         └────────┬────────┘
                  ▼
        ┌──────────────────┐
        │     MongoDB      │
        │   port 27017     │
        │                  │
        │  collections:    │
        │  - rooms         │
        │  - messages      │
        └──────────────────┘
```

---

## Services

| Service | Port | Responsibility |
|---------|------|---------------|
| **API Gateway** | 8080 | Single entry point. Routes traffic, handles CORS. |
| **Room Service** | 8081 | Create rooms, join rooms, existence check. |
| **Message Service** | 8082 | Persist and retrieve paginated message history. |
| **Chat Service** | 8083 | WebSocket/STOMP real-time broadcasting. |

---

## Key Architectural Changes vs Monolith

| Concern | Monolith | Microservices |
|---------|----------|---------------|
| Entry point | Single Spring Boot app (port 8080) | API Gateway routes to 4 services |
| Message storage | Embedded `List<Message>` inside `Room` document | Independent `messages` collection with `roomId` index |
| Room validation | Direct `RoomRepository` call | Chat Service calls Room Service via HTTP |
| Message persistence | Synchronous inside WebSocket handler | Async fire-and-forget from Chat Service to Message Service |
| CORS | Configured per controller | Centralised in Gateway `globalcors` |
| Scalability | Scale entire app | Scale services independently |

---

## Running Locally (without Docker)

### Prerequisites
- Java 21
- Maven 3.9+
- MongoDB running on `localhost:27017`
- Node 20+ (for frontend)

### Start order
```bash
# 1. Room Service
cd room-service && ./mvnw spring-boot:run

# 2. Message Service
cd message-service && ./mvnw spring-boot:run

# 3. Chat Service
cd chat-service && ./mvnw spring-boot:run

# 4. API Gateway (last — depends on the other three)
cd api-gateway && ./mvnw spring-boot:run

# 5. Frontend
cd front-chat && npm install && npm run dev
```

---

## Running with Docker Compose

```bash
# Build & start everything (from microservices/ root)
docker compose up --build

# Stop
docker compose down

# Stop + remove volumes (wipes MongoDB data)
docker compose down -v
```

---

## API Reference

### Room Service  (via Gateway at :8080)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/rooms` | Create a room `{ "roomId": "my-room" }` |
| `GET`  | `/api/v1/rooms/{roomId}` | Get / join a room |
| `GET`  | `/api/v1/rooms/{roomId}/exists` | Internal existence check |

### Message Service  (via Gateway at :8080)

| Method | Path | Description |
|--------|------|-------------|
| `GET`  | `/api/v1/messages/{roomId}?page=0&size=20` | Paginated message history |
| `POST` | `/api/v1/messages` | Save a message (called internally by Chat Service) |

### Chat Service  (WebSocket via Gateway at :8080)

| Direction | Destination | Description |
|-----------|-------------|-------------|
| Client → Server | `/app/sendMessage/{roomId}` | Send a message |
| Server → Client | `/topic/room/{roomId}` | Subscribe to room messages |
| Connect | `ws://localhost:8080/chat` (SockJS) | Establish connection |

---

## Message Flow (Real-time)

```
Frontend
  │  1. STOMP connect to ws://localhost:8080/chat
  │  2. Subscribe to /topic/room/{roomId}
  │  3. Send message to /app/sendMessage/{roomId}
  ▼
API Gateway  ──► Chat Service (8083)
                    │  4. Call Room Service: GET /api/v1/rooms/{roomId}/exists
                    │  5. Build ChatMessage with timestamp
                    │  6. Broadcast to /topic/room/{roomId}  ◄── All subscribers get it
                    │  7. Async POST /api/v1/messages (fire-and-forget)
                    ▼
               Message Service (8082)  →  MongoDB
```
