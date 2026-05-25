# Montessori Learning

Android app for Montessori preschools. Teachers capture children's daily work (photos + observations) and share instantly with parents via push notifications. Self-hosted, open source, zero monthly fees.

---

## Features

### 📸 Daily Work Capture
Teachers photograph each child's work — drawings, worksheets, practical life activities, art projects — and write a brief observation. Each capture takes ~30 seconds. Over a day, each child accumulates 2–5 entries forming a digital portfolio.

```mermaid
flowchart LR
    T[Teacher opens app] --> D[Dashboard: child grid]
    D --> C[Tap child avatar]
    C --> P[CaptureScreen:<br/>Camera + title<br/>+ area + comment]
    P --> U[Upload photos<br/>to MinIO]
    U --> E[Create work entry]
    E --> N[Push notification<br/>to parent via ntfy]
```

- CameraX integration with live preview
- Title, Montessori area picker (5 areas), teacher comment
- Multiple photos per entry (different angles / progress shots)
- Photo review — delete, reorder before submitting

### 👪 Parent Feed
Parents see a real-time chronological feed of their child's day with photos, area badges, and teacher observations.

```mermaid
flowchart LR
    P[Parent receives<br/>push notification] --> O[Opens app]
    O --> F[Today's Feed]
    F --> D[Entry detail:<br/>full-size photo<br/>+ comment]
    D --> S[Share via system<br/>share sheet]
    F --> A[Archive: past<br/>days by date]
```

- Daily summary card (X entries, Y photos today) with share button
- Archive with date picker for any past day
- Share single entry or daily summary via system share sheet

### 📨 Messaging
Teachers send announcements to individual parents, a classroom, or everyone. Read receipts included.

### 📊 Daily Limit (50 images/child/day)
Server-enforced limit with teacher override:
- `COUNT` query before each upload
- Warning dialog when approaching limit
- "Upload Anyway" with `X-Override-Limit` header

### 🔌 Offline Queue
Entries save to Room DB and sync when network returns via WorkManager.

### 🔔 Self-Hosted Push (ntfy)
No Google Play Services or Firebase required. Works on any Android device including Chinese tablets.

