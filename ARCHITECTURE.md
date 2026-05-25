# Montessori Learning — Architecture Document

## Core Concept

This app is built around one primary activity: **teachers photographing children's physical work and writing observations about it each day**.

Each entry is a **photo + teacher comment**. Over a day, a child accumulates 2-5 entries. Parents see them as a chronological feed. The daily collection becomes a digital portfolio the parent can look back on anytime.

---

## Containers

The entire backend runs as 5 Docker containers orchestrated by `docker compose`. All share a single `internal` Docker network and communicate by service name.

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   postgres   │  │    minio     │  │     api      │  │     ntfy     │  │    caddy     │
│  PostgreSQL  │  │ S3-compatible│  │   Express.js  │  │   Push notif  │  │  Reverse     │
│   Database   │  │   Storage    │  │   (Node.js)   │  │   (self-host) │  │   Proxy      │
│   :5432      │  │   :9000      │  │   :3000       │  │   :80        │  │   :8080      │
└──────┬───────┘  └──────┬───────┘  └──────┬────────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │                  │                  │
       └─────────────────┴─────────────────┴──────────────────┴──────────────────┘
                                        │
                              internal Docker network
                                        │
                              ┌─────────┴─────────┐
                              │   Host machine     │
                              │   Port 3000 (API)  │
                              │   Port 8081 (Caddy)│
                              └───────────────────┘
```

### 1. `postgres` (PostgreSQL 16)

**Image:** `postgres:16-alpine`

**Purpose:** Primary relational database. Stores all app data — users, children, work entries, media metadata, messages, classroom assignments, read receipts. Every API call that reads or writes structured data goes here.

**Why this over alternatives:**
- Mature, battle-tested, excellent JSONB support for flexible fields like notification preferences
- Knex.js migration system gives us version-controlled schema changes
- Alpine variant keeps image size small (~200MB)

**Config passed:**
| Variable | Value | Why |
|----------|-------|-----|
| `POSTGRES_DB` | `montessori` | Database name the API connects to |
| `POSTGRES_USER` | `montessori` | Application database user |
| `POSTGRES_PASSWORD_FILE` | `/run/secrets/db_password` | Password from Docker secret (avoids env var leaks) |
| Volume `./init.sql` → `/docker-entrypoint-initdb.d/` | — | SQL schema auto-executed on first container start |
| Volume `./data/pg` → `/var/lib/postgresql/data` | — | Persists all data across restarts |

**How the API connects:** `DATABASE_URL=postgres://montessori:<password>@postgres/montessori` — the hostname `postgres` resolves via Docker's internal DNS to this container's IP. Knex.js uses this URL to create a connection pool.

---

### 2. `minio` (MinIO — S3-compatible Object Storage)

**Image:** `minio/minio`

**Command:** `server /data --console-address ":9001"`

**Purpose:** Stores all binary files — photos (full resolution), thumbnails (400px), and avatars. Three buckets are created automatically at API startup: `photos`, `thumbnails`, `avatars`.

