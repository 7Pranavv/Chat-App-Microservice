# 💬 Chat App — Microservices Architecture

A real-time chat application converted from a **Spring Boot monolith** into **4 independent microservices**, with a React frontend and MongoDB database.

---

## 📁 Project Structure

```
microservices/
├── api-gateway/          # Spring Cloud Gateway — port 8080
├── room-service/         # Room management — port 8081
├── message-service/      # Message persistence — port 8082
├── chat-service/         # WebSocket/STOMP real-time chat — port 8083
├── front-chat/           # React + Vite frontend — port 5173
├── docker-compose.yml    # Runs everything with one command
└── README.md
```

---

## 🏗️ Architecture

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
         └────────┬────────┘
                  ▼
        ┌──────────────────┐
        │     MongoDB      │
        │   port 27017     │
        │  collections:    │
        │  - rooms         │
        │  - messages      │
        └──────────────────┘
```

---

## 🔧 Services

| Service | Port | Tech | Responsibility |
|---------|------|------|---------------|
| **API Gateway** | 8080 | Spring Cloud Gateway | Single entry point, routing, CORS |
| **Room Service** | 8081 | Spring Boot + MongoDB | Create & join chat rooms |
| **Message Service** | 8082 | Spring Boot + MongoDB | Store & retrieve message history |
| **Chat Service** | 8083 | Spring Boot + WebSocket | Real-time STOMP messaging |
| **Frontend** | 5173 | React + Vite | User interface |
| **MongoDB** | 27017 | MongoDB 7 | Database |

---

## 🚀 Running the App

### Option 1 — Docker Compose (Recommended)

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

```bash
# Step 1 — Start all backend services
cd microservices
docker compose up --build

# Step 2 — Start the frontend (new terminal)
cd microservices/front-chat
npm install
npm run dev
```

Open **http://localhost:5173** in your browser.

```bash
# Stop all services
docker compose down

# Stop and wipe database
docker compose down -v
```

> **Note:** First build takes 5–10 minutes. Subsequent runs with `docker compose up` (no `--build`) are fast.

---

### Option 2 — Run Manually (No Docker)

**Prerequisites:** Java 21, Maven 3.9+, MongoDB, Node 20+

```bash
# Start MongoDB
mongod

# Terminal 1 — Room Service
cd room-service
./mvnw spring-boot:run

# Terminal 2 — Message Service
cd message-service
./mvnw spring-boot:run

# Terminal 3 — Chat Service
cd chat-service
./mvnw spring-boot:run

# Terminal 4 — API Gateway (start last)
cd api-gateway
./mvnw spring-boot:run

# Terminal 5 — Frontend
cd front-chat
npm install
npm run dev
```

---

## 📡 API Reference

### Room Service (via Gateway)

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/rooms` | `{"roomId": "my-room"}` | Create a new room |
| `GET` | `/api/v1/rooms/{roomId}` | — | Join / get a room |
| `GET` | `/api/v1/rooms/{roomId}/exists` | — | Check if room exists (internal) |

### Message Service (via Gateway)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/messages/{roomId}?page=0&size=20` | Get paginated message history |
| `POST` | `/api/v1/messages` | Save a message (called by Chat Service) |

### Chat Service — WebSocket (via Gateway)

| Direction | Destination | Description |
|-----------|-------------|-------------|
| Connect | `ws://localhost:8080/chat` (SockJS) | Establish WebSocket connection |
| Client → Server | `/app/sendMessage/{roomId}` | Send a message to a room |
| Server → Client | `/topic/room/{roomId}` | Subscribe to receive room messages |

---

## 💬 Message Flow

```
User types a message
        │
        ▼
Frontend (React)
  1. Sends STOMP message → /app/sendMessage/{roomId}
        │
        ▼
API Gateway → Chat Service (8083)
  2. Validates room exists  → calls Room Service GET /exists
  3. Builds ChatMessage with timestamp
  4. Broadcasts to /topic/room/{roomId}  ← all users in room receive it
  5. Async saves to Message Service POST /api/v1/messages (fire-and-forget)
        │
        ▼
Message Service (8082) → MongoDB
  6. Persists message to messages collection
```

---

## 🔄 Monolith vs Microservices

| Concern | Monolith | Microservices |
|---------|----------|---------------|
| Entry point | Single app on port 8080 | API Gateway routes to 4 services |
| Message storage | Embedded list inside Room document | Own `messages` collection with `roomId` index |
| Room validation | Direct repository call | HTTP call to Room Service |
| Message persistence | Synchronous in WebSocket handler | Async fire-and-forget to Message Service |
| CORS | Per-controller annotation | Centralised in Gateway |
| Scalability | Must scale entire app | Scale each service independently |
| Fault isolation | One bug can crash everything | Services fail independently |

---

## 🩺 Health Checks

```bash
curl http://localhost:8080/actuator/health   # API Gateway
curl http://localhost:8081/actuator/health   # Room Service
curl http://localhost:8082/actuator/health   # Message Service
curl http://localhost:8083/actuator/health   # Chat Service
```

Each should return `{"status":"UP"}`.

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Vite, Tailwind CSS, SockJS, STOMP.js, Axios |
| Backend | Java 21, Spring Boot 3.4, Spring Cloud Gateway |
| Database | MongoDB 7 |
| Real-time | WebSocket, STOMP protocol, SockJS |
| Inter-service | Spring WebFlux WebClient (non-blocking HTTP) |
| Containerization | Docker, Docker Compose |
| Build tool | Maven 3.9 |
