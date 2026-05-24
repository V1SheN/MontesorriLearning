# Montessori Learning — Architecture Document

## 0. Core Concept: Daily Work Capture

This app is built around one primary activity: **teachers photographing learners' physical work and writing observations about it each day.**

A teacher's daily flow:

```
Morning / Work Cycle
        │
        ├─ Child completes a drawing / worksheet / practical life activity
        │
        ├─ Teacher opens app → taps child's name
        │
        ├─ Snaps photo of the physical work (paper, material, art project)
        │
        ├─ Types a brief observation / comment
        │   e.g. "Maya completed the Pink Tower independently today.
        │         She concentrated for 15 minutes and carefully graded
        │         each block by size."
        │
        ├─ Optionally adds more photos (multiple angles, progress shots)
        │
        └─ Submits → parent receives notification instantly
```

Each entry is a **photo + teacher comment**. Over a day, a child accumulates 2-5 entries. Parents see them as a chronological feed. The daily collection becomes a digital portfolio the parent can look back on anytime.

## 1. System Overview

```
┌─────────────────────────┐      ┌─────────────────────────┐
│     Android App         │      │    macOS App (Teacher)  │
│  (Kotlin / Jetpack      │      │    (SwiftUI)            │
│   Compose)              │      │                         │
│  Teacher / Parent       │      │    Teacher only         │
└──────────┬──────────────┘      └───────────┬─────────────┘
           │                                  │
           │     HTTPS + WSS                  │
           ▼                                  ▼
┌─────────────────────────────────────────────────────────────┐
│              CADDY (Reverse Proxy)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │ TLS (Let's    │  │ Rate Limiting│  │ WebSocket       │  │
│  │ Encrypt)      │  │              │  │ Upgrade         │  │
│  └──────────────┘  └──────────────┘  └─────────────────┘  │
└─────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                Node.js API Server (Express)                  │
│                                                              │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌─────────────┐│
│  │ REST      │ │ Auth      │ │ Socket.io │ │ Image       ││
│  │ Endpoints │ │ JWT       │ │ Real-time │ │ Processor   ││
│  │           │ │           │ │           │ │ (Sharp)     ││
│  └───────────┘ └───────────┘ └───────────┘ └─────────────┘│
│  ┌───────────┐ ┌───────────┐ ┌──────────────────────────┐ │
│  │ Upload    │ │ SendGrid  │ │ WhatsApp Business API    │ │
│  │ Handler   │ │ / SMTP    │ │ (minimal external dep)   │ │
│  └───────────┘ └───────────┘ └──────────────────────────┘ │
└──────────┬────────────────┬───────────────────────────────┘
           │                │
           ▼                ▼
┌──────────────────┐  ┌──────────────────┐
│   PostgreSQL 16  │  │   MinIO          │
│   (Primary DB)   │  │   (S3-compatible │
│                  │  │    object store) │
│   All app data   │  │                  │
│   (users,        │  │  /work/photos/   │
│    children,     │  │  /work/videos/   │
│    entries,      │  │  /thumbnails/    │
│    messages)     │  │  /avatars/       │
└──────────────────┘  └──────────────────┘
                          │
                          ▼
┌───────────────────────────────────────────────┐
│              Docker Compose                    │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌────────────┐  │
│  │ API  │ │ PG   │ │MinIO │ │ Caddy      │  │
│  │ Server│ │      │ │      │ │            │  │
│  └──────┘ └──────┘ └──────┘ └────────────┘  │
│  All containers on one physical server        │
└───────────────────────────────────────────────┘
```

## 2. Open Source Stack

Every component is open source. Zero proprietary dependencies on the backend.

