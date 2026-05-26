const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');

const router = express.Router();

const VALID_AREAS = ['practical_life', 'sensorial', 'language', 'math', 'cultural', 'extracurricular'];

// ─── List teacher's plans (or by classroom) ───────────────────
router.get('/', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    let query = knex('teacher_plans')
      .leftJoin('classrooms', 'teacher_plans.classroom_id', 'classrooms.id')
      .leftJoin('terms', 'teacher_plans.term_id', 'terms.id')
      .select('teacher_plans.*', 'classrooms.name as classroom_name', 'terms.name as term_name')
      .orderBy('teacher_plans.planned_date');

    if (req.user.role === 'teacher') {
      query = query.where('teacher_plans.teacher_id', req.user.id);
    } else if (req.query.teacherId) {
      query = query.where('teacher_plans.teacher_id', req.query.teacherId);
    }
    if (req.query.classroomId) {
      query = query.where('teacher_plans.classroom_id', req.query.classroomId);
    }
    if (req.query.termId) {
      query = query.where('teacher_plans.term_id', req.query.termId);
    }
    if (req.query.fromDate) {
      query = query.where('teacher_plans.planned_date', '>=', req.query.fromDate);
    }
    if (req.query.toDate) {
      query = query.where('teacher_plans.planned_date', '<=', req.query.toDate);
    }

    const items = await query;
    res.json(items);
  } catch (err) { next(err); }
});

// ─── Get single plan ─────────────────────────────────────────
router.get('/:id', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const item = await knex('teacher_plans')
      .leftJoin('classrooms', 'teacher_plans.classroom_id', 'classrooms.id')
      .leftJoin('terms', 'teacher_plans.term_id', 'terms.id')
      .select('teacher_plans.*', 'classrooms.name as classroom_name', 'terms.name as term_name')
      .where('teacher_plans.id', req.params.id).first();
    if (!item) return res.status(404).json({ error: 'Plan not found' });
    res.json(item);
  } catch (err) { next(err); }
});

// ─── Create plan (from syllabus or custom) ────────────────────
router.post('/', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const { syllabusId, termId, classroomId, title, montessoriArea, description,
            plannedDate, dayOfWeek, weekNumber, isExtracurricular, activityType,
            durationMinutes, teacherNotes } = req.body;

    if (!termId || !classroomId || !title || !montessoriArea || !plannedDate) {
      return res.status(400).json({ error: 'termId, classroomId, title, montessoriArea, and plannedDate are required' });
    }
    if (!VALID_AREAS.includes(montessoriArea)) {
      return res.status(400).json({ error: `montessoriArea must be one of: ${VALID_AREAS.join(', ')}` });
    }

    const dt = new Date(plannedDate);
    const dow = dayOfWeek || (dt.getDay() === 0 ? 7 : dt.getDay());

    const [item] = await knex('teacher_plans').insert({
      syllabus_id: syllabusId || null,
      teacher_id: req.user.id,
      term_id: termId,
      classroom_id: classroomId,
      title,
      montessori_area: montessoriArea,
      description: description || '',
      planned_date: plannedDate,
      day_of_week: dow,
      week_number: weekNumber || null,
      is_extracurricular: isExtracurricular || false,
      activity_type: activityType || null,
      duration_minutes: durationMinutes || null,
      teacher_notes: teacherNotes || null,
    }).returning('*');

    res.status(201).json(item);
  } catch (err) { next(err); }
});

// ─── Update plan ──────────────────────────────────────────────
router.put('/:id', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const existing = await knex('teacher_plans').where({ id: req.params.id }).first();
    if (!existing) return res.status(404).json({ error: 'Plan not found' });
    if (req.user.role === 'teacher' && existing.teacher_id !== req.user.id) {
      return res.status(403).json({ error: 'Access denied' });
    }

    const { syllabusId, termId, title, montessoriArea, description,
            plannedDate, dayOfWeek, weekNumber, isExtracurricular, activityType,
            durationMinutes, isCompleted, teacherNotes } = req.body;

    if (montessoriArea && !VALID_AREAS.includes(montessoriArea)) {
      return res.status(400).json({ error: `montessoriArea must be one of: ${VALID_AREAS.join(', ')}` });
    }

    const updates = { updated_at: knex.fn.now() };
    if (syllabusId !== undefined) updates.syllabus_id = syllabusId;
    if (termId !== undefined) updates.term_id = termId;
    if (title !== undefined) updates.title = title;
    if (montessoriArea !== undefined) updates.montessori_area = montessoriArea;
    if (description !== undefined) updates.description = description;
    if (plannedDate !== undefined) updates.planned_date = plannedDate;
    if (dayOfWeek !== undefined) updates.day_of_week = dayOfWeek;
    if (weekNumber !== undefined) updates.week_number = weekNumber;
    if (isExtracurricular !== undefined) updates.is_extracurricular = isExtracurricular;
    if (activityType !== undefined) updates.activity_type = activityType;
    if (durationMinutes !== undefined) updates.duration_minutes = durationMinutes;
    if (isCompleted !== undefined) updates.is_completed = isCompleted;
    if (teacherNotes !== undefined) updates.teacher_notes = teacherNotes;

    const [item] = await knex('teacher_plans').where({ id: req.params.id }).update(updates).returning('*');
    res.json(item);
  } catch (err) { next(err); }
});

// ─── Delete plan ──────────────────────────────────────────────
router.delete('/:id', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const existing = await knex('teacher_plans').where({ id: req.params.id }).first();
    if (!existing) return res.status(404).json({ error: 'Plan not found' });
    if (req.user.role === 'teacher' && existing.teacher_id !== req.user.id) {
      return res.status(403).json({ error: 'Access denied' });
    }
    await knex('teacher_plans').where({ id: req.params.id }).del();
    res.json({ message: 'Deleted' });
  } catch (err) { next(err); }
});

module.exports = router;
