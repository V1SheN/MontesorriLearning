# Montessori Learning

Android app for Montessori preschools. Teachers photograph learners' daily work, write observations, and share instantly with parents. Push notifications keep families connected. Everything self-hosted, open source, zero monthly fees.

## Quick Start

### 1. Start backend

```bash
docker compose up -d
```

This starts PostgreSQL, MinIO, ntfy (push notifications), the Express API, and Caddy (reverse proxy with TLS).

### 2. Verify the stack is running

```bash
docker compose ps
```

All 5 services should show `Up`.

### 3. Test login

```bash
# Teacher
curl -s -k https://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher@demo.com","password":"password123"}'

# Parent
curl -s -k https://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"parent@demo.com","password":"password123"}'
```

### 4. Build Android app

```bash
# Requires JDK 17
cd app
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

## Documentation

| File | What it covers |
|------|---------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System design, data model, API design, data flows, deployment |
| [PLAN.md](PLAN.md) | Project objective, user flows, business rules |
| [TODO.md](TODO.md) | Phased task list with checkboxes |
| [TESTING.md](TESTING.md) | Setup instructions, API testing, Android build, E2E test flows |

## Repo Structure

```
server/          Express.js API (Node.js, PostgreSQL, MinIO, ntfy, Socket.io)
  src/
    routes/      auth, children, workEntries, upload, messages, classrooms, dailySummary
    services/    image (Sharp), push (ntfy), email
    middleware/  JWT auth
    db/          Knex migrations
app/             Android app (Kotlin, Jetpack Compose, CameraX)
  src/main/java/com/example/montesorrilearning/
    ui/          teacher/, parent/, auth/, messaging/, navigation/, theme/
    data/        remote/ (Retrofit, Socket.io), local/ (Room), repository/
    domain/      models
    di/          Hilt modules
```

## Tech Stack

- **Backend:** Node.js + Express + PostgreSQL + MinIO + ntfy + Socket.io
- **Android:** Kotlin + Jetpack Compose + CameraX + Hilt + Room + Retrofit
- **Infrastructure:** Docker Compose on a single server
- **All open source** — no Firebase, no proprietary services

## Key Features

- **Daily work capture** — photo + comment per entry, teacher takes ~30s per child
- **Parent feed** — real-time push notifications, daily summary card, calendar archive
- **Daily limit** — 50 images/child/day with teacher override
- **Offline queue** — entries saved locally, sync when network returns
- **Messaging** — teacher to parent, class broadcasts
- **Self-hosted** — zero monthly fees, full data ownership

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | No | Register new user |
| POST | /api/auth/login | No | Login, returns JWT |
| POST | /api/auth/refresh | No | Refresh access token |
| GET | /api/classrooms | User | List classrooms |
| GET | /api/children | User | List children |
| POST | /api/work-entries | Teacher | Create work entry |
| GET | /api/work-entries | User | List work entries |
| POST | /api/upload | Teacher | Upload photo |
| POST | /api/messages | Teacher | Send message |
| GET | /api/messages | User | Read messages |
| GET | /api/daily-summary | Parent | Daily feed |

## Test Accounts

- **Teacher:** teacher@demo.com / password123 (Maria Montessori)
- **Parent:** parent@demo.com / password123 (Anna Parent)
- **Child:** Luca Rossi (Sunshine Casa classroom)

## Current Status

All 5 Docker containers running with full API functionality verified. The backend is ready for development/testing at `https://localhost:8081`.

## License

MIT
