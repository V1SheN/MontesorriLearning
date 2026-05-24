const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, async (req, res, next) => {
  try {
    let query = knex('classrooms').orderBy('name');

    if (req.user.role === 'teacher') {
      const classroomIds = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id })
        .pluck('classroom_id');
      query = query.whereIn('id', classroomIds);
    }

    const classrooms = await query;
    res.json(classrooms);
  } catch (err) {
    next(err);
  }
});

router.get('/:id', authenticate, async (req, res, next) => {
  try {
    const classroom = await knex('classrooms').where({ id: req.params.id }).first();

    if (!classroom) {
      return res.status(404).json({ error: 'Classroom not found' });
    }

    if (req.user.role === 'teacher') {
      const [assignment] = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id, classroom_id: classroom.id });
      if (!assignment) {
        return res.status(403).json({ error: 'Access denied' });
      }
    }

    const [children, teachers] = await Promise.all([
      knex('children').where({ classroom_id: classroom.id }).orderBy('name'),
      knex('users')
        .join('classroom_teachers', 'users.id', 'classroom_teachers.teacher_id')
        .where('classroom_teachers.classroom_id', classroom.id)
        .select('users.id', 'users.display_name', 'users.email', 'users.avatar_path'),
    ]);

    res.json({ ...classroom, children, teachers });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
