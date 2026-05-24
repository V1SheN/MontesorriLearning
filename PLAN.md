# Montessori Learning — Parent-School Communication App

## Objective

Build an Android app for a Montessori preschool that lets teachers **photograph learners' physical work** (drawings, worksheets, practical life activities, art projects) each day, **write a brief observation**, and share it with parents instantly.

**Core goals:**
- Teacher captures **1 photo + 1 comment per entry** (takes ~30 seconds per child).
- Over a day, each child accumulates a **daily portfolio** of 2-5 entries.
- Parents receive **instant push notifications** for each new entry, see a chronological feed, and can look back at any past day.
- Teachers and admin can send **class announcements and individual messages** to parents.
- Communication extends to **WhatsApp** (sharing entries) and **email** (end-of-day digest).
- **Everything self-hosted, open source, zero monthly fees.**

## Current State

- Android project (Kotlin, minSdk 21, targetSdk 34, Jetpack/XML layouts).
- Existing code is a coloring app proof-of-concept (DrawingView, MainActivity) — will be replaced.
- macOS app not yet started.

## Architecture

| Layer | Android |
|-------|---------|
| UI | Jetpack Compose |
| Camera | CameraX |
| Local DB | Room |
| Remote | REST API (Express) |
| Auth | JWT + bcrypt |
| Push | ntfy (self-hosted) |
| Messaging | WhatsApp API + Email (SendGrid) |

Backend options: Firebase (fastest to ship) or custom Node.js/PostgreSQL/S3.

## Key User Flows

1. **Teacher capture:** Open app → tap child avatar → take photo/record video → select Montessori area → add note → submit → parent notified.
2. **Parent view:** Notification opens app → see new entry → scroll daily feed → tap to expand → reply with message → teacher notified.
3. **Messaging:** Teacher → tap message icon → select recipients (individual/class/all) → type → send → delivered via push + WhatsApp + email (configurable).

## Core Data Model

Each work entry captures one piece of the child's work:

```
WorkEntry
  ├── child_id        → which child
  ├── teacher_id      → who observed
  ├── title           → "Pink Tower", "Sandpaper Letters"
  ├── montessori_area → practical_life | sensorial | language | math | cultural
  ├── teacher_comment → "Maya completed the Pink Tower independently..."
  ├── media[]         → 1-5 photos (different angles, progress shots)
  │     ├── storage_key      → MinIO path
  │     ├── thumbnail_key    → resized version
  │     ├── is_cover         → first/thumbnail image
  │     └── caption          → per-photo note (optional)
  └── created_at      → timestamp
```

Supporting models: User (teacher/parent/admin), Child, Classroom, Message.

### Business Rules

- **Daily upload limit:** Max **50 images per child per day**. When the teacher reaches 50, the app shows a warning but allows override (admin configurable). Checked server-side on `POST /api/upload`.
