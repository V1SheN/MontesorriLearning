const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, async (req, res, next) => {
  try {
    let query = knex('children')
      .select('children.*', 'classrooms.name as classroom_name')
      .leftJoin('classrooms', 'children.classroom_id', 'classrooms.id')
      .orderBy('children.name');

    if (req.user.role === 'teacher') {
      const classroomIds = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id })
        .pluck('classroom_id');
      query = query.whereIn('children.classroom_id', classroomIds);
    } else if (req.user.role === 'parent') {
      const childIds = await knex('child_parents')
        .where({ parent_id: req.user.id })
        .pluck('child_id');
      query = query.whereIn('children.id', childIds);
    }

    const children = await query;
    res.json(children);
  } catch (err) {
    next(err);
  }
});

router.post('/', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const { name, dateOfBirth, classroomId, photoPath } = req.body;

    if (!name || !classroomId) {
      return res.status(400).json({ error: 'name and classroomId are required' });
    }

    if (req.user.role === 'teacher') {
      const [assignment] = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id, classroom_id: classroomId });
      if (!assignment) {
        return res.status(403).json({ error: 'You are not assigned to this classroom' });
      }
    }

    const [child] = await knex('children')
      .insert({
        name,
        date_of_birth: dateOfBirth,
        classroom_id: classroomId,
        photo_path: photoPath,
      })
      .returning('*');

    res.status(201).json(child);
  } catch (err) {
    next(err);
  }
});

router.get('/:id', authenticate, async (req, res, next) => {
  try {
    const child = await knex('children')
      .select('children.*', 'classrooms.name as classroom_name')
      .leftJoin('classrooms', 'children.classroom_id', 'classrooms.id')
      .where('children.id', req.params.id)
      .first();

    if (!child) {
      return res.status(404).json({ error: 'Child not found' });
    }

    if (req.user.role === 'teacher') {
      const [assignment] = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id, classroom_id: child.classroom_id });
      if (!assignment) {
        return res.status(403).json({ error: 'Access denied' });
      }
    } else if (req.user.role === 'parent') {
      const [relation] = await knex('child_parents')
        .where({ parent_id: req.user.id, child_id: child.id });
      if (!relation) {
        return res.status(403).json({ error: 'Access denied' });
      }
    }

    res.json(child);
  } catch (err) {
    next(err);
  }
});

router.put('/:id', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const existing = await knex('children').where({ id: req.params.id }).first();
    if (!existing) {
      return res.status(404).json({ error: 'Child not found' });
    }

    if (req.user.role === 'teacher') {
      const [assignment] = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id, classroom_id: existing.classroom_id });
      if (!assignment) {
        return res.status(403).json({ error: 'You are not assigned to this classroom' });
      }
    }

    const { name, dateOfBirth, classroomId, photoPath, active } = req.body;
    const updates = {};
    if (name !== undefined) updates.name = name;
    if (dateOfBirth !== undefined) updates.date_of_birth = dateOfBirth;
    if (classroomId !== undefined) {
      if (req.user.role === 'teacher') {
        const [assignment] = await knex('classroom_teachers')
          .where({ teacher_id: req.user.id, classroom_id: classroomId });
        if (!assignment) {
          return res.status(403).json({ error: 'You are not assigned to this classroom' });
        }
      }
      updates.classroom_id = classroomId;
    }
    if (photoPath !== undefined) updates.photo_path = photoPath;
    if (active !== undefined) updates.active = active;

    const [child] = await knex('children')
      .where({ id: req.params.id })
      .update(updates)
      .returning('*');

    res.json(child);
  } catch (err) {
    next(err);
  }
});

module.exports = router;
