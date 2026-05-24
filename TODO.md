# Montessori Learning — Project TODO

## Phase 0: Backend & Infrastructure

- [ ] Choose backend: Firebase vs custom Node.js/PostgreSQL
- [ ] Set up Firebase project (Auth, Firestore, Storage, FCM)
- [ ] Define data models (User, Child, WorkEntry, Message, Classroom)
- [ ] Implement REST API or Firebase security rules
- [ ] Set up CI/CD (GitHub Actions)
- [ ] Create dev/staging/prod environments

## Phase 1: Android — Auth & Onboarding

- [ ] Replace existing coloring app code with new project structure
- [ ] Implement Firebase Auth (email + password, Google Sign-In)
- [ ] Role-based navigation: Teacher flow vs Parent flow vs Admin flow
- [ ] Profile setup screen (name, phone, classroom selection)
- [ ] Child profile creation (admin/teacher only)

## Phase 2: Android — Teacher Daily Work Capture

### Core Capture Flow (photo + comment per entry)
- [ ] Classroom dashboard — grid of children with avatars + today's entry count per child
- [ ] Camera integration (CameraX) — photo capture with preview
- [ ] Title field — name the work (e.g. "Pink Tower")
- [ ] Montessori area picker (Practical Life, Sensorial, Language, Math, Cultural)
- [ ] Teacher observation/comment text field (free text, 1-3 sentences)
- [ ] Multiple photos per entry (different angles, progress stages)
- [ ] Photo review — swipe through, delete, reorder, set cover image
- [ ] Upload queue with progress indicators per photo
- [ ] Offline support — save drafts to Room, sync when online via WorkManager
- [ ] Today's entries list — quick view of all entries captured today, grouped by child

### Daily Upload Limit (50 images/child/day)
- [ ] Server: `COUNT` query on `media` by child+date before each upload
- [ ] Server: `X-Daily-Count` and `X-Daily-Max` response headers on upload
- [ ] Server: `GET /api/daily-count/:childId` endpoint
- [ ] Client: check daily count before starting capture
- [ ] Client: show warning dialog when limit reached with "Upload Anyway" option
- [ ] Client: `X-Override-Limit` header when teacher overrides

## Phase 3: Android — Parent Daily Feed

- [ ] Today's feed — chronological list of entries with photo thumbnail + title + comment
- [ ] Entry detail card — full-size photo, Montessori area badge, teacher comment, timestamp
- [ ] Multi-photo viewer — swipe between photos of same entry, pinch-to-zoom
- [ ] Push notifications (ntfy) — instant alert when new entry posted: "{child} — {title}"
- [ ] Daily summary card at top — "3 entries today · 5 photos" with share button
- [ ] Historical archive — date picker to view past days, grouped by date
- [ ] Calendar heatmap — visual overview of how many entries per day this month
- [ ] Share single entry — via WhatsApp deep link or system share sheet

## Phase 4: Android — Messaging System

- [ ] In-app chat: teacher ↔ parent, teacher ↔ class group
- [ ] Admin broadcast to all parents
- [ ] Message threading per child or classroom
- [ ] Read receipts
- [ ] Push notification for new messages

## Phase 5: WhatsApp & Email Integration

- [ ] WhatsApp deep link sharing — share individual entries via `wa.me`
- [ ] WhatsApp Business API — daily digest opt-in, broadcast announcements
- [ ] Email integration (SendGrid/SES) — daily/weekly portfolio digests
- [ ] Email templates for work summaries and announcements
- [ ] Notification preferences per parent (push / whatsapp / email / all)

## Phase 6: Admin Panel

- [ ] User management (teachers, parents, children)
- [ ] Classroom creation and assignment
- [ ] Broadcast messaging
- [ ] Analytics — entries per day, active users, message volume
- [ ] Audit log

## Phase 7: Polish & Launch

- [ ] UI/UX review — kid-appropriate design, accessibility
- [ ] Performance — image/video compression, caching
- [ ] Security audit — data privacy, GDPR/COPPA compliance
- [ ] Testing — unit tests, UI tests, beta testing with school
- [ ] App store assets — screenshots, descriptions, privacy policy
- [ ] Play Store release (Android)
- [ ] App Store release (macOS)
