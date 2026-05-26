const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');

const router = express.Router();

// ─── Analytics stub ──────────────────────────────────────────
router.get('/analytics', authenticate, requireRole('admin'), (req, res) => {
  res.status(501).json({ message: 'Not implemented', status: 'stub' });
});

// ─── Terms CRUD ──────────────────────────────────────────────

const TERM_ORDER = ['Term 1', 'Term 2', 'Term 3', 'Term 4'];

router.get('/terms', authenticate, async (req, res, next) => {
  try {
    let query = knex('terms').orderBy('start_date');
    if (req.query.year) query = query.where('year', parseInt(req.query.year));
    const items = await query;
    res.json(items);
  } catch (err) { next(err); }
});

router.get('/terms/:id', authenticate, async (req, res, next) => {
  try {
    const item = await knex('terms').where({ id: req.params.id }).first();
    if (!item) return res.status(404).json({ error: 'Term not found' });
    res.json(item);
  } catch (err) { next(err); }
});

router.post('/terms', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const { name, startDate, endDate, year } = req.body;
    if (!name || !startDate || !endDate) {
      return res.status(400).json({ error: 'name, startDate, and endDate are required' });
    }
    const [item] = await knex('terms').insert({
      name, start_date: startDate, end_date: endDate,
      year: year || new Date().getFullYear(),
    }).returning('*');
    res.status(201).json(item);
  } catch (err) { next(err); }
});

router.put('/terms/:id', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const existing = await knex('terms').where({ id: req.params.id }).first();
    if (!existing) return res.status(404).json({ error: 'Term not found' });

    const { name, startDate, endDate, year } = req.body;
    const updates = {};
    if (name !== undefined) updates.name = name;
    if (startDate !== undefined) updates.start_date = startDate;
    if (endDate !== undefined) updates.end_date = endDate;
    if (year !== undefined) updates.year = year;

    const [item] = await knex('terms').where({ id: req.params.id }).update(updates).returning('*');
    res.json(item);
  } catch (err) { next(err); }
});

router.delete('/terms/:id', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const existing = await knex('terms').where({ id: req.params.id }).first();
    if (!existing) return res.status(404).json({ error: 'Term not found' });
    await knex('terms').where({ id: req.params.id }).del();
    res.json({ message: 'Deleted' });
  } catch (err) { next(err); }
});

// ─── Syllabus CRUD (revised — term-based, day_of_week, extracurricular) ──

const VALID_AREAS = ['practical_life', 'sensorial', 'language', 'math', 'cultural', 'extracurricular'];

router.get('/syllabus', authenticate, requireRole('admin', 'teacher'), async (req, res, next) => {
  try {
    let query = knex('syllabus')
      .leftJoin('classrooms', 'syllabus.classroom_id', 'classrooms.id')
      .leftJoin('terms', 'syllabus.term_id', 'terms.id')
      .select('syllabus.*', 'classrooms.name as classroom_name', 'terms.name as term_name')
      .orderBy(['terms.start_date', 'syllabus.week_number', 'syllabus.day_of_week', 'syllabus.sort_order']);

    if (req.query.classroomId) query = query.where('syllabus.classroom_id', req.query.classroomId);
    if (req.query.termId) query = query.where('syllabus.term_id', req.query.termId);
    if (req.query.montessoriArea) query = query.where('syllabus.montessori_area', req.query.montessoriArea);
    if (req.query.weekNumber) query = query.where('syllabus.week_number', parseInt(req.query.weekNumber));
    if (req.query.dayOfWeek) query = query.where('syllabus.day_of_week', parseInt(req.query.dayOfWeek));
    if (req.query.isExtracurricular !== undefined) query = query.where('syllabus.is_extracurricular', req.query.isExtracurricular === 'true');

    const items = await query;
    res.json(items);
  } catch (err) { next(err); }
});

router.get('/syllabus/:id', authenticate, requireRole('admin', 'teacher'), async (req, res, next) => {
  try {
    const item = await knex('syllabus')
      .leftJoin('classrooms', 'syllabus.classroom_id', 'classrooms.id')
      .leftJoin('terms', 'syllabus.term_id', 'terms.id')
      .select('syllabus.*', 'classrooms.name as classroom_name', 'terms.name as term_name')
      .where('syllabus.id', req.params.id).first();
    if (!item) return res.status(404).json({ error: 'Syllabus item not found' });
    res.json(item);
  } catch (err) { next(err); }
});

router.post('/syllabus', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const { termId, classroomId, montessoriArea, title, description, dayOfWeek, weekNumber, sortOrder, isExtracurricular, activityType, durationMinutes } = req.body;

    if (!termId || !classroomId || !montessoriArea || !title) {
      return res.status(400).json({ error: 'termId, classroomId, montessoriArea, and title are required' });
    }
    if (!VALID_AREAS.includes(montessoriArea)) {
      return res.status(400).json({ error: `montessoriArea must be one of: ${VALID_AREAS.join(', ')}` });
    }

    const [existing] = await knex('classrooms').where({ id: classroomId });
    if (!existing) return res.status(404).json({ error: 'Classroom not found' });

    const [item] = await knex('syllabus').insert({
      term_id: termId,
      classroom_id: classroomId,
      montessori_area: montessoriArea,
      title,
      description: description || '',
      day_of_week: dayOfWeek || 1,
      week_number: weekNumber || null,
      sort_order: sortOrder || 0,
      is_extracurricular: isExtracurricular || false,
      activity_type: activityType || null,
      duration_minutes: durationMinutes || null,
    }).returning('*');

    res.status(201).json(item);
  } catch (err) { next(err); }
});

router.put('/syllabus/:id', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const existing = await knex('syllabus').where({ id: req.params.id }).first();
    if (!existing) return res.status(404).json({ error: 'Syllabus item not found' });

    const { termId, classroomId, montessoriArea, title, description, dayOfWeek, weekNumber, sortOrder, isExtracurricular, activityType, durationMinutes } = req.body;

    if (montessoriArea && !VALID_AREAS.includes(montessoriArea)) {
      return res.status(400).json({ error: `montessoriArea must be one of: ${VALID_AREAS.join(', ')}` });
    }

    const updates = { updated_at: knex.fn.now() };
    if (termId !== undefined) updates.term_id = termId;
    if (classroomId !== undefined) updates.classroom_id = classroomId;
    if (montessoriArea !== undefined) updates.montessori_area = montessoriArea;
    if (title !== undefined) updates.title = title;
    if (description !== undefined) updates.description = description;
    if (dayOfWeek !== undefined) updates.day_of_week = dayOfWeek;
    if (weekNumber !== undefined) updates.week_number = weekNumber;
    if (sortOrder !== undefined) updates.sort_order = sortOrder;
    if (isExtracurricular !== undefined) updates.is_extracurricular = isExtracurricular;
    if (activityType !== undefined) updates.activity_type = activityType;
    if (durationMinutes !== undefined) updates.duration_minutes = durationMinutes;

    const [item] = await knex('syllabus').where({ id: req.params.id }).update(updates).returning('*');
    res.json(item);
  } catch (err) { next(err); }
});

router.delete('/syllabus/:id', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const existing = await knex('syllabus').where({ id: req.params.id }).first();
    if (!existing) return res.status(404).json({ error: 'Syllabus item not found' });
    await knex('syllabus').where({ id: req.params.id }).del();
    res.json({ message: 'Deleted' });
  } catch (err) { next(err); }
});

module.exports = router;
