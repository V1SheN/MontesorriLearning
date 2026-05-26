# Montessori Learning — Session Continuity

## Project State (as of May 26, 2026)

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
GET  /api/health                     → 200
POST /api/auth/login                 → accessToken + refreshToken + user
POST /api/auth/register              → user + tokens (role defaults to parent, never admin)
POST /api/auth/refresh               → new accessToken
GET  /api/classrooms                 → [classroom]
GET  /api/children                   → [child with classroom_name]
POST /api/work-entries               → work entry with media[]
GET  /api/work-entries               → filtered entries
POST /api/messages                   → message (needs recipientType)
GET  /api/messages                   → messages with sender + is_read
GET  /api/daily-summary              → children with entries[]
GET  /api/daily-counts               → [{childId, childName, date, count, max:50}]
GET  /api/daily-counts/range         → [{date, count}] (calendar heatmap)
GET  /api/admin/terms                → [term]
POST /api/admin/terms                → create term (admin)
PUT  /api/admin/terms/:id            → update term (admin)
DELETE /api/admin/terms/:id          → delete term (admin)
GET  /api/admin/syllabus             → [syllabus with term/classroom names]
POST /api/admin/syllabus             → create syllabus (admin)
PUT  /api/admin/syllabus/:id         → update syllabus (admin)
DELETE /api/admin/syllabus/:id       → delete syllabus (admin)
GET  /api/admin/users                → [user] (admin only)
GET  /api/teacher-plans              → [teacher plan]
POST /api/teacher-plans              → create plan
PUT  /api/teacher-plans/:id          → update plan
DELETE /api/teacher-plans/:id        → delete plan
GET  /api/child-progress             → [progress]
POST /api/child-progress             → upsert progress (teacher/admin)
GET  /api/child-progress/:id         → single progress (restricted to own child for parents)
DELETE /api/child-progress/:id       → delete (admin)
GET  /api/admin/analytics            → 501 stub (admin)
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

### Remaining Code Review Findings (not yet fixed)
- **Photo swipe/reorder gesture**: CaptureScreen uses static list, not drag-reorderable
- **Per-photo upload progress**: UploadWorker batches, no individual progress tracking
- **Admin Users/Analytics screens**: Android screens are stubs (backend ready)
- **WhatsApp sharing**: `Intent(Intent.ACTION_SEND).setPackage("com.whatsapp")` not implemented
- **Google Sign-In**: No stub button in LoginScreen
- **No tests**: Backend or Android test suite not started
- **Docker secrets**: `.env` baked into Docker image; `docker-compose.yml` has passwords in env vars
- **AuthInterceptor**: `runBlocking` on OkHttp dispatcher — works but not ideal (standard pattern)
- **init.sql**: Not updated with curriculum tables (handled by knex migrations instead)
- **ParentExpectationsScreen**: Loads syllabus without filtering by child's classroom

## Files Changed This Session

| File | Change |
|------|--------|
| `docker-compose.yml` | ntfy command, caddy port 8081, db password in URL |
| `server/Dockerfile` | bcrypt→bcryptjs |
| `server/package.json` | bcrypt→bcryptjs |
| `server/src/index.js` | trust proxy, validate xForwardedForHeader, uncaughtException handlers |
| `server/src/db/knex.js` | conditional DB_PASSWORD |
| `server/src/routes/auth.js` | require bcrypt→bcryptjs, added role validation (never admin via register) |
| `server/src/routes/workEntries.js` | removed array destructuring from .first(), fixed isCover boolean logic |
| `server/src/routes/childProgress.js` | Added parent access check on single GET /:id |
| `server/src/routes/dailyCounts.js` | Added GET /range endpoint for calendar heatmap |
| `server/src/routes/admin.js` | Added GET /api/admin/users endpoint, terms+syllabus CRUD |
| `server/src/db/migrations/20260527000001_curriculum.js` | Added day_of_week CHECK constraints, CASCADE deletes, down cleanup |
| `server/src/db/migrations/20260526000002_fix_constraints.js` | NEW — adds missing CHECK constraints for existing DBs |
| `gradle.properties` | org.gradle.jvmargs (2g heap) |
| `README.md` | Updated with API endpoints, test accounts, current status |
| `app/.../domain/model/Child.kt` | Added @SerializedName for snake_case API fields |
| `app/.../domain/model/WorkEntry.kt` | Added @SerializedName for snake_case API fields |
| `app/.../domain/model/Message.kt` | Added @SerializedName for snake_case API fields |
| `app/.../domain/model/Classroom.kt` | Added import for @SerializedName |
| `app/.../data/remote/ApiService.kt` | Added AdminUserDto, MediaKey, DailyRangeCount, getDailyCountRange(), getAdminUsers(), SerializedName import |
| `app/.../data/remote/AuthInterceptor.kt` | No functional change (runBlocking pattern standard for OkHttp interceptors) |
| `app/.../data/repository/WorkRepository.kt` | Added getDailyCountRange() |
| `app/.../data/repository/TermRepository.kt` | Fixed delete to check HTTP status |
| `app/.../data/repository/TeacherPlanRepository.kt` | Fixed delete to check HTTP status |
| `app/.../data/repository/ChildProgressRepository.kt` | Fixed delete to check HTTP status |
| `app/.../data/local/UploadWorker.kt` | NEW — implements actual photo upload + work entry creation with media keys |
| `app/.../ui/admin/AdminViewModel.kt` | Added ClassRepository inject, loadClassrooms() |
| `app/.../ui/admin/TermManagementScreen.kt` | FIXED: edit dialog now calls onUpdateTerm (was silently doing nothing) |
| `app/.../ui/admin/SyllabusEditScreen.kt` | FIXED: dayOfWeek dropdown, added term/classroom pickers |
| `app/.../ui/admin/ClassroomsScreen.kt` | Populated with real classroom list (was "Coming soon" stub) |
| `app/.../ui/navigation/NavGraph.kt` | Wired term/classroom data into SyllabusEditScreen, onUpdateTerm for Terms, CalendarHeatmapViewModel |
| `app/.../ui/parent/CalendarHeatmapScreen.kt` | NEW — full implementation with child selector + 3-month color grid |
| `app/.../ui/parent/CalendarHeatmapViewModel.kt` | NEW — loads children + daily count range |
| `app/.../ui/parent/ParentViewModel.kt` | FIXED: SocketManager.connect called with token on init |
| `app/.../ui/teacher/TeacherViewModel.kt` | FIXED: loadTodayEntries passes today's date instead of null |
| `app/.../util/DateUtils.kt` | Added minusMonths() |

## Key Decisions
- PostgreSQL + MinIO + ntfy + Express instead of Firebase (open source, zero cost, on-prem)
- Android only (macOS deferred)
- 50 images/child/day limit with teacher override
- bcryptjs instead of bcrypt (native module segfaulted on Alpine's musl libc)
