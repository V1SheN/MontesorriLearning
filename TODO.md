# Montessori Learning — Project TODO

Status: ✅ Done  ⚠️ Partial  ❌ Missing

## Phase 0: Backend & Infrastructure

- [x] Choose backend: custom Node.js/Express + PostgreSQL (rejected Firebase)
- [x] Define data models (User, Child, WorkEntry, Message, Classroom)
- [x] Implement REST API (7 route files: auth, children, classrooms, workEntries, upload, messages, dailySummary)
- [ ] Set up CI/CD (GitHub Actions)
- [ ] Create dev/staging/prod environments

## Phase 1: Android — Auth & Onboarding

- [x] Replace existing coloring app code with new project structure
- [x] Email/password auth via custom JWT backend
- [ ] Google Sign-In
- [x] Role-based navigation: Teacher flow vs Parent flow
- [x] Admin flow navigation (AdminDashboardScreen + Users/Classrooms/Analytics stubs)
- [ ] Profile setup screen (name, phone, classroom selection)
- [x] Server: Child profile creation API (teacher/admin)
- [ ] Android: Child profile creation UI

## Phase 2: Teacher Daily Work Capture

- [x] Server: Work entries CRUD with media support
- [x] Classroom dashboard — grid of children
- [x] Dashboard: populate daily counts from API (TeacherViewModel → dailyCounts endpoint)
- [x] Camera integration (CameraX) — photo capture with preview
- [x] Title field — name the work
- [x] Montessori area picker (5 areas)
- [x] Teacher observation/comment text field
- [x] Multiple photos per entry
- [x] Photo review — delete photos
- [ ] Photo review — swipe gestures, reorder, set cover image
- [x] Upload progress indicator (global)
- [ ] Upload queue with per-photo progress
- [x] Room database for offline drafts
- [ ] WorkManager sync for pending uploads (UploadWorker stub created)
- [x] Today's entries list
- [x] Today's entries: group by child

### Daily Upload Limit (50 images/child/day)
- [x] Server: COUNT query before each upload
- [x] Server: X-Daily-Count / X-Daily-Max headers
- [x] Server: GET /api/daily-count/:childId endpoint
- [x] Client: check daily count before capture
- [x] Client: warning dialog with "Upload Anyway" override
- [x] Client: X-Override-Limit header

## Phase 3: Parent Daily Feed

- [x] Today's feed — chronological list with thumbnail + title + comment
- [x] Entry detail card — full-size photo, area badge, timestamp
- [x] Multi-photo viewer (vertical list)
- [ ] Multi-photo viewer: swipe between photos, pinch-to-zoom
- [x] Push notifications (ntfy) — instant alert on new entry
- [x] Daily summary card (entries + photos count)
- [ ] Share button on daily summary card
- [x] Historical archive — date picker to view past days
- [ ] Calendar heatmap (stub screen created)
- [x] Share single entry via system share sheet
- [ ] WhatsApp deep link sharing

## Phase 4: Messaging

- [x] Server: send messages (individual, class, all)
- [x] Android: message compose UI (subject + body)
- [ ] Android: specify individual recipient or broadcast
- [ ] Message threading per child or classroom
- [x] Read receipts
- [x] Push notification for new messages

## Phase 5: WhatsApp & Email

- [ ] WhatsApp deep link — share entries via wa.me
- [ ] WhatsApp Business API — digest opt-in, broadcasts
- [ ] Email integration (SendGrid/SES)
- [ ] Email templates for summaries
- [x] Database schema: notification preferences per parent
- [ ] Android: notification preferences UI (stub screen created)

## Phase 6: Admin Panel

- [ ] User management (stub admin screen created)
- [ ] Classroom creation and assignment (server: GET only, POST/PUT/DELETE stubs)
- [x] Broadcast messaging (server: recipientType=all)
- [ ] Android: broadcast messaging UI
- [ ] Analytics dashboard (server stub + admin stub screen)
- [ ] Audit log

## Phase 7: Polish & Launch

- [ ] UI/UX review
- [x] Image compression (Sharp: 1600px, 80% quality, thumbnails)
- [ ] Client-side caching strategy
- [ ] Security audit
- [ ] Unit tests / UI tests
- [ ] App store assets
- [ ] Play Store release
