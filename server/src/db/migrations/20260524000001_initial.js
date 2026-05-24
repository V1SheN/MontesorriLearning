exports.up = function (knex) {
  return knex.schema
    .raw('CREATE EXTENSION IF NOT EXISTS "pgcrypto"')
    .createTable('users', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.text('email').unique().notNullable();
      table.text('phone');
      table.text('password_hash').notNullable();
      table.text('display_name').notNullable();
      table.text('role').notNullable();
      table.text('avatar_path');
      table.jsonb('notif_prefs').notNullable().defaultTo({ push: true, email: false, digest: 'daily' });
      table.timestamptz('created_at').notNullable().defaultTo(knex.fn.now());
      table.timestamptz('updated_at').notNullable().defaultTo(knex.fn.now());
    })
    .raw(
      "ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('teacher', 'parent', 'admin'))"
    )
    .createTable('classrooms', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.text('name').notNullable();
      table.text('level').notNullable();
      table.timestamptz('created_at').notNullable().defaultTo(knex.fn.now());
    })
    .raw(
      "ALTER TABLE classrooms ADD CONSTRAINT classrooms_level_check CHECK (level IN ('toddler', 'casa', 'elementary'))"
    )
    .createTable('classroom_teachers', (table) => {
      table.uuid('classroom_id').notNullable().references('id').inTable('classrooms').onDelete('CASCADE');
      table.uuid('teacher_id').notNullable().references('id').inTable('users').onDelete('CASCADE');
      table.primary(['classroom_id', 'teacher_id']);
    })
    .createTable('children', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.text('name').notNullable();
      table.date('date_of_birth');
      table.uuid('classroom_id').notNullable().references('id').inTable('classrooms');
      table.text('photo_path');
      table.boolean('active').notNullable().defaultTo(true);
      table.timestamptz('created_at').notNullable().defaultTo(knex.fn.now());
    })
    .createTable('child_parents', (table) => {
      table.uuid('child_id').notNullable().references('id').inTable('children').onDelete('CASCADE');
      table.uuid('parent_id').notNullable().references('id').inTable('users').onDelete('CASCADE');
      table.primary(['child_id', 'parent_id']);
    })
    .createTable('work_entries', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.uuid('child_id').notNullable().references('id').inTable('children');
      table.uuid('teacher_id').notNullable().references('id').inTable('users');
      table.uuid('classroom_id').notNullable().references('id').inTable('classrooms');
      table.text('montessori_area').notNullable();
      table.text('title').notNullable().defaultTo('');
      table.text('teacher_comment').notNullable().defaultTo('');
      table.timestamptz('deleted_at');
      table.timestamptz('created_at').notNullable().defaultTo(knex.fn.now());
    })
    .raw(
      "ALTER TABLE work_entries ADD CONSTRAINT work_entries_area_check CHECK (montessori_area IN ('practical_life', 'sensorial', 'language', 'math', 'cultural'))"
    )
    .createTable('media', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.uuid('entry_id').notNullable().references('id').inTable('work_entries').onDelete('CASCADE');
      table.text('media_type').notNullable();
      table.text('storage_key').notNullable();
      table.text('thumbnail_key');
      table.integer('width');
      table.integer('height');
      table.bigInteger('file_size');
      table.boolean('is_cover').notNullable().defaultTo(false);
      table.text('caption');
      table.integer('sort_order').notNullable().defaultTo(0);
      table.timestamptz('created_at').notNullable().defaultTo(knex.fn.now());
    })
    .raw(
      "ALTER TABLE media ADD CONSTRAINT media_type_check CHECK (media_type IN ('image', 'video'))"
    )
    .createTable('messages', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.uuid('sender_id').notNullable().references('id').inTable('users');
      table.uuid('classroom_id').references('id').inTable('classrooms');
      table.text('subject');
      table.text('body').notNullable();
      table.specificType('channels', 'TEXT[]').notNullable().defaultTo(knex.raw("'{push}'"));
      table.timestamptz('created_at').notNullable().defaultTo(knex.fn.now());
    })
    .createTable('message_recipients', (table) => {
      table.uuid('message_id').notNullable().references('id').inTable('messages').onDelete('CASCADE');
      table.uuid('user_id').notNullable().references('id').inTable('users').onDelete('CASCADE');
      table.timestamptz('read_at');
      table.primary(['message_id', 'user_id']);
    })
    .raw('CREATE INDEX idx_media_created ON media (created_at)')
    .raw('CREATE INDEX idx_entries_child_created ON work_entries (child_id, created_at DESC)')
    .raw('CREATE INDEX idx_entries_classroom_date ON work_entries (classroom_id, created_at DESC)')
    .raw('CREATE INDEX idx_media_entry ON media (entry_id)')
    .raw('CREATE INDEX idx_messages_recipient ON message_recipients (user_id, message_id)')
    .raw('CREATE INDEX idx_entries_created ON work_entries (created_at)');
};

exports.down = function (knex) {
  return knex.schema
    .dropTableIfExists('message_recipients')
    .dropTableIfExists('messages')
    .dropTableIfExists('media')
    .dropTableIfExists('work_entries')
    .dropTableIfExists('child_parents')
    .dropTableIfExists('children')
    .dropTableIfExists('classroom_teachers')
    .dropTableIfExists('classrooms')
    .dropTableIfExists('users');
};
