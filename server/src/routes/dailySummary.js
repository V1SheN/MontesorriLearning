const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, async (req, res, next) => {
  try {
    const date = req.query.date || new Date().toISOString().split('T')[0];
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
    } else if (req.user.role === 'parent') {
      const parentChildIds = await knex('child_parents')
        .where({ parent_id: req.user.id })
        .pluck('child_id');
      const parentChildren = await knex('children')
        .whereIn('id', parentChildIds)
        .select('classroom_id');
      classroomIds = [...new Set(parentChildren.map((c) => c.classroom_id))];
    } else if (classroomId) {
      classroomIds = [classroomId];
    } else {
      const allClassrooms = await knex('classrooms').pluck('id');
      classroomIds = allClassrooms;
    }

    let childrenQuery = knex('children')
      .whereIn('classroom_id', classroomIds)
      .where('active', true)
      .orderBy('name');

    if (req.user.role === 'parent') {
      const parentChildIds = await knex('child_parents')
        .where({ parent_id: req.user.id })
        .pluck('child_id');
      childrenQuery = childrenQuery.whereIn('id', parentChildIds);
    }

    const children = await childrenQuery;
    const childIds = children.map((c) => c.id);

    if (childIds.length === 0) {
      return res.json({ date, children: [] });
    }

    const entries = await knex('work_entries as e')
      .select(
        'e.*',
        knex.raw(
          `COALESCE(json_agg(json_build_object(
            'id', m.id,
            'mediaType', m.media_type,
            'storageKey', m.storage_key,
            'thumbnailKey', m.thumbnail_key,
            'width', m.width,
            'height', m.height,
            'fileSize', m.file_size,
            'isCover', m.is_cover,
            'caption', m.caption,
            'sortOrder', m.sort_order,
            'createdAt', m.created_at
          ) ORDER BY m.sort_order) FILTER (WHERE m.id IS NOT NULL), '[]'::json) as media`
        )
      )
      .leftJoin('media as m', 'e.id', 'm.entry_id')
      .whereIn('e.child_id', childIds)
      .whereNull('e.deleted_at')
      .whereRaw('e.created_at::date = ?', [date])
      .groupBy('e.id')
      .orderBy('e.created_at');

    const entriesByChild = {};
    for (const entry of entries) {
      if (!entriesByChild[entry.child_id]) {
        entriesByChild[entry.child_id] = [];
      }
      entriesByChild[entry.child_id].push(entry);
    }

    const result = {
      date,
      children: children.map((child) => ({
        child,
        entries: entriesByChild[child.id] || [],
      })),
    };

    res.json(result);
  } catch (err) {
    next(err);
  }
});

module.exports = router;
