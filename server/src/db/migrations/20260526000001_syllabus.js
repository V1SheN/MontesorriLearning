exports.up = function (knex) {
  return knex.schema
    .createTable('syllabus', (table) => {
      table.uuid('id').primary().defaultTo(knex.raw('gen_random_uuid()'));
      table.uuid('classroom_id').notNullable().references('id').inTable('classrooms').onDelete('CASCADE');
      table.text('montessori_area').notNullable();
      table.text('title').notNullable();
      table.text('description').notNullable().defaultTo('');
      table.integer('week_number');
      table.integer('year').notNullable().defaultTo(knex.raw("EXTRACT(YEAR FROM CURRENT_DATE)"));
      table.integer('sort_order').notNullable().defaultTo(0);
      table.timestamp('created_at', { useTz: true }).notNullable().defaultTo(knex.fn.now());
      table.timestamp('updated_at', { useTz: true }).notNullable().defaultTo(knex.fn.now());
    })
    .raw(
      "ALTER TABLE syllabus ADD CONSTRAINT syllabus_area_check CHECK (montessori_area IN ('practical_life', 'sensorial', 'language', 'math', 'cultural'))"
    )
    .raw('CREATE INDEX idx_syllabus_classroom ON syllabus (classroom_id, sort_order)')
    .raw('CREATE INDEX idx_syllabus_area ON syllabus (classroom_id, montessori_area, week_number)');
};

exports.down = function (knex) {
  return knex.schema.dropTableIfExists('syllabus');
};
