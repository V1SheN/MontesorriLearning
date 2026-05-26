exports.up = async function (knex) {
  const hasDow = await knex.schema.hasColumn('syllabus', 'day_of_week');

  // Add syllabus day_of_week CHECK if table exists
  if (hasDow) {
    await knex.raw('ALTER TABLE syllabus DROP CONSTRAINT IF EXISTS syllabus_dow_check');
    await knex.raw("ALTER TABLE syllabus ADD CONSTRAINT syllabus_dow_check CHECK (day_of_week BETWEEN 1 AND 5)");
  }

  // Add teacher_plans day_of_week CHECK
  const hasPlanDow = await knex.schema.hasColumn('teacher_plans', 'day_of_week');
  if (hasPlanDow) {
    await knex.raw('ALTER TABLE teacher_plans DROP CONSTRAINT IF EXISTS teacher_plans_dow_check');
    await knex.raw("ALTER TABLE teacher_plans ADD CONSTRAINT teacher_plans_dow_check CHECK (day_of_week BETWEEN 1 AND 5)");
  }
};

exports.down = async function (knex) {
  await knex.raw('ALTER TABLE syllabus DROP CONSTRAINT IF EXISTS syllabus_dow_check');
  await knex.raw('ALTER TABLE teacher_plans DROP CONSTRAINT IF EXISTS teacher_plans_dow_check');
};