| Component | Project | License | Role |
|-----------|---------|---------|------|
| **API Server** | Node.js + Express | MIT | REST + WebSocket server |
| **Database** | PostgreSQL 16 | PostgreSQL License | Primary relational database |
| **ORM / Query** | Knex.js | MIT | SQL query builder + migrations |
| **Auth** | JWT (jsonwebtoken) + bcrypt | MIT | Stateless authentication |
| **File Storage** | MinIO | AGPL v3 | S3-compatible object storage |
| **Image Processing** | Sharp | Apache 2.0 | Server-side image resize/compress |
| **Real-time** | Socket.io | MIT | WebSocket real-time feed updates |
| **Reverse Proxy** | Caddy | Apache 2.0 | Auto TLS, reverse proxy, static files |
| **Containerization** | Docker + Compose | Apache 2.0 | All services in one deploy |
| **Push (Android)** | ntfy | Apache 2.0 | Self-hosted push notifications (FCM-free) |
| **OS** | Debian / Ubuntu | GPL v2 | Server operating system |
| **Monitoring** | Prometheus + Grafana | Apache 2.0 | Metrics and dashboards |

### Why not Firebase / Supabase / Appwrite?

- **Firebase:** Proprietary, vendor lock-in, runs on Google Cloud.
- **Supabase:** Open source, but self-hosting Supabase is complex (many microservices, requires Kubernetes for production). If you want PostgreSQL + auto-generated REST API + auth + real-time in one package, Supabase self-hosted is worth evaluating later, but for a single-school app a simpler architecture is easier to maintain.
- **Appwrite:** Open source, but heavier than needed for this scope.

**This stack is intentionally minimal.** Each service does one thing and does it well. A single $40/month VPS or an old school server can run all of it.

## 3. Data Model (PostgreSQL)

### Tables

```sql
-- ============================================================
-- Users & Roles
-- ============================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           TEXT UNIQUE NOT NULL,
    phone           TEXT,
    password_hash   TEXT NOT NULL,
    display_name    TEXT NOT NULL,
    role            TEXT NOT NULL CHECK (role IN ('teacher','parent','admin')),
    avatar_path     TEXT,
    notif_prefs     JSONB NOT NULL DEFAULT '{
        "push": true,
        "email": false,
        "digest": "daily"
    }'::jsonb,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Classrooms
-- ============================================================
CREATE TABLE classrooms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    level       TEXT NOT NULL CHECK (level IN ('toddler','casa','elementary')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Classroom <> Teacher (many-to-many)
-- ============================================================
CREATE TABLE classroom_teachers (
    classroom_id UUID NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE,
    teacher_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (classroom_id, teacher_id)
);

-- ============================================================
-- Children
-- ============================================================
CREATE TABLE children (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          TEXT NOT NULL,
    date_of_birth DATE,
    classroom_id  UUID NOT NULL REFERENCES classrooms(id),
    photo_path    TEXT,
    active        BOOLEAN NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Child <> Parent (many-to-many)
-- ============================================================
CREATE TABLE child_parents (
    child_id  UUID NOT NULL REFERENCES children(id) ON DELETE CASCADE,
    parent_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (child_id, parent_id)
);

-- ============================================================
-- Work Entries (each = one photo + one comment from teacher)
-- ============================================================
CREATE TABLE work_entries (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    child_id          UUID NOT NULL REFERENCES children(id),
    teacher_id        UUID NOT NULL REFERENCES users(id),
    classroom_id      UUID NOT NULL REFERENCES classrooms(id),
    montessori_area   TEXT NOT NULL CHECK (montessori_area IN (
        'practical_life','sensorial','language','math','cultural'
    )),
    title             TEXT NOT NULL DEFAULT '',     -- e.g. "Pink Tower", "Sandpaper Letters"
    teacher_comment   TEXT NOT NULL DEFAULT '',     -- teacher's observation/note on this piece
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Media (photos of the physical work — one entry can have
-- multiple photos showing different angles or progress stages)
-- ============================================================
CREATE TABLE media (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id       UUID NOT NULL REFERENCES work_entries(id) ON DELETE CASCADE,
    media_type     TEXT NOT NULL CHECK (media_type IN ('image','video')),
    storage_key    TEXT NOT NULL,         -- MinIO object key
    thumbnail_key  TEXT,                  -- MinIO thumbnail key
    width          INT,
    height         INT,
    file_size      BIGINT,
    is_cover       BOOLEAN NOT NULL DEFAULT false,  -- first/thumbnail image
    caption        TEXT,                  -- optional per-photo caption
    sort_order     INT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Messages
-- ============================================================
CREATE TABLE messages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id    UUID NOT NULL REFERENCES users(id),
    classroom_id UUID REFERENCES classrooms(id),  -- NULL = individual
    subject      TEXT,
    body         TEXT NOT NULL,
    channels     TEXT[] NOT NULL DEFAULT '{push}',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Message Recipients
-- ============================================================
CREATE TABLE message_recipients (
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    read_at    TIMESTAMPTZ,
    PRIMARY KEY (message_id, user_id)
);

-- ============================================================
-- Daily upload limit tracker (capped at 50 per child per day)
-- The app checks COUNT(*) from media m
-- JOIN work_entries e ON m.entry_id = e.id
-- WHERE e.child_id = ? AND m.created_at::date = CURRENT_DATE
-- If >= 50 → warn teacher, allow override
-- ============================================================
CREATE INDEX idx_media_created ON media (created_at);
CREATE INDEX idx_entries_child_created ON work_entries (child_id, created_at DESC);
CREATE INDEX idx_entries_classroom_date ON work_entries (classroom_id, created_at DESC);
CREATE INDEX idx_media_entry ON media (entry_id);
CREATE INDEX idx_messages_recipient ON message_recipients (user_id, message_id);
```

