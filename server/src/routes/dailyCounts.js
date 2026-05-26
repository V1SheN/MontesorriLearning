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

// ─── Daily count range (for calendar heatmap) ────────────────
router.get('/range', authenticate, requireRole('teacher', 'admin', 'parent'), async (req, res, next) => {
  try {
    const { childId, from, to } = req.query;
    if (!childId || !from || !to) {
      return res.status(400).json({ error: 'childId, from, and to are required' });
    }

    if (req.user.role === 'parent') {
      const childIds = await knex('child_parents').where({ parent_id: req.user.id }).pluck('child_id');
      if (!childIds.includes(childId)) {
        return res.status(403).json({ error: 'Access denied' });
      }
    }

    const rows = await knex('media as m')
      .join('work_entries as we', 'm.entry_id', 'we.id')
      .where('we.child_id', childId)
      .whereNull('we.deleted_at')
      .whereRaw('m.created_at::date BETWEEN ? AND ?', [from, to])
      .select(knex.raw('m.created_at::date as date'), knex.raw('COUNT(*)::int as count'))
      .groupByRaw('m.created_at::date')
      .orderBy('date');

    res.json(rows);
  } catch (err) { next(err); }
});

module.exports = router;
