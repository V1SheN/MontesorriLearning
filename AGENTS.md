# Montessori Learning — Session Continuity

## Project State (as of May 25, 2026)

Full-stack Montessori preschool communication app: Express.js backend + Kotlin/Compose Android app. Self-hosted (PostgreSQL, MinIO, ntfy), zero monthly fees.

## Running Stack

5 Docker containers running via `docker compose`:
- **postgres**: DB with schema via init.sql + knex migration tracking
- **minio**: S3-compatible storage for photos/thumbnails/avatars
- **ntfy**: Push notification server
- **api**: Express.js on port 3000
- **caddy**: Reverse proxy with TLS at **https://localhost:8081**

### Verified Working
```
GET  /api/health              → 200
POST /api/auth/login          → accessToken + refreshToken + user
POST /api/auth/register       → user + tokens
POST /api/auth/refresh        → new accessToken
GET  /api/classrooms          → [classroom]
GET  /api/children            → [child with classroom_name]
POST /api/work-entries        → work entry with media[]
GET  /api/work-entries        → filtered entries
POST /api/messages            → message (needs recipientType)
GET  /api/messages            → messages with sender + is_read
GET  /api/daily-summary       → children with entries[]
GET  /api/daily-counts        → [{childId, childName, date, count, max:50}]
POST /api/classrooms          → 501 stub (admin)
PUT  /api/classrooms/:id      → 501 stub (admin)
DELETE /api/classrooms/:id    → 501 stub (admin)
GET  /api/admin/analytics     → 501 stub (admin)
```

### Test Accounts
- Teacher: `teacher@demo.com` / `password123` (Maria Montessori, Sunshine Casa)
- Parent: `parent@demo.com` / `password123` (Anna Parent, Luca Rossi's parent)
- Child: Luca Rossi (DOB 2021-03-15, Sunshine Casa)

## Build Environment

### JDK
- Downloaded to `~/jdk17/jdk-17.0.14+7` (Temurin)
- Set JAVA_HOME and PATH before any Gradle command:
```bash
export JAVA_HOME=~/jdk17/jdk-17.0.14+7
export PATH=$JAVA_HOME/bin:$PATH
```

### Android SDK
- Location: `~/Android/Sdk` (`/home/fish/Android/Sdk`)
- In local.properties already
- Platforms: android-34, android-36
- Build tools: 33.0.1, 34.0.0, 35.0.0, 36.1.0

### AVD
- Emulator: `Medium_Phone_API_36.1` available
- Start with: `$ANDROID_HOME/emulator/emulator -avd Medium_Phone_API_36.1 -no-snapshot`

### Build APK
```bash
export JAVA_HOME=~/jdk17/jdk-17.0.14+7
export PATH=$JAVA_HOME/bin:$PATH
cd ~/Software/Development/github/MontesorriLearning
GRADLE=$(ls -d ~/.gradle/wrapper/dists/gradle-8.13-bin/*/gradle-8.13)
$GRADLE/bin/gradle :app:assembleDebug --no-daemon
```
APK at: `app/build/outputs/apk/debug/app-debug.apk`

## Emulator

- `Medium_Phone_API_36.1` AVD is running on `emulator-5554`
- APK `app-debug.apk` (19MB) installed on the emulator
- **NEXT SESSION**: Check if emulator is still running with `adb devices`; if not, restart with:
  ```bash
  export ANDROID_HOME=~/Android/Sdk
  export PATH=$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH
  DISPLAY=:0 $ANDROID_HOME/emulator/emulator -avd Medium_Phone_API_36.1 -no-snapshot -no-audio -memory 2048 -gpu off &
  ```

## Known Issues

### Backend
- **MinIO bucket init not logging**: `initBuckets()` runs at startup but doesn't log if buckets already exist (expected, buckets persist in `./data/minio`)
- **Rate limiter validation**: `validate: { xForwardedForHeader: false }` is required behind Caddy proxy
- **db_password in URL**: Password embedded in `DATABASE_URL` (dev only, should use secrets in prod)
- **No email service**: `services/email.js` requires configuration for SMTP

### Android
- **BASE_URL** in `AppModule.kt:28`: set to `http://10.0.2.2:3000/` (emulator host loopback to API directly, bypassing Caddy TLS)
- **API port 3000 exposed** on host in docker-compose so emulator can reach it via HTTP
- **Network security config** allows cleartext to `10.0.2.2` only (per-domain, not global)
- **No macOS/desktop support**: Android-only app
- **WhatsApp sharing**: Not implemented in current code

### Infrastructure
- **Port 80 in use**: `miningcore-webui` occupies host port 80; Caddy uses 8081

## Files Changed This Session

| File | Change |
|------|--------|
| `docker-compose.yml` | ntfy command, caddy port 8081, db password in URL |
| `server/Dockerfile` | bcrypt→bcryptjs |
| `server/package.json` | bcrypt→bcryptjs |
| `server/src/index.js` | trust proxy, validate xForwardedForHeader, uncaughtException handlers |
| `server/src/db/knex.js` | conditional DB_PASSWORD |
| `server/src/routes/auth.js` | require bcrypt→bcryptjs |
| `server/src/routes/workEntries.js` | removed array destructuring from .first() |
| `gradle.properties` | org.gradle.jvmargs (2g heap) |
| `README.md` | Updated with API endpoints, test accounts, current status |
| `app/.../MessageThreadScreen.kt` | Added LazyColumn import |
| `app/.../CaptureScreen.kt` | Moved LocalContext.current out of remember, fixed LocalLifecycleOwner import |
| `app/.../Theme.kt` | Replaced CardDefaults.shape.copy with RoundedCornerShape |
| `app/.../WorkRepository.kt` | Added @ApplicationContext qualifier |
| `server/src/routes/dailyCounts.js` | NEW — GET /api/daily-counts endpoint |
| `server/src/routes/admin.js` | NEW — GET /api/admin/analytics stub |
| `server/src/routes/classrooms.js` | Added POST/PUT/DELETE admin stubs |
| `server/src/index.js` | Registered dailyCounts + admin routes |
| `app/.../ApiService.kt` | Added ChildDailyCount DTO + getDailyCounts() |
| `app/.../WorkRepository.kt` | Added getDailyCounts() |
| `app/.../TeacherViewModel.kt` | Added loadDailyCounts(), wired to dailyCounts |
| `app/.../NavGraph.kt` | Real share intent, admin routes, FeedScreen onShareSummary |
| `app/.../FeedScreen.kt` | Added onShareSummary param + share button on DailySummaryCard |
| `app/.../TodayEntriesScreen.kt` | Grouped entries by childName |
| `app/.../AdminDashboardScreen.kt` | NEW — admin panel stub |
| `app/.../UsersScreen.kt` | NEW — admin users stub |
| `app/.../ClassroomsScreen.kt` | NEW — admin classrooms stub |
| `app/.../AnalyticsScreen.kt` | NEW — admin analytics stub |
| `app/.../CalendarHeatmapScreen.kt` | NEW — calendar heatmap stub |
| `app/.../NotificationSettingsScreen.kt` | NEW — notification prefs stub |
| `app/.../UploadWorker.kt` | NEW — WorkManager worker stub |
| `app/.../network_security_config.xml` | NEW — cleartext to 10.0.2.2 |
| `AndroidManifest.xml` | Added networkSecurityConfig reference |

## Key Decisions
- PostgreSQL + MinIO + ntfy + Express instead of Firebase (open source, zero cost, on-prem)
- Android only (macOS deferred)
- 50 images/child/day limit with teacher override
- bcryptjs instead of bcrypt (native module segfaulted on Alpine's musl libc)