### 🛡️ Zero Monthly Cost
| Component | Cost |
|-----------|------|
| Server | Your hardware |
| Database (PostgreSQL) | $0 |
| File storage (MinIO) | $0 |
| Push notifications (ntfy) | $0 |
| SSL (Caddy + Let's Encrypt) | $0 |
| **Total** | **$10–20/mo (electricity)** |

---

## System Architecture

```mermaid
flowchart TB
    subgraph "Android App"
        A1[Teacher UI<br/>Dashboard, Capture,<br/>Today's Entries]
        A2[Parent UI<br/>Feed, Detail, Archive]
        A3[Admin UI<br/>Users, Classrooms,<br/>Analytics]
    end

    subgraph "Docker Host"
        subgraph "Caddy :8081"
            RP[Reverse Proxy<br/>TLS + Rate Limiting]
        end

        subgraph "Express API :3000"
            API[REST Endpoints<br/>auth, children, entries,<br/>upload, messages]
            WS[Socket.io<br/>Real-time feed updates]
        end

        subgraph "Data Layer"
            PG[PostgreSQL<br/>Users, children,<br/>entries, messages]
            MINIO[MinIO<br/>Photos, thumbnails,<br/>avatars]
        end

        subgraph "Services"
            NTFY[ntfy<br/>Push notifications]
        end
    end

    A1 --> RP
    A2 --> RP
    A3 --> RP
    RP --> API
    RP --> WS
    API --> PG
    API --> MINIO
    API --> NTFY
    WS --> A1
    WS --> A2
```

---

## Auth Flow

```mermaid
sequenceDiagram
    participant App
    participant API
    participant DB as PostgreSQL

    App->>API: POST /api/auth/login {email, password}
    API->>DB: SELECT user WHERE email = ?
    API->>API: bcrypt.compare(password, hash)
    API->>API: jwt.sign({ sub: user.id, role })
    API-->>App: { accessToken (15m), refreshToken (7d), user }

    Note over App,API: All subsequent requests include<br/>Authorization: Bearer <accessToken>

    App->>API: GET /api/children
    API->>API: jwt.verify(accessToken)
    API->>API: Check role for authorization
    API->>DB: SELECT children (scoped by role)
    API-->>App: [child, child, ...]

    Note over App,API: When 401 — refresh token

    App->>API: POST /api/auth/refresh { refreshToken }
    API->>API: jwt.verify(refreshToken, type: 'refresh')
    API-->>App: { accessToken (15m) }
```

---

## Teacher Capture Flow

```mermaid
sequenceDiagram
    participant T as Teacher App
    participant API
    participant M as MinIO
    participant DB as PostgreSQL
    participant N as ntfy
    participant P as Parent App

    T->>API: GET /api/daily-count/{childId}
    API-->>T: { count: 12, max: 50 }

    alt count >= 50
        T->>T: Show warning dialog
        T->>API: X-Override-Limit: true
    end

    T->>API: POST /api/upload (multipart photo)
    API->>API: Sharp compress (1600px, 80% JPEG)
    API->>M: Store /photos/{date}/{child}/{uuid}.jpg
    API->>API: Sharp thumbnail (400px)
    API->>M: Store /thumbnails/{date}/{child}/{uuid}.jpg
    API-->>T: { storageKey, thumbnailKey }

    T->>API: POST /api/work-entries { childId, title, area, comment, media[] }
    API->>DB: INSERT work_entry + media rows
    API->>N: POST /{parent_topic} — "New entry for {child}"
    API->>API: Socket.io emit to parent room
    N-->>P: Push notification
    API-->>T: 201 WorkEntry with media[]
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Android UI** | Kotlin, Jetpack Compose, Material3 |
| **Camera** | CameraX |
| **DI** | Hilt |
| **Local DB** | Room (offline queue + cache) |
| **HTTP** | Retrofit + OkHttp |
| **Real-time** | Socket.io |
| **Backend** | Node.js + Express |
| **Database** | PostgreSQL 16 (Knex.js) |
| **File storage** | MinIO (S3-compatible) |
| **Image processing** | Sharp (resize + compress) |
| **Push** | ntfy (self-hosted, FCM-free) |
| **Auth** | JWT + bcryptjs |
| **Reverse proxy** | Caddy (auto TLS) |
| **Container** | Docker Compose |

---

## Quick Start

### 1. Start backend

```bash
docker compose up -d
```

This starts PostgreSQL, MinIO, ntfy, the Express API, and Caddy.

### 2. Verify

```bash
docker compose ps
```

### 3. Test login

```bash
# Teacher
curl -s http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher@demo.com","password":"password123"}'

# Parent
curl -s http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"parent@demo.com","password":"password123"}'
```

### 4. Build Android app

```bash
export JAVA_HOME=~/jdk17/jdk-17.0.14+7
export PATH=$JAVA_HOME/bin:$PATH
cd app
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | No | Register (email, password, displayName) |
| POST | /api/auth/login | No | Login → JWT |
| POST | /api/auth/refresh | No | Refresh access token |
| GET | /api/classrooms | User | List classrooms |
| GET | /api/classrooms/:id | User | Classroom with children + teachers |
| POST | /api/classrooms | Admin | Create classroom (stub) |
| GET | /api/children | User | List children (role-scoped) |
| POST | /api/children | Teacher | Create child |
| GET | /api/work-entries | User | List entries (role-scoped) |
| POST | /api/work-entries | Teacher | Create entry with media refs |
| DELETE | /api/work-entries/:id | Teacher | Soft delete entry |
| POST | /api/upload | Teacher | Upload photo (multipart) |
| GET | /api/daily-counts | User | Photo counts per child (dashboard) |
| GET | /api/daily-count/:childId | User | Single child's count |
| GET | /api/daily-summary | Parent | Today's feed grouped by child |
| GET | /api/messages | User | List messages |
| POST | /api/messages | Teacher | Send message |
| PUT | /api/messages/:id/read | User | Mark message read |
| GET | /api/admin/analytics | Admin | Analytics (stub) |

---

## Test Accounts

| Role | Email | Password | Display Name |
|------|-------|----------|-------------|
| Teacher | teacher@demo.com | password123 | Maria Montessori |
| Parent | parent@demo.com | password123 | Anna Parent |
| Child | Luca Rossi (Sunshine Casa, DOB 2021-03-15) | — | — |

---

## Documentation

| File | Contents |
|------|----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Data model, deployment, security, costs |
| [PLAN.md](PLAN.md) | Business rules, user stories |
| [TODO.md](TODO.md) | Phased task list |
| [AGENTS.md](AGENTS.md) | Build environment, session continuity |

---

## License

MIT
