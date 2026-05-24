# Testing Guide

## Prerequisites

| Tool | Required for | Status |
|------|-------------|--------|
| Docker + Compose | Backend (Postgres, MinIO, ntfy, API) | ✅ Installed |
| Node.js 20 | Server dependencies | ✅ Installed |
| Java 17+ | Android build | ❌ Install via SDKMAN or distro package |
| Android Studio | Android build + emulator | ❌ Download from developer.android.com |
| Android SDK 34 | Android build | ❌ Install via Android Studio SDK Manager |

---

## 1. Start the Backend (Docker)

```bash
# From project root
docker compose up -d
```

This starts 5 containers:
- **postgres**: Database on port 5432
- **minio**: File storage on ports 9000 (API) + 9001 (Console)
- **ntfy**: Push notifications on internal port 80
- **api**: Express server on port 3000
- **caddy**: Reverse proxy on port 8080

Check they're running:
```bash
docker compose ps
```

Check logs:
```bash
docker compose logs -f api
```

---

## 2. Run Database Migrations

```bash
cd server
npm run migrate
```

---

## 3. Seed Test Data

```bash
cd server
node -e "
const knex = require('./src/db/knex');
const bcrypt = require('bcrypt');
const { v4: uuid } = require('uuid');

async function seed() {
  // Teacher
  const teacherId = uuid();
  await knex('users').insert({
    id: teacherId, email: 'teacher@school.com',
    password_hash: await bcrypt.hash('password', 10),
    display_name: 'Maria Montessori', role: 'teacher'
  });

  // Parent
  const parentId = uuid();
  await knex('users').insert({
    id: parentId, email: 'parent@home.com',
    password_hash: await bcrypt.hash('password', 10),
    display_name: 'Anna Parent', role: 'parent'
  });

  // Classroom
  const classroomId = uuid();
  await knex('classrooms').insert({
    id: classroomId, name: 'Casa A', level: 'casa'
  });
  await knex('classroom_teachers').insert({
    classroom_id: classroomId, teacher_id: teacherId
  });

  // Children
  const childIds = [];
  for (const name of ['Ava', 'Ben', 'Catarina']) {
    const id = uuid();
    childIds.push(id);
    await knex('children').insert({
      id, name, classroom_id: classroomId, active: true
    });
    await knex('child_parents').insert({
      child_id: id, parent_id: parentId
    });
  }

  console.log('Seeded: teacher@school.com / parent@home.com');
  console.log('Password: password');
  console.log('Children:', childIds.join(', '));
  process.exit(0);
}
seed().catch(e => { console.error(e); process.exit(1); });
"
```

Or use this one-liner for a quick login test:
```bash
curl -s localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher@school.com","password":"password"}'
```

---

## 4. Test API Endpoints (curl)

Save your token:
```bash
TOKEN=$(curl -s localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher@school.com","password":"password"}' | \
  node -e "process.stdin.on('data',d=>console.log(JSON.parse(d).accessToken))")
```

### Auth
```bash
curl localhost:3000/api/auth/me -H "Authorization: Bearer $TOKEN"
```

### Children
```bash
curl localhost:3000/api/children -H "Authorization: Bearer $TOKEN"
```

### Classrooms
```bash
curl localhost:3000/api/classrooms -H "Authorization: Bearer $TOKEN"
```

### Daily Count (before uploading)
```bash
curl localhost:3000/api/daily-count/<childId> -H "Authorization: Bearer $TOKEN"
```

### Upload a test photo
```bash
# Create a small test image
convert -size 400x300 xc:white /tmp/test.jpg 2>/dev/null || \
echo "A fake image file..." > /tmp/test.jpg

curl -X POST localhost:3000/api/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/tmp/test.jpg" \
  -F "childId=<childId>"
```

### Create a work entry
```bash
curl -X POST localhost:3000/api/work-entries \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "childId": "<childId>",
    "title": "Pink Tower",
    "montessoriArea": "sensorial",
    "teacherComment": "Completed the Pink Tower independently today.",
    "media": []
  }'
```

