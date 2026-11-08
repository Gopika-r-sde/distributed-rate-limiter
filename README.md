# Distributed Rate Limiter

A distributed rate limiting service built with Spring Boot, Redis and MySQL. It enforces per-client request limits consistently across multiple application instances, using atomic Redis Lua scripts so concurrent requests cannot slip past a limit.

> **Status:** Core features complete · Cloud deployment in progress

<!-- Add a dashboard screenshot at docs/dashboard.png, then uncomment the next line -->
<!-- ![Dashboard](docs/dashboard.png) -->

## Features

- **Three algorithms behind one interface** (Strategy pattern): Fixed Window, Token Bucket and Sliding Window Log
- **Atomic checks:** each algorithm runs as a Lua script inside Redis, so there are no race conditions, even with several app instances
- **Per-client rules** (limit, window, algorithm) stored in MySQL and changeable at runtime through the API
- **Request log:** every decision (allowed or denied) is saved for the dashboard and for analysis
- **Clean errors:** denied requests get HTTP 429 with a JSON body
- **React dashboard:** register clients, switch algorithms, send test traffic and watch a live allowed/denied chart
- **Tests:** JUnit 5 tests for each algorithm, run against a real Redis
## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot 4, Spring Web, Spring Data JPA |
| Rate-limit state | Redis (Lettuce client) with Lua scripts |
| Persistence | MySQL 8 |
| Frontend | React (Vite) |
| Testing | JUnit 5 |

## How it works

Other services call this one before doing their work. If the answer is 429, they reject the request.

```
Client -> POST /api/ratelimit/check
       -> RateLimitController
       -> load the client's rules from MySQL
       -> RateLimiterFactory picks the strategy from the client's algorithmType
       -> the strategy runs its Lua script in Redis (atomic)
       -> the decision is saved to the request log (MySQL)
       -> 200 allowed  |  429 Too Many Requests
```

## Algorithms

| Algorithm | How it counts | Redis structure | Strength | Trade-off |
|---|---|---|---|---|
| Fixed Window | One counter per time window, expires automatically | String with TTL | Simplest and cheapest | Boundary burst: up to 2x the limit across a window edge |
| Token Bucket | Tokens refill gradually, each request costs one | Hash (tokens, last refill time) | No reset boundary, allows controlled bursts up to capacity | Refill is calculated in whole seconds |
| Sliding Window Log | Stores the timestamp of every allowed request | Sorted set | Most accurate rolling window | Highest memory: one entry per allowed request |

For Token Bucket, the capacity is `maxRequests` and the refill rate is `maxRequests / windowSizeInSeconds`.

## Getting started

### Prerequisites

- JDK (the version set as `java.version` in `pom.xml`)
- Node.js (current LTS)
- MySQL 8
- Redis
  The Maven wrapper is included, so you don't need to install Maven.

### 1. Clone the repo

```bash
git clone https://github.com/Gopika-r-sde/distributed-rate-limiter.git
cd distributed-rate-limiter
```

### 2. Create the database and user

```sql
CREATE DATABASE ratelimiter_db;
CREATE USER 'ratelimiter_user'@'localhost' IDENTIFIED BY 'choose_a_password';
GRANT ALL PRIVILEGES ON ratelimiter_db.* TO 'ratelimiter_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure the backend

Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties` and fill in your MySQL credentials. The real file is gitignored so secrets never reach GitHub.

### 4. Start Redis

```bash
redis-server
```

On Windows, run Redis inside WSL (`sudo service redis-server start`) and check it with `redis-cli ping`, which should reply `PONG`.

### 5. Run the backend

```bash
./mvnw spring-boot:run
```

On Windows use `.\mvnw.cmd spring-boot:run`. Hibernate creates the tables on the first run. The API listens on `http://localhost:9090`.

### 6. Run the dashboard

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The dashboard calls the API at `http://localhost:9090/api` (set in `frontend/src/api/rateLimiterApi.js`). The backend's CORS rules allow `localhost:5173` and `localhost:3000`.

## API reference

| Method | Endpoint | Purpose | Success | Errors |
|---|---|---|---|---|
| POST | `/api/clients` | Register a client | 201 | 400 invalid input, 409 duplicate |
| GET | `/api/clients` | List clients | 200 | |
| PUT | `/api/clients/{clientId}` | Update limit, window or algorithm | 200 | 400 invalid input, 404 unknown client |
| GET | `/api/clients/{clientId}/logs` | Request history for a client | 200 | |
| POST | `/api/ratelimit/check` | Check a request against the client's limit | 200 allowed | 404 unknown client, 429 denied |

`algorithmType` must be one of `FIXED_WINDOW`, `TOKEN_BUCKET` or `SLIDING_WINDOW_LOG`.

**Register a client**

```http
POST /api/clients
Content-Type: application/json
 
{
  "clientId": "client_101",
  "maxRequests": 3,
  "windowSizeInSeconds": 60,
  "algorithmType": "FIXED_WINDOW"
}
```

**Check a request**

```http
POST /api/ratelimit/check
Content-Type: application/json
 
{ "clientId": "client_101" }
```

Allowed (200):

```json
{ "allowed": true, "remainingRequests": -1, "message": "Request allowed" }
```

Denied (429):

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded for client: client_101"
}
```

`remainingRequests` is `-1` for now because the algorithms only return allowed or denied.

## Running the tests

```bash
./mvnw test
```

The algorithm tests run against a real local Redis, so start Redis first. Maven's default `contextLoads` test starts the whole app, so it also needs MySQL running and your `application.properties`.

## Project structure

```
src/main/java/com/gopika/ratelimiter/
├── config/          Redis and CORS configuration
├── controller/      REST endpoints
├── exception/       Custom exception and global 429 handler
├── model/
│   ├── dto/         Request and response objects
│   └── entity/      JPA entities (ClientConfig, RequestLog)
├── redis/           RedisScriptExecutor (runs Lua scripts)
├── repository/      Spring Data JPA repositories
└── service/         RateLimiterStrategy, RateLimiterFactory, impl/ (the 3 algorithms)
src/main/resources/scripts/   Lua scripts, one per algorithm
src/test/                     JUnit tests
frontend/                     React dashboard (Vite)
```

## Design decisions

- **Lua scripts for atomicity:** the read, check and write steps run as one operation inside Redis. A Java lock only protects a single instance, while Redis is shared by every instance.
- **Strategy and Factory patterns:** a new algorithm is a new class with its own bean name. No existing code changes.
- **DTOs separate from entities:** clients cannot set internal fields such as the database id.
- **Separate Redis key prefix per algorithm:** switching a client's algorithm never mixes old and new state.
- **`Instant` timestamps (UTC):** window calculations stay correct regardless of timezone.
- **Secrets stay out of Git:** `application.properties` is gitignored and a `.example` template is committed.
## Known limitations and roadmap

- No authentication yet on the client and check endpoints, which is needed before a public deployment.
- `remainingRequests` is not tracked yet.
- Sliding Window Log uses the timestamp as the sorted-set member, so two requests in the exact same microsecond would count once.
- The dashboard polls every 2 seconds instead of using WebSockets.
- Planned: load testing to compare the algorithms on accuracy, latency and memory use.
- Planned: cloud deployment.
## Author

Built by [Gopika](https://www.linkedin.com/in/gopika-r-s1705/).