## 4. Push Notifications — Self-Hosted (ntfy)

Since the user wants to minimize external dependencies, push notifications can use **ntfy** instead of Firebase Cloud Messaging.

### How ntfy works

```
Teacher submits work entry
        │
        ▼
API Server receives POST /api/entries
        │
        ├─ 1. Insert into PostgreSQL
        ├─ 2. Resolve parent user IDs
        └─ 3. POST to ntfy server:
               POST /{topic_parent_id}
               Body: "New work entry for {child}"
                      └── via internal Docker network
        │
        ▼
ntfy server (running in Docker)
        │
        ├─ Android: ntfy app receives via persistent connection
        │            (WebSocket or HTTP long-polling)
        │            → Shows Android notification
        │
        └─ macOS: ntfy does not support macOS directly
                  → Use WebSocket in app (Socket.io)
```

### ntfy setup

```yaml
# docker-compose.yml
services:
  ntfy:
    image: binwiederhier/ntfy
    volumes:
      - ./ntfy/data:/var/lib/ntfy
      - ./ntfy/server.yml:/etc/ntfy/server.yml
    ports:
      - "2586:80"
```

### What this means for the user

- **No Google Play Services required** — works on any Android device, even Chinese tablets without Google
- **No Firebase project** — one less account, zero vendor dependency
- **No per-notification cost** — unlimited push for free
- **Single Docker container** — minimal maintenance

**Trade-off:** The ntfy Android app must be installed alongside your app (it handles the notification display). Your app communicates with ntfy via a background service or by subscribing to the user's ntfy topic.

## 5. API Design

### Endpoints

```
POST   /api/auth/register          # Create account
POST   /api/auth/login             # Returns JWT
POST   /api/auth/refresh           # Refresh token

GET    /api/children               # List children (filtered by role)
POST   /api/children               # Create child (teacher/admin)
GET    /api/children/:id           # Child detail
PUT    /api/children/:id           # Update child

# ---- Work Entry (photo + comment) ----
GET    /api/work-entries           # List entries (filtered by role)
POST   /api/work-entries           # Create entry (teacher)
       # Body: { childId, montessoriArea, title, teacherComment }
GET    /api/work-entries/:id       # Entry detail with all media
DELETE /api/work-entries/:id       # Soft delete (teacher/admin)

# ---- Daily Summary ----
GET    /api/daily-summary?date=2026-05-24&classroomId=xxx
                                   # Returns all entries for that date
                                   # grouped by child, with media + comments
                                   # Parent: shows only own children
                                   # Teacher: shows entire class

# ---- Media Upload ----
POST   /api/upload                 # Upload photo → MinIO
       # Multipart: file, childId, entryId, isCover, caption, sortOrder
       #
       # Server checks: total images for this child today >= 50?
       #   If yes, response: { status: "limit_reached", count: 50, max: 50 }
       #   Client shows warning and retry button with override=true
       #   If override header X-Override-Limit: true, server permits it
       #
       # Returns: { storageKey, thumbnailKey, width, height, fileSize }
       # Headers: X-Daily-Count: 12, X-Daily-Max: 50

GET    /api/upload/:key            # Serve thumbnail (proxied from MinIO)

# ---- Daily Count ----
GET    /api/daily-count/:childId   # Returns { childId, date, count: 12, max: 50 }
                                   # Used by client to check before upload starts

# ---- Messaging ----
GET    /api/messages               # List messages for current user
POST   /api/messages               # Send message (teacher/admin)
PUT    /api/messages/:id/read      # Mark as read

# ---- Classrooms ----
GET    /api/classrooms             # List classrooms
GET    /api/classrooms/:id         # Classroom with children + teachers

WS     /socket.io                  # Real-time feed updates
```