### Daily Summary
```bash
curl "localhost:3000/api/daily-summary?date=$(date +%Y-%m-%d)" \
  -H "Authorization: Bearer $TOKEN"
```

### Test daily limit (upload 50 images)
```bash
for i in $(seq 1 50); do
  curl -s -X POST localhost:3000/api/upload \
    -H "Authorization: Bearer $TOKEN" \
    -F "file=@/tmp/test.jpg" \
    -F "childId=<childId>" | node -e "process.stdin.on('data',d=>console.log(d.toString().includes('limit')?'LIMIT REACHED':'ok'))"
done
```

---

## 5. Build & Run Android App (requires JDK 17 + Android SDK)

### Install JDK 17
```bash
# Using SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.10-tem

# OR using apt (Ubuntu/Debian)
sudo apt update && sudo apt install openjdk-17-jdk
```

### Install Android SDK
Option A — Android Studio (easiest):
1. Download from https://developer.android.com/studio
2. Install → SDK Manager → install Android 34 (API 34)
3. Open project → it will sync and build

Option B — Command-line only:
```bash
# Install command-line tools
cd ~
curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip cmdline-tools.zip
mkdir -p android-sdk/cmdline-tools
mv cmdline-tools android-sdk/cmdline-tools/latest
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

# Accept licenses + install SDK
yes | sdkmanager --licenses
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

### Set API URL
Edit `app/src/main/java/com/example/montesorrilearning/data/remote/ApiService.kt`:
```kotlin
// Emulator accessing host machine:
// Use 10.0.2.2 instead of localhost
baseUrl = "http://10.0.2.2:8080/"

// Physical device on same WiFi:
// Use your machine's LAN IP
baseUrl = "http://192.168.1.x:8080/"
```

### Build and install
```bash
export ANDROID_HOME=~/android-sdk
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 6. End-to-End Test Flow

Once both backend and Android app are running:

```
TEACHER DEVICE                     PARENT DEVICE
─────────────                      ─────────────
Login as teacher                   Login as parent
                                  ↓
See classroom dashboard           See empty feed (no entries yet)
                                  ↓
Tap "Ava" → camera opens         (notification arrives)
                                  ↓
Take photo of Pink Tower         Tap notification → feed opens
                                  ↓
Title: "Pink Tower"              See entry card with photo
Area: Sensorial                   See title + comment
Comment: "Completed..."           Can tap for full size
                                  ↓
Tap Submit                       See daily summary card
  → photo uploads                "1 entry today · 1 photo"
  → entry saved
  → push sent to parent
```

### What to verify

- [ ] Login works for both teacher and parent
- [ ] Teacher sees children in dashboard
- [ ] Camera opens and captures photo
- [ ] Photo uploads and entry saves
- [ ] Parent receives push notification
- [ ] Parent sees entry in feed
- [ ] Parent can view full-size photo
- [ ] Daily summary card shows correct count
- [ ] Messaging: teacher → parent
- [ ] Daily limit: upload 50 → see warning → override works
- [ ] Offline: disconnect WiFi → capture → reconnect → syncs
- [ ] Calendar archive: pick past date → see entries
- [ ] Share entry via WhatsApp

---

## 7. Run Server Tests (Jest)

```bash
cd server
npm install --save-dev jest supertest
```

Create `src/__tests__/auth.test.js`:
```javascript
const request = require('supertest');
const app = require('../index'); // export app from index.js

describe('Auth', () => {
  it('registers a teacher', async () => {
    const res = await request(app)
      .post('/api/auth/register')
      .send({ email: 'test@test.com', password: 'pass', displayName: 'Test', role: 'teacher' });
    expect(res.status).toBe(201);
    expect(res.body).toHaveProperty('accessToken');
  });
});
```

Run:
```bash
npm test
```
