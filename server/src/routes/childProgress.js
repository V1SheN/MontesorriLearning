const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');

const router = express.Router();

const VALID_STATUSES = ['pending', 'in_progress', 'completed', 'mastered'];

// ─── List progress (by child, syllabus, term, classroom) ─────
router.get('/', authenticate, requireRole('teacher', 'admin', 'parent'), async (req, res, next) => {
  try {
    let query = knex('child_progress')
      .leftJoin('children', 'child_progress.child_id', 'children.id')
      .leftJoin('syllabus', 'child_progress.syllabus_id', 'syllabus.id')
      .leftJoin('teacher_plans', 'child_progress.teacher_plan_id', 'teacher_plans.id')
      .select(
        'child_progress.*',
        'children.name as child_name',
        'syllabus.title as syllabus_title',
        'syllabus.montessori_area as syllabus_area',
        'teacher_plans.title as plan_title'
      )
      .orderBy(['children.name', 'child_progress.updated_at']);

    if (req.query.childId) {
      query = query.where('child_progress.child_id', req.query.childId);
    }
    if (req.query.status) {
      query = query.where('child_progress.status', req.query.status);
    }
    if (req.query.syllabusId) {
      query = query.where('child_progress.syllabus_id', req.query.syllabusId);
    }

    // Parent sees only their children
    if (req.user.role === 'parent') {
      const childIds = await knex('child_parents').where({ parent_id: req.user.id }).pluck('child_id');
      query = query.whereIn('child_progress.child_id', childIds);
    }

    const items = await query;
    res.json(items);
  } catch (err) { next(err); }
});

// ─── Get single progress record ──────────────────────────────
router.get('/:id', authenticate, requireRole('teacher', 'admin', 'parent'), async (req, res, next) => {
  try {
    const item = await knex('child_progress')
      .leftJoin('children', 'child_progress.child_id', 'children.id')
      .select('child_progress.*', 'children.name as child_name')
      .where('child_progress.id', req.params.id).first();
    if (!item) return res.status(404).json({ error: 'Progress record not found' });
    res.json(item);
  } catch (err) { next(err); }
});

// ─── Create or update progress ───────────────────────────────
router.post('/', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const { childId, syllabusId, teacherPlanId, status, observationNotes } = req.body;

    if (!childId) return res.status(400).json({ error: 'childId is required' });
    if (!syllabusId && !teacherPlanId) {
      return res.status(400).json({ error: 'Either syllabusId or teacherPlanId is required' });
    }
    if (status && !VALID_STATUSES.includes(status)) {
      return res.status(400).json({ error: `status must be one of: ${VALID_STATUSES.join(', ')}` });
    }

    // Upsert: find existing record for this child + syllabus/plan
    let existing = null;
    if (syllabusId) {
      existing = await knex('child_progress').where({ child_id: childId, syllabus_id: syllabusId }).first();
    } else if (teacherPlanId) {
      existing = await knex('child_progress').where({ child_id: childId, teacher_plan_id: teacherPlanId }).first();
    }

    if (existing) {
      const updates = { updated_at: knex.fn.now() };
      if (status !== undefined) updates.status = status;
      if (observationNotes !== undefined) updates.observation_notes = observationNotes;
      if (status === 'completed' || status === 'mastered') updates.completed_at = knex.fn.now();

      const [item] = await knex('child_progress').where({ id: existing.id }).update(updates).returning('*');
      return res.json(item);
    }

    const [item] = await knex('child_progress').insert({
      child_id: childId,
      syllabus_id: syllabusId || null,
      teacher_plan_id: teacherPlanId || null,
      status: status || 'pending',
      observation_notes: observationNotes || null,
      completed_at: (status === 'completed' || status === 'mastered') ? knex.fn.now() : null,
    }).returning('*');

    res.status(201).json(item);
  } catch (err) { next(err); }
});

// ─── Delete progress ─────────────────────────────────────────
router.delete('/:id', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    const existing = await knex('child_progress').where({ id: req.params.id }).first();
    if (!existing) return res.status(404).json({ error: 'Progress record not found' });
    await knex('child_progress').where({ id: req.params.id }).del();
    res.json({ message: 'Deleted' });
  } catch (err) { next(err); }
});

module.exports = router;