### Auth flow

```
1. POST /api/auth/login { email, password }
   → { accessToken (15min), refreshToken (7d) }

2. All subsequent requests:
   Authorization: Bearer <accessToken>

3. When 401:
   POST /api/auth/refresh { refreshToken }
   → { accessToken (15min) }

4. Middleware decodes JWT, attaches user to req:
   req.user = { id, role, classroomIds }

5. Route handlers check req.user.role for authorization
```

## 6. Component Architecture — Android

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ Auth UI      │  │ Teacher UI   │  │ Parent UI        │  │
│  │ (Login,      │  │ (Dashboard,  │  │ (Feed, Detail,   │  │
│  │  Register)   │  │  Capture,    │  │  Archive)        │  │
│  │              │  │  History)    │  │                  │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                 │                    │            │
│  ┌──────┴─────────────────┴────────────────────┴─────────┐ │
│  │              ViewModels (StateFlow)                    │ │
│  └────────────────────────┬──────────────────────────────┘ │
├───────────────────────────┼───────────────────────────────┤
│                       Domain Layer                         │
│  ┌────────────────────────┼──────────────────────────────┐ │
│  │  Repositories (interfaces)                            │ │
│  │  ┌──────────┐ ┌──────────┐ ┌────────┐ ┌──────────┐  │ │
│  │  │ AuthRepo │ │ WorkRepo │ │ MsgRepo│ │ ChildRepo│  │ │
│  │  └──────────┘ └──────────┘ └────────┘ └──────────┘  │ │
│  └────────────────────────┬──────────────────────────────┘ │
├───────────────────────────┼───────────────────────────────┤
│                        Data Layer                          │
│  ┌────────────────────────┼──────────────────────────────┐ │
│  │  ┌──────────┐  ┌──────────────┐  ┌────────────────┐  │ │
│  │  │ Retrofit │  │ Socket.io    │  │ Room (cache)   │  │ │
│  │  │ (REST)   │  │ (real-time)  │  │ + sync queue   │  │ │
│  │  └──────────┘  └──────────────┘  └────────────────┘  │ │
│  │  ┌──────────┐  ┌──────────────┐                       │ │
│  │  │ OkHttp   │  │ WorkManager  │                       │ │
│  │  │ (upload) │  │ (bg sync)    │                       │ │
│  │  └──────────┘  └──────────────┘                       │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Offline Strategy

Without Firestore's built-in offline, we build a lightweight sync layer:

```
User action → Room (local DB)
                  │
                  ▼
         pending_uploads table
         ┌──────────────────────┐
         │ id | payload | retry │
         │ uuid| {...}   | 0   │
         └──────────────────────┘
                  │
                  ▼
WorkManager periodic worker
(NetworkType.CONNECTED)
                  │
                  ▼
         POST /api/work-entries
                  │
           ┌──────┴──────┐
         200              error
           │                │
           ▼                ▼
     Delete from      Retry later
     pending queue    (backoff: 30s, 60s, 2min, 5min)
```



## 7. Data Flow — Daily Work Capture

### Teacher — Photo + Comment Flow

A teacher captures multiple pieces of work per child during the day. Each capture = one entry (photo + comment).

