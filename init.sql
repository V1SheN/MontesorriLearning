-- Montessori Learning — Database Schema

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- Users
-- ============================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           TEXT UNIQUE NOT NULL,
    phone           TEXT,
    password_hash   TEXT NOT NULL,
    display_name    TEXT NOT NULL,
    role            TEXT NOT NULL CHECK (role IN ('teacher', 'parent', 'admin')),
    avatar_path     TEXT,
    notif_prefs     JSONB NOT NULL DEFAULT '{"push":true,"email":false,"digest":"daily"}'::jsonb,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Classrooms
-- ============================================================
CREATE TABLE classrooms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    level       TEXT NOT NULL CHECK (level IN ('toddler', 'casa', 'elementary')),
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
-- Work Entries (one photo + one comment per entry)
-- ============================================================
CREATE TABLE work_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    child_id        UUID NOT NULL REFERENCES children(id),
    teacher_id      UUID NOT NULL REFERENCES users(id),
    classroom_id    UUID NOT NULL REFERENCES classrooms(id),
    montessori_area TEXT NOT NULL CHECK (montessori_area IN (
        'practical_life', 'sensorial', 'language', 'math', 'cultural'
    )),
    title           TEXT NOT NULL DEFAULT '',
    teacher_comment TEXT NOT NULL DEFAULT '',
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Media (photos of physical work)
-- ============================================================
CREATE TABLE media (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id       UUID NOT NULL REFERENCES work_entries(id) ON DELETE CASCADE,
    media_type     TEXT NOT NULL CHECK (media_type IN ('image', 'video')),
    storage_key    TEXT NOT NULL,
    thumbnail_key  TEXT,
    width          INT,
    height         INT,
    file_size      BIGINT,
    is_cover       BOOLEAN NOT NULL DEFAULT false,
    caption        TEXT,
    sort_order     INT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Messages
-- ============================================================
CREATE TABLE messages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id    UUID NOT NULL REFERENCES users(id),
    classroom_id UUID REFERENCES classrooms(id),
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
-- Syllabus (admin-managed curriculum)
-- ============================================================
CREATE TABLE syllabus (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    classroom_id    UUID NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE,
    montessori_area TEXT NOT NULL CHECK (montessori_area IN (
        'practical_life', 'sensorial', 'language', 'math', 'cultural'
    )),
    title           TEXT NOT NULL,
    description     TEXT NOT NULL DEFAULT '',
    week_number     INT,
    year            INT NOT NULL DEFAULT EXTRACT(YEAR FROM CURRENT_DATE),
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_media_created ON media (created_at);
CREATE INDEX idx_entries_child_created ON work_entries (child_id, created_at DESC);
CREATE INDEX idx_entries_classroom_date ON work_entries (classroom_id, created_at DESC);
CREATE INDEX idx_media_entry ON media (entry_id);
CREATE INDEX idx_messages_recipient ON message_recipients (user_id, message_id);
CREATE INDEX idx_entries_created ON work_entries (created_at);
CREATE INDEX idx_syllabus_classroom ON syllabus (classroom_id, sort_order);
CREATE INDEX idx_syllabus_area ON syllabus (classroom_id, montessori_area, week_number);
