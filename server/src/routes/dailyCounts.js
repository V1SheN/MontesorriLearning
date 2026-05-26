const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const { classroomId } = req.query;

    let classroomIds = [];

    if (req.user.role === 'teacher') {
      classroomIds = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id })
        .pluck('classroom_id');
      if (classroomId) {
        if (!classroomIds.includes(classroomId)) {
          return res.status(403).json({ error: 'Access denied to this classroom' });
        }
        classroomIds = [classroomId];
      }
    } else if (classroomId) {
      classroomIds = [classroomId];
    } else {
      classroomIds = await knex('classrooms').pluck('id');
    }

    if (classroomIds.length === 0) {
      return res.json([]);
    }

    const rows = await knex('children as c')
      .select(
        'c.id as childId',
        'c.name as childName',
        knex.raw('CURRENT_DATE as date'),
        knex.raw('COUNT(m.id)::int as count')
      )
      .leftJoin('work_entries as we', function () {
        this.on('we.child_id', 'c.id').andOnNull('we.deleted_at');
      })
      .leftJoin('media as m', function () {
        this.on('m.entry_id', 'we.id').andOn(knex.raw('m.created_at::date = CURRENT_DATE'));
      })
      .whereIn('c.classroom_id', classroomIds)
      .where('c.active', true)
      .groupBy('c.id', 'c.name')
      .orderBy('c.name');

    const result = rows.map((r) => ({ ...r, max: 50 }));
    res.json(result);
  } catch (err) {
    next(err);
  }
});

module.exports = router;