```
Teacher's day begins → opens app → sees classroom dashboard
                           │
                           ▼
               ┌───────────────────────┐
               │ Today's Class View    │
               │                      │
               │  ┌────┐ ┌────┐ ┌───┐ │
               │  │ Ava │ │ Ben│ │Cat│ │← child avatars
               │  └────┘ └────┘ └───┘ │
               │  ... (tap any child)  │
               └───────────────────────┘
                           │
                           ▼
               ┌───────────────────────┐
               │ Capture Screen        │
               │                      │
               │  [Camera preview]    │← CameraX viewfinder
               │                      │
               │  Title: "Pink Tower" │← free text
               │                      │
               │  Area: [Practical ▼]│← Montessori area picker
               │                      │
               │  My observation:     │
               │  ┌─────────────────┐│
               │  │ "Maya completed ││← teacher's comment
               │  │  the Pink Tower  ││
               │  │  independently.  ││
               │  │  She graded each ││
               │  │  block by size." ││
               │  └─────────────────┘│
               │                      │
               │  [📷 Take Photo]     │← captures photo
               │  [+ Add Another]    │← multiple angles
               │  [✓ Submit]          │
               └───────────────────────┘
                           │
                           ▼
        Repeats for next child or next activity
```

### Behind the Scenes

```
Teacher taps "Submit"
        │
        ▼
┌──────────────────────────────────────────────────────────┐
│ ANDROID CLIENT                                            │
│ 1. Check daily count first:                               │
│    GET /api/daily-count/{childId}                         │
│    → { count: 12, max: 50 }                              │
│                                                           │
│ 2. If count >= 50:                                        │
│    ├─ Show warning dialog:                                │
│    │  "⚠️ Ava has 50 images today. Max is 50.            │
│    │   Do you still want to upload more?"                 │
│    │  [Cancel] [Upload Anyway]                            │
│    └─ If teacher taps "Upload Anyway":                    │
│       → Set X-Override-Limit: true header                │
│                                                           │
│ 3. Save to Room (pending_sync = true)                     │
│ 4. Show in UI as "uploading..." with progress bar         │
│ 5. WorkManager starts (on WiFi or mobile data):           │
│    ├─ For each photo:                                     │
│    │   POST /api/upload (multipart)                       │
│    │   Header: X-Override-Limit: true (if overridden)     │
│    │   → Server: Sharp compress (1600px, 80% JPEG)       │
│    │   → MinIO: /photos/{date}/{childId}/{uuid}.jpg      │
│    │   → Server: Sharp thumbnail (400px)                  │
│    │   → MinIO: /thumbnails/{date}/{childId}/{uuid}.jpg  │
│    │   → Response: { storageKey, thumbnailKey }           │
│    │     Headers: X-Daily-Count: 13, X-Daily-Max: 50     │
│    │                                                      │
│    └─ POST /api/work-entries {                            │
│         childId: uuid,                                    │
│         title: "Pink Tower",                              │
│         montessoriArea: "sensorial",                      │
│         teacherComment: "...",                            │
│         media: [ { storageKey, thumbnailKey, isCover } ] │
│       }                                                   │
│ 6. On 201: mark Room entry as synced, show green check   │
│    On error: retry (30s → 60s → 2min → 5min)             │
└──────────────────────────────┬────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────┐
│ SERVER (Express)                                          │
│ 1. POST /upload:                                          │
│    ├─ SELECT COUNT(*) FROM media m                        │
│    │   JOIN work_entries e ON m.entry_id = e.id          │
│    │   WHERE e.child_id = ? AND m.created_at::date = TODAY│
│    ├─ If count >= 50 AND no override header:              │
│    │   → return 429 { status: "limit_reached",           │
│    │       count: 50, max: 50 }                          │
│    ├─ Else: Sharp resize + compress → MinIO              │
│    └─ Response headers: X-Daily-Count, X-Daily-Max       │
│                                                           │
│ 2. POST /work-entries → INSERT work_entries + media      │
│ 3. Socket.io: emit to parent's topic:                    │
│    io.to(`parent:${parentId}`).emit('new_entry', {       │
│      childName, title, thumbnailUrl, teacherComment       │
│    })                                                     │
│ 4. Push notification (ntfy):                              │
│    "Ava — Pink Tower"                                     │
│ 5. If email digest enabled: queue for tonight's digest   │
└──────────────────────────────┬────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────┐
│ PARENT (receives notification)                            │
│ 1. ntfy shows: "{child} — {title}" with thumbnail        │
│ 2. Taps notification → app opens to today's feed         │
│ 3. Sees the new entry card:                              │
│    ┌──────────────────────────────────┐                  │
│    │  Ava             9:45 AM  ⋮      │                  │
│    │                                  │                  │
│    │  ┌──────────────────────────┐   │                  │
│    │  │   [photo of Pink Tower]  │   │                  │
│    │  └──────────────────────────┘   │                  │
│    │                                  │                  │
│    │  **Pink Tower**                  │                  │
│    │  Sensorial                       │                  │
│    │                                  │                  │
│    │  "Maya completed the Pink       │                  │
│    │   Tower independently today.    │                  │
│    │   She concentrated for 15       │                  │
│    │   minutes and carefully graded  │                  │
│    │   each block by size."          │                  │
│    │                                  │                  │
│    │  [👁️ View full size]            │                  │
│    └──────────────────────────────────┘                  │
│                                                           │
│ 4. Parent can scroll through all of today's entries       │
│ 5. Tap any to see full-size photo                         │
│ 6. Could happen 3-5 times per day per child               │
└──────────────────────────────────────────────────────────┘
```

