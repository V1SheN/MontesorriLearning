exports.up = function (knex) {
  return knex.schema
    .createTable('terms', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.text('name').notNullable();
      table.date('start_date').notNullable();
      table.date('end_date').notNullable();
      table.integer('year').notNullable().defaultTo(knex.raw("EXTRACT(YEAR FROM CURRENT_DATE)"));
      table.timestamp('created_at', { useTz: true }).notNullable().defaultTo(knex.fn.now());
    })
    .raw('DROP TABLE IF EXISTS syllabus CASCADE')
    .createTable('syllabus', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.uuid('term_id').notNullable().references('id').inTable('terms').onDelete('CASCADE');
      table.uuid('classroom_id').notNullable().references('id').inTable('classrooms').onDelete('CASCADE');
      table.text('montessori_area').notNullable();
      table.text('title').notNullable();
      table.text('description').notNullable().defaultTo('');
      table.integer('day_of_week').notNullable().defaultTo(1);
      table.integer('week_number');
      table.integer('sort_order').notNullable().defaultTo(0);
      table.boolean('is_extracurricular').notNullable().defaultTo(false);
      table.text('activity_type');
      table.integer('duration_minutes');
      table.timestamp('created_at', { useTz: true }).notNullable().defaultTo(knex.fn.now());
      table.timestamp('updated_at', { useTz: true }).notNullable().defaultTo(knex.fn.now());
    })
    .raw("ALTER TABLE syllabus ADD CONSTRAINT syllabus_area_check CHECK (montessori_area IN ('practical_life', 'sensorial', 'language', 'math', 'cultural', 'extracurricular'))")
    .raw('CREATE INDEX idx_syllabus_term ON syllabus (term_id, sort_order)')
    .raw('CREATE INDEX idx_syllabus_week ON syllabus (term_id, classroom_id, week_number, day_of_week)')
    .createTable('teacher_plans', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.uuid('syllabus_id').references('id').inTable('syllabus').onDelete('SET NULL');
      table.uuid('teacher_id').notNullable().references('id').inTable('users');
      table.uuid('classroom_id').notNullable().references('id').inTable('classrooms');
      table.uuid('term_id').notNullable().references('id').inTable('terms');
      table.text('title').notNullable();
      table.text('montessori_area').notNullable();
      table.text('description').notNullable().defaultTo('');
      table.date('planned_date').notNullable();
      table.integer('day_of_week').notNullable();
      table.integer('week_number');
      table.boolean('is_extracurricular').notNullable().defaultTo(false);
      table.text('activity_type');
      table.integer('duration_minutes');
      table.boolean('is_completed').notNullable().defaultTo(false);
      table.text('teacher_notes');
      table.timestamp('created_at', { useTz: true }).notNullable().defaultTo(knex.fn.now());
      table.timestamp('updated_at', { useTz: true }).notNullable().defaultTo(knex.fn.now());
    })
    .raw("ALTER TABLE teacher_plans ADD CONSTRAINT teacher_plans_area_check CHECK (montessori_area IN ('practical_life', 'sensorial', 'language', 'math', 'cultural', 'extracurricular'))")
    .raw('CREATE INDEX idx_plans_classroom ON teacher_plans (classroom_id, planned_date)')
    .raw('CREATE INDEX idx_plans_teacher ON teacher_plans (teacher_id)')
    .createTable('child_progress', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.uuid('child_id').notNullable().references('id').inTable('children').onDelete('CASCADE');
      table.uuid('syllabus_id').references('id').inTable('syllabus').onDelete('SET NULL');
      table.uuid('teacher_plan_id').references('id').inTable('teacher_plans').onDelete('SET NULL');
      table.text('status').notNullable().defaultTo('pending');
      table.text('observation_notes');
      table.timestamp('completed_at');
      table.timestamp('created_at', { useTz: true }).notNullable().defaultTo(knex.fn.now());
      table.timestamp('updated_at', { useTz: true }).notNullable().defaultTo(knex.fn.now());
    })
    .raw("ALTER TABLE child_progress ADD CONSTRAINT child_progress_status_check CHECK (status IN ('pending', 'in_progress', 'completed', 'mastered'))")
    .raw('CREATE INDEX idx_progress_child ON child_progress (child_id)')
    .raw('CREATE UNIQUE INDEX idx_progress_child_syllabus ON child_progress (child_id, syllabus_id) WHERE syllabus_id IS NOT NULL')
    .raw('CREATE UNIQUE INDEX idx_progress_child_plan ON child_progress (child_id, teacher_plan_id) WHERE teacher_plan_id IS NOT NULL');
};

exports.down = function (knex) {
  return knex.schema
    .dropTableIfExists('child_progress')
    .dropTableIfExists('teacher_plans')
    .dropTableIfExists('syllabus')
    .dropTableIfExists('terms');
};
