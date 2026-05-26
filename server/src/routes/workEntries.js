const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');
const { sendPush } = require('../services/push');

const router = express.Router();

router.get('/', authenticate, async (req, res, next) => {
  try {
    const { childId, date, classroomId } = req.query;

    let query = knex('work_entries as e')
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
          ) ORDER BY m.sort_order, m.created_at) FILTER (WHERE m.id IS NOT NULL), '[]'::json) as media`
        )
      )
      .leftJoin('media as m', 'e.id', 'm.entry_id')
      .whereNull('e.deleted_at')
      .groupBy('e.id')
      .orderBy('e.created_at', 'desc');

    if (childId) {
      query = query.where('e.child_id', childId);
    }

    if (date) {
      query = query.whereRaw('e.created_at::date = ?', [date]);
    }

    if (req.user.role === 'teacher') {
      const classroomIds = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id })
        .pluck('classroom_id');
      query = query.whereIn('e.classroom_id', classroomIds);
    } else if (req.user.role === 'parent') {
      const childIds = await knex('child_parents')
        .where({ parent_id: req.user.id })
        .pluck('child_id');
      query = query.whereIn('e.child_id', childIds);
    }

    if (classroomId && req.user.role === 'admin') {
      query = query.where('e.classroom_id', classroomId);
    }

    const entries = await query;
    res.json(entries);
  } catch (err) {
    next(err);
  }
});

router.post('/', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const { childId, title, montessoriArea, teacherComment, media } = req.body;

    if (!childId || !montessoriArea) {
      return res.status(400).json({ error: 'childId and montessoriArea are required' });
    }

    const child = await knex('children').where({ id: childId }).first();
    if (!child) {
      return res.status(404).json({ error: 'Child not found' });
    }

    if (req.user.role === 'teacher') {
      const [assignment] = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id, classroom_id: child.classroom_id });
      if (!assignment) {
        return res.status(403).json({ error: 'You are not assigned to this child classroom' });
      }
    }

    const entry = await knex.transaction(async (trx) => {
      const [entry] = await trx('work_entries')
        .insert({
          child_id: childId,
          teacher_id: req.user.id,
          classroom_id: child.classroom_id,
          montessori_area: montessoriArea,
          title: title || '',
          teacher_comment: teacherComment || '',
        })
        .returning('*');

      if (media && Array.isArray(media) && media.length > 0) {
        const mediaRows = media.map((m, i) => ({
          entry_id: entry.id,
          media_type: 'image',
          storage_key: m.storageKey,
          thumbnail_key: m.thumbnailKey || null,
          is_cover: m.isCover || i === 0,
          caption: m.caption || null,
          sort_order: m.sortOrder || i,
        }));
        await trx('media').insert(mediaRows);
      }

      return entry;
    });

    const entryWithMedia = await knex('work_entries as e')
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
      .where('e.id', entry.id)
      .groupBy('e.id')
      .first();

    const parents = await knex('child_parents')
      .where({ child_id: childId })
      .pluck('parent_id');

    const io = req.app.get('io');
    for (const parentId of parents) {
      if (io) {
        io.to(`parent:${parentId}`).emit('new_entry', {
          childName: child.name,
          title: entry.title,
          thumbnailUrl: entryWithMedia.media.length > 0 ? entryWithMedia.media[0].thumbnailKey : null,
          teacherComment: entry.teacher_comment,
          entryId: entry.id,
          createdAt: entry.created_at,
        });
      }
      await sendPush(`parent:${parentId}`, child.name, entry.title || 'New work entry', [
        'camera',
      ]);
    }

    res.status(201).json(entryWithMedia);
  } catch (err) {
    next(err);
  }
});

router.get('/:id', authenticate, async (req, res, next) => {
  try {
    const entry = await knex('work_entries as e')
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
      .where('e.id', req.params.id)
      .whereNull('e.deleted_at')
      .groupBy('e.id')
      .first();

    if (!entry) {
      return res.status(404).json({ error: 'Work entry not found' });
    }

    if (req.user.role === 'teacher') {
      const [assignment] = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id, classroom_id: entry.classroom_id });
      if (!assignment) {
        return res.status(403).json({ error: 'Access denied' });
      }
    } else if (req.user.role === 'parent') {
      const [relation] = await knex('child_parents')
        .where({ parent_id: req.user.id, child_id: entry.child_id });
      if (!relation) {
        return res.status(403).json({ error: 'Access denied' });
      }
    }

    res.json(entry);
  } catch (err) {
    next(err);
  }
});

router.delete('/:id', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const entry = await knex('work_entries').where({ id: req.params.id }).first();
    if (!entry) {
      return res.status(404).json({ error: 'Work entry not found' });
    }

    if (req.user.role === 'teacher') {
      const [assignment] = await knex('classroom_teachers')
        .where({ teacher_id: req.user.id, classroom_id: entry.classroom_id });
      if (!assignment) {
        return res.status(403).json({ error: 'Access denied' });
      }
    }

    const [deleted] = await knex('work_entries')
      .where({ id: req.params.id })
      .update({ deleted_at: knex.fn.now() })
      .returning('*');

    res.json(deleted);
  } catch (err) {
    next(err);
  }
});

module.exports = router;
