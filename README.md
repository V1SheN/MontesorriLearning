# Montessori Learning

Android app for Montessori preschools. Teachers photograph learners' daily work, write observations, and share instantly with parents. Push notifications, WhatsApp sharing, and email digests keep families connected. Everything self-hosted, open source, zero monthly fees.

## Quick Start

```bash
# Start backend (Postgres, MinIO, ntfy, API)
docker compose up -d

# Run database migrations
cd server && npm install && npm run migrate

# Seed test accounts
# teacher@school.com / password (teacher)
# parent@home.com / password (parent)
node -e "require('./src/db/seeds/seed')"  # if available
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
    services/    image (Sharp), push (ntfy), email, whatsapp
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
- **Messaging** — teacher ↔ parent, class broadcasts
- **WhatsApp/Email** — share entries, daily digests

## License

MIT