### Daily Summary End of Day

```
At end of day (configurable time, e.g. 6 PM), a daily
summary is generated for each child:

┌──────────────────────────────────────────────────────────┐
│ Today's Work — Ava                                       │
│ May 24, 2026 · Casa Class                                │
│                                                           │
│ ┌──────┐ ┌──────┐ ┌──────┐                               │
│ │Pink  │ │Sand  │ │Water │                               │
│ │Tower │ │Paper │ │ing   │                               │
│ │      │ │Letters│ │Plants│                               │
│ └──────┘ └──────┘ └──────┘                               │
│                                                           │
│ 3 activities · 5 photos · 3 comments                     │
│                                                           │
│ [📧 Email to me] [💬 Share on WhatsApp]                  │
└──────────────────────────────────────────────────────────┘

Delivery via:
  - Push notification (ntfy): "Ava's day — 3 new entries"
  - Email digest (if opted in): daily email with all photos
  - WhatsApp digest (if opted in): summary with link
```

## 8. Data Flow — Messaging

```
Teacher composes message
    ├─ Selects recipients: individual / class / all
    ├─ Writes body
    ├─ Checks channels: push✓, email✓
    └─ Taps "Send"
          │
          ▼
┌─────────────────────────────────────────────────────┐
│ POST /api/messages                                   │
│ Server:                                               │
│ 1. INSERT into messages + message_recipients        │
│ 2. For each recipient:                               │
│    ├─ push: ntfy publish to recipient's topic       │
│    └─ email: SendGrid / SMTP                         │
│ 3. Socket.io: emit to online recipients              │
└─────────────────────────────────────────────────────┘
```

## 9. Deployment

### Single-Server Docker Compose

```yaml
services:
  postgres:
    image: postgres:16-alpine
    volumes:
      - ./data/pg:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    environment:
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password
    secrets:
      - db_password
    restart: always

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    volumes:
      - ./data/minio:/data
    restart: always

  api:
    build: ./server
    depends_on:
      - postgres
      - minio
    environment:
      DATABASE_URL: postgres://montessori@postgres/montessori
      MINIO_ENDPOINT: minio:9000
      JWT_SECRET_FILE: /run/secrets/jwt_secret
    secrets:
      - jwt_secret
    restart: always

  ntfy:
    image: binwiederhier/ntfy
    volumes:
      - ./data/ntfy:/var/lib/ntfy
    restart: always

  caddy:
    image: caddy:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile
      - ./data/caddy/data:/data
      - ./data/caddy/config:/config
    depends_on:
      - api
      - ntfy
    restart: always

secrets:
  db_password:
    file: ./secrets/db_password.txt
  jwt_secret:
    file: ./secrets/jwt_secret.txt
```

### Caddyfile

```
school.example.com {
    reverse_proxy /api/* api:3000
    reverse_proxy /socket.io/* api:3000
    reverse_proxy /ntfy/* ntfy:80
}
```

### Backup Strategy

```bash
#!/bin/bash
# Daily cron job
pg_dump -h localhost -U montessori montessori > /backups/db/$(date +%Y%m%d).sql
mc mirror /data/minio /backups/minio/$(date +%Y%m%d)
# Keep 30 days, sync to external drive weekly
```

