const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');

const router = express.Router();

// ─── Analytics stub ──────────────────────────────────────────
router.get('/analytics', authenticate, requireRole('admin'), (req, res) => {
  res.status(501).json({ message: 'Not implemented', status: 'stub' });
});

// ─── Syllabus CRUD ───────────────────────────────────────────

const VALID_AREAS = ['practical_life', 'sensorial', 'language', 'math', 'cultural'];

router.get('/syllabus', authenticate, requireRole('admin', 'teacher'), async (req, res, next) => {
  try {
    let query = knex('syllabus')
      .leftJoin('classrooms', 'syllabus.classroom_id', 'classrooms.id')
      .select('syllabus.*', 'classrooms.name as classroom_name')
      .orderBy(['syllabus.sort_order', 'syllabus.title']);

    if (req.query.classroomId) {
      query = query.where('syllabus.classroom_id', req.query.classroomId);
    }
    if (req.query.montessoriArea) {
      query = query.where('syllabus.montessori_area', req.query.montessoriArea);
    }
    if (req.query.year) {
      query = query.where('syllabus.year', parseInt(req.query.year));
    }

    const items = await query;
    res.json(items);
  } catch (err) {
    next(err);
  }
});

router.get('/syllabus/:id', authenticate, requireRole('admin', 'teacher'), async (req, res, next) => {
  try {
    const item = await knex('syllabus').where({ id: req.params.id }).first();
    if (!item) return res.status(404).json({ error: 'Syllabus item not found' });
    res.json(item);
  } catch (err) {
    next(err);
  }
});

router.post('/syllabus', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const { classroomId, montessoriArea, title, description, weekNumber, year, sortOrder } = req.body;

    if (!classroomId || !montessoriArea || !title) {
      return res.status(400).json({ error: 'classroomId, montessoriArea, and title are required' });
    }
    if (!VALID_AREAS.includes(montessoriArea)) {
      return res.status(400).json({ error: `montessoriArea must be one of: ${VALID_AREAS.join(', ')}` });
    }

    const [classroom] = await knex('classrooms').where({ id: classroomId });
    if (!classroom) return res.status(404).json({ error: 'Classroom not found' });

    const [item] = await knex('syllabus').insert({
      classroom_id: classroomId,
      montessori_area: montessoriArea,
      title,
      description: description || '',
      week_number: weekNumber || null,
      year: year || new Date().getFullYear(),
      sort_order: sortOrder || 0,
    }).returning('*');

    res.status(201).json(item);
  } catch (err) {
    next(err);
  }
});

router.put('/syllabus/:id', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const existing = await knex('syllabus').where({ id: req.params.id }).first();
    if (!existing) return res.status(404).json({ error: 'Syllabus item not found' });

    const { classroomId, montessoriArea, title, description, weekNumber, year, sortOrder } = req.body;

    if (montessoriArea && !VALID_AREAS.includes(montessoriArea)) {
      return res.status(400).json({ error: `montessoriArea must be one of: ${VALID_AREAS.join(', ')}` });
    }

    const updates = {};
    if (classroomId !== undefined) updates.classroom_id = classroomId;
    if (montessoriArea !== undefined) updates.montessori_area = montessoriArea;
    if (title !== undefined) updates.title = title;
    if (description !== undefined) updates.description = description;
    if (weekNumber !== undefined) updates.week_number = weekNumber;
    if (year !== undefined) updates.year = year;
    if (sortOrder !== undefined) updates.sort_order = sortOrder;
    updates.updated_at = knex.fn.now();

    const [item] = await knex('syllabus').where({ id: req.params.id }).update(updates).returning('*');
    res.json(item);
  } catch (err) {
    next(err);
  }
});

router.delete('/syllabus/:id', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const existing = await knex('syllabus').where({ id: req.params.id }).first();
    if (!existing) return res.status(404).json({ error: 'Syllabus item not found' });

    await knex('syllabus').where({ id: req.params.id }).del();
    res.json({ message: 'Deleted' });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