**Why this over alternatives:**
- S3-compatible API means the same `Minio.Client` SDK works if you ever migrate to AWS S3, DigitalOcean Spaces, or Backblaze B2
- Keeps multi-GB photo storage out of PostgreSQL (which isn't designed for blobs)
- Web console on port 9001 lets you browse files manually
- Lightweight (~100MB image)

**Config passed:**
| Variable | Value | Why |
|----------|-------|-----|
| `MINIO_ROOT_USER` | `minioadmin` | Admin user for bucket management |
| `MINIO_ROOT_PASSWORD_FILE` | `/run/secrets/minio_password` | Password from Docker secret |
| Volume `./data/minio` → `/data` | — | Persists all uploaded photos on disk |

**How the API connects:** `MINIO_ENDPOINT=minio:9000`, `MINIO_ACCESS_KEY=minioadmin`, `MINIO_SECRET_KEY=<password>`. The Express `upload.js` route creates a `Minio.Client` with these credentials and uses it for all `putObject` / `getObject` calls.

**Data flow on upload:**
```
Teacher photo → API receives multipart
    → Sharp.compress (1600px, 80% JPEG quality)
    → minioClient.putObject('photos', key, buffer)
    → Sharp.thumbnail (400px)
    → minioClient.putObject('thumbnails', key, buffer)
    → 200 { storageKey, thumbnailKey }
```

---

### 3. `ntfy` (Self-Hosted Push Notifications)

**Image:** `binwiederhier/ntfy`

**Command:** `serve`

**Purpose:** Sends push notifications to parents' phones when teachers submit work entries or messages. Replaces Firebase Cloud Messaging entirely.

**Why this over alternatives:**
- **No Google Play Services required** — works on any Android device, even Chinese tablets without Google
- **No Firebase project** — zero vendor dependency, zero account setup
- **No per-notification cost** — unlimited push for free
- **Single Docker container** — minimal maintenance
- The ntfy Android app (separate install) handles the actual notification display

**Config passed:**
| Config | Value | Why |
|--------|-------|-----|
| `command` | `serve` | Runs the ntfy HTTP server |
| Volume `./data/ntfy` → `/var/lib/ntfy` | — | Persists subscriber state |
| No ports exposed externally | — | Only reachable on the `internal` Docker network |

**How the API connects:** `NTFY_URL=http://ntfy:80`. The `push.js` service posts `POST /{topic_id}` with a JSON body:
```json
{ "topic": "parent_uuid", "title": "Luca Rossi — Pink Tower",
  "message": "Maya completed the Pink Tower independently...",
  "tags": ["tada"], "priority": 4 }
```
The ntfy Android app (subscribed to the parent's topic UUID) receives this and displays an Android notification.

**Trade-off:** Parents must install the ntfy Android app alongside this app. The app doesn't display notifications itself — ntfy handles that.

---

### 4. `api` (Express.js — Application Server)

**Build:** `./server/Dockerfile`

**Purpose:** Contains all business logic — REST endpoints for auth, entries, uploads, messages, daily summaries; image processing via Sharp; JWT token management; Socket.io real-time events; coordination of push notifications.

**Why this over alternatives:**
- Node.js + Express is lightweight and well-suited for I/O-bound workloads like proxying uploads and database queries
- Same language (JavaScript) is usable elsewhere if needed
- Single process, no complex orchestration

**Config passed:**
| Variable | Value | Why |
|----------|-------|-----|
| `DATABASE_URL` | `postgres://montessori:...@postgres/montessori` | PostgreSQL connection string |
| `MINIO_ENDPOINT` | `minio:9000` | MinIO service address |
| `MINIO_ACCESS_KEY` | `minioadmin` | MinIO auth |
| `MINIO_SECRET_KEY` | `<password>` | MinIO auth |
| `JWT_SECRET` | `<secret>` | Signs and verifies access/refresh tokens |
| `NTFY_URL` | `http://ntfy:80` | ntfy push endpoint |
| `PORT` | `3000` | Internal listen port |
| Port `3000:3000` | — | Exposed to host so Android emulator can reach it at `http://10.0.2.2:3000` (bypasses Caddy TLS) |

**What the API serves (all endpoints):**

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | No | Register (email, password, displayName) |
| POST | /api/auth/login | No | Login → JWT |
| POST | /api/auth/refresh | No | Refresh access token |
| GET | /api/classrooms | User | List classrooms |
| GET | /api/classrooms/:id | User | Classroom with children + teachers |
| POST | /api/classrooms | Admin | Create classroom (stub) |
| PUT | /api/classrooms/:id | Admin | Update classroom (stub) |
| DELETE | /api/classrooms/:id | Admin | Delete classroom (stub) |
| GET | /api/children | User | List children (role-scoped) |
| POST | /api/children | Teacher | Create child |
| GET | /api/work-entries | User | List entries (role-scoped) |
| POST | /api/work-entries | Teacher | Create entry with media refs |
| DELETE | /api/work-entries/:id | Teacher | Soft delete entry |
| POST | /api/upload | Teacher | Upload photo (multipart) |
| GET | /api/upload/:key | User | Serve photo/thumbnail |
| GET | /api/daily-counts | User | Photo counts per child (dashboard) |
| GET | /api/daily-count/:childId | User | Single child's daily count |
| GET | /api/daily-summary | Parent | Today's feed grouped by child |
| GET | /api/messages | User | List messages |
| POST | /api/messages | Teacher | Send message |
| PUT | /api/messages/:id/read | User | Mark message read |
| GET | /api/admin/analytics | Admin | Analytics (stub) |

---

### 5. `caddy` (Reverse Proxy with Auto TLS)

**Image:** `caddy:alpine`

**Purpose:** Single entry point for all HTTP traffic. Handles TLS termination (auto HTTPS via Let's Encrypt), reverse proxying to the API and ntfy, and rate limiting.

**Why this over alternatives:**
- Built-in auto HTTPS with Let's Encrypt — zero config for TLS
- Extremely simple config language (Caddyfile)
- Automatic HTTP/2, OCSP stapling, log rotation
- Much simpler than Nginx for this use case

**Config passed:**
| Config | Value | Why |
|--------|-------|-----|
| Port `8081:8080` | — | Exposed on host (port 80 was taken by another service) |
| Volume `./Caddyfile` → `/etc/caddy/Caddyfile` | — | Routing rules |
| Volume `./data/caddy/data` → `/data` | — | TLS certificates |
| Volume `./data/caddy/config` → `/config` | — | Caddy config state |

**Caddyfile rules:**
```
localhost:8080 {
    reverse_proxy /api/* api:3000       # All API calls
    reverse_proxy /socket.io/* api:3000 # WebSocket upgrade
    reverse_proxy /ntfy/* ntfy:80       # ntfy management
    reverse_proxy /uploads/* api:3000   # Photo serving
}
```

All five containers communicate over the `internal` Docker network using service hostnames. No container can be reached from outside except through Caddy's port 8081 and the API's port 3000 (the latter is exposed for emulator HTTP access only).

---

## Auth Flow

```
1. POST /api/auth/login { email, password }
   → bcrypt.compare(password, hash)
   → jwt.sign({ sub: user.id, role })
   → { accessToken (15min), refreshToken (7d), user }

2. All subsequent requests:
   Authorization: Bearer <accessToken>

3. Middleware decodes JWT, attaches to req:
   req.user = { id, role }

4. Route handlers check req.user.role for authorization

5. When accessToken expires (401):
   POST /api/auth/refresh { refreshToken }
   → jwt.verify(token, type='refresh')
   → { accessToken (15min) }
```

---

## Data Model (PostgreSQL)

### Entity Relationships

```
users (teacher/parent/admin)
  ├── classroom_teachers (M:N with classrooms)
  ├── child_parents (M:N with children)
  ├── work_entries (1:N, as teacher)
  ├── messages (1:N, as sender)
  └── message_recipients (1:N, as recipient)

classrooms
  ├── classroom_teachers (M:N with users)
  ├── children (1:N)
  └── work_entries (1:N)

children
  ├── child_parents (M:N with users)
  └── work_entries (1:N)

work_entries
  └── media (1:N — photos of the work)

messages
  └── message_recipients (1:N — per-user read tracking)
```

### Business Rules

- **Daily upload limit:** Max 50 images per child per day. Checked server-side via `COUNT(*) FROM media JOIN work_entries WHERE child_id = ? AND created_at::date = TODAY`. Teacher can override with `X-Override-Limit: true` header.
- **Soft delete:** Work entries have a `deleted_at` column. They're hidden from queries but not physically removed.
- **Role scoping:** Teachers see only their assigned classrooms (via `classroom_teachers`). Parents see only their children (via `child_parents`). Admins see everything.

---

## Data Flow — Teacher Capture

```
Teacher opens app → sees classroom dashboard with child grid
                           │
                           ▼
               Tap child avatar → CaptureScreen
                           │
                           ▼
               Take photo(s) → type title + area + comment
                           │
                           ▼
               Tap "Submit"
                           │
               ┌───────────┴───────────┐
               │                       │
               ▼                       ▼
     Check daily count        Queue to Room (offline)
     GET /daily-count/{id}    return immediately
               │
               ▼
     If >= 50: show warning
     else: proceed
               │
               ▼
     For each photo:
       POST /api/upload (multipart)
         → Sharp compress (1600px, 80%)
         → MinIO: /photos/{date}/{child}/{uuid}.jpg
         → Sharp thumbnail (400px)
         → MinIO: /thumbnails/{date}/{child}/{uuid}.jpg
         → Response: { storageKey, thumbnailKey }
               │
               ▼
     POST /api/work-entries {
       childId, title, montessoriArea, teacherComment, media[]
     }
         → INSERT work_entry + media rows
         → Socket.io emit to parent's room
         → ntfy push: "Child — Title"
         → Response: 201 WorkEntry
```

---

## Data Flow — Parent Notification

```
ntfy push: "Luca Rossi — Pink Tower"
         │
         ▼
Parent taps notification → app opens to Today's Feed
         │
         ▼
FeedScreen: chronological list of entries
  ├── DailySummaryCard (X entries, Y photos, share + archive)
  ├── EntryCard per activity
  │     ├── Thumbnail, child name, timestamp
  │     ├── Title, Montessori area badge
  │     └── Teacher comment (truncated to 2 lines)
  └── Tap → full-size photo + full comment
         │
         ▼
ArchiveScreen: pick any past date → see that day's entries
         │
         ▼
Share: Intent.ACTION_SEND → system share sheet
       (WhatsApp, email, messaging apps)
```

---

## Android Component Architecture

```
┌────────────────────────────────────────────────────────────┐
│                  Presentation Layer                         │
│  ┌──────────┐ ┌──────────────┐ ┌──────────┐ ┌──────────┐ │
│  │ Auth UI  │ │ Teacher UI   │ │ Parent UI│ │ Admin UI │ │
│  │(Login,   │ │(Dashboard,   │ │(Feed,    │ │(Users,   │ │
│  │ Register)│ │ Capture,     │ │ Detail,  │ │ Classrm, │ │
│  │          │ │ TodayEntries)│ │ Archive) │ │ Analytics│ │
│  └────┬─────┘ └──────┬───────┘ └────┬─────┘ └────┬─────┘ │
│       │              │              │             │       │
│  ┌────┴──────────────┴──────────────┴─────────────┴─────┐ │
│  │              ViewModels (StateFlow)                   │ │
│  │  AuthVM │ TeacherVM │ ParentVM │ MessageVM           │ │
│  └────────────────────────┬─────────────────────────────┘ │
├───────────────────────────┼──────────────────────────────┤
│                      Domain Layer                         │
│  ┌────────────────────────┼─────────────────────────────┐ │
│  │        Repositories (interfaces via Hilt)             │ │
│  └────────────────────────┬─────────────────────────────┘ │
├───────────────────────────┼──────────────────────────────┤
│                       Data Layer                          │
│  ┌────────────────────────┼─────────────────────────────┐ │
│  │ ┌──────────┐ ┌──────────────┐ ┌──────────────────┐  │ │
│  │ │ Retrofit │ │ Socket.io    │ │ Room             │  │ │
│  │ │ (REST)   │ │ (real-time)  │ │ (offline queue)  │  │ │
│  │ └──────────┘ └──────────────┘ └──────────────────┘  │ │
│  │ ┌──────────┐ ┌──────────────┐                       │ │
│  │ │ OkHttp   │ │ WorkManager  │                       │ │
│  │ │ (upload) │ │ (bg sync)    │                       │ │
│  │ └──────────┘ └──────────────┘                       │ │
│  └─────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

---

## Offline Strategy

Entries are saved to Room (local SQLite) immediately when submitted. If upload fails, they stay in a `pending_uploads` table:

```
User submits entry
       │
       ▼
  Room pending_uploads table
  ┌──────┬─────────────────┬──────┐
  │  id  │    payload      │ retry│
  │ uuid │ {entry data}    │  0   │
  └──────┴─────────────────┴──────┘
       │
       ▼
  WorkManager (NetworkType.CONNECTED)
       │
       ▼
  POST /api/work-entries
       │
  ┌────┴────┐
 200        error
  │           │
  ▼           ▼
Delete    Retry with backoff
from      30s → 60s → 2min → 5min
queue
```

---

## Push Notification Flow (ntfy)

```
Teacher submits entry
       │
       ▼
API: resolve parent IDs for this child
       │
       ▼
API: POST http://ntfy:80/{parent_topic_uuid}
    Body: { title, message, tags, priority: 4 }
       │
       ▼
ntfy server: persists + delivers to subscribers
       │
       ├─ Android ntfy app (persistent WebSocket)
       │    → System notification: "Luca Rossi — Pink Tower"
       │
       └─ Socket.io: emit to parent's room for in-app update
```

---

## Security

| Concern | Solution |
|---------|----------|
| **TLS** | Caddy auto HTTPS via Let's Encrypt |
| **Auth** | JWT with 15min access + 7d refresh tokens |
| **Passwords** | bcryptjs, cost factor 12 |
| **Rate limiting** | express-rate-limit, 100 req/min/IP |
| **SQL injection** | Parameterized queries via Knex.js |
| **CORS** | Whitelist only known origins |
| **Network** | Internal Docker network, only Caddy + API ports exposed |

---

## Cost Breakdown

| Item | Cost | Notes |
|------|------|-------|
| Server hardware | $0 | Already owned |
| Electricity | ~$10-20/mo | School pays already |
| Internet | $0 | School pays already |
| SSL | $0 | Let's Encrypt auto-renewing |
| Push notifications | $0 | ntfy self-hosted |
| Email | $0 | SendGrid free tier (100/day) |
| Software | $0 | All open source |
| **Total monthly** | **$10-20** | |

---

## When to Reconsider

This architecture is ideal for 1-3 schools (<500 children, <10k entries/day). Beyond that:

- **Multiple API instances** → add a load balancer + PostgreSQL read replicas
- **Storage growing** → add more disks to MinIO or switch to distributed mode
- **Search across entries** → add Meilisearch
- **Real-time at scale** → replace Socket.io with native WebSockets behind Nginx
- **Server maintenance burden** → at scale, $50-100/mo for managed service may be cheaper than your time