## 10. Security

| Concern | Solution | Open Source Tool |
|---------|----------|-----------------|
| **TLS** | Auto HTTPS via Let's Encrypt | Caddy (built-in) |
| **Auth** | JWT with 15min access + 7d refresh tokens | jsonwebtoken |
| **Password storage** | bcrypt, cost factor 12 | bcrypt npm package |
| **Rate limiting** | Per-IP, 100 req/min | express-rate-limit |
| **SQL injection** | Parameterized queries via Knex.js | Knex.js |
| **CORS** | Whitelist only app origins | cors npm package |
| **Secrets** | Docker secrets, not in env or code | Docker Compose |
| **Firewall** | Only ports 80/443 open | ufw / iptables |
| **Backups** | Encrypted, rotated, off-site | pg_dump + rsync |
| **Updates** | `unattended-upgrades` for OS | apt |

## 11. Cost Breakdown

| Item | Cost | Notes |
|------|------|-------|
| **Physical server** | $0 | Already owned |
| **Electricity** | ~$10-20/mo | School pays already |
| **Internet** | $0 | School pays already |
| **SSL certificate** | $0 | Let's Encrypt (auto-renewing) |
| **Push notifications** | $0 | ntfy self-hosted, no per-notification cost |
| **WhatsApp API** | $0 | Meta charges per conversation (~$0.005 each) at volume |
| **Email (SendGrid)** | $0 | Free tier: 100 emails/day (enough for single school) |
| **OR self-hosted email** | $0 | Postfix + Dovecot on same server (not recommended for deliverability) |
| **Software** | $0 | All open source |
| **Labor (you)** | $0 | DIY |
| **Total monthly** | **$10-20** | |

## 12. When to Reconsider

This architecture is ideal for 1-3 schools (<500 children, <10k entries/day). If you scale beyond that, consider:

- **Multiple API instances** → add a load balancer (HAProxy / Traefik) + read replicas for PostgreSQL
- **Storage growing** → add more disks to MinIO or switch to distributed MinIO
- **Search across entries** → add Meilisearch (open source, MIT)
- **Real-time at scale** → replace Socket.io with native WebSockets or SSE behind Nginx
- **Server maintenance burden** → at that scale, paying $50-100/mo for a managed service may become cheaper than your time

## 13. Folder Structure

```
MontesorriLearning/
├── server/
│   ├── src/
│   │   ├── index.js           # Express app entry
│   │   ├── db/
│   │   │   ├── knex.js        # Knex client
│   │   │   └── migrations/    # Database migrations
│   │   ├── middleware/
│   │   │   ├── auth.js        # JWT verification
│   │   │   └── rateLimit.js
│   │   ├── routes/
│   │   │   ├── auth.js
│   │   │   ├── children.js
│   │   │   ├── workEntries.js
│   │   │   ├── messages.js
│   │   │   ├── classrooms.js
│   │   │   └── upload.js
│   │   ├── services/
│   │   │   ├── push.js        # ntfy client
│   │   │   ├── email.js       # SendGrid / SMTP
│   │   │   ├── image.js       # Sharp processing
│   │   │   └── whatsapp.js    # WhatsApp API
│   │   └── websocket.js       # Socket.io setup
│   ├── Dockerfile
│   └── package.json
├── android/
│   ├── app/src/main/java/com/example/montesorrilearning/
│   │   ├── di/                # Manual DI / Hilt
│   │   ├── data/
│   │   │   ├── local/         # Room DAOs, entities
│   │   │   ├── remote/        # Retrofit, Socket.io
│   │   │   └── repository/    # Impl
│   │   ├── domain/
│   │   │   ├── model/         # Domain models
│   │   │   └── repository/    # Interfaces
│   │   ├── ui/
│   │   │   ├── auth/
│   │   │   ├── teacher/
│   │   │   ├── parent/
│   │   │   ├── messaging/
│   │   │   ├── common/
│   │   │   └── navigation/
│   │   └── sync/              # WorkManager + sync queue
│   └── app/src/main/res/
├── docker-compose.yml
├── Caddyfile
├── init.sql                   # Schema for first run
├── PLAN.md
├── TODO.md
└── ARCHITECTURE.md
```
