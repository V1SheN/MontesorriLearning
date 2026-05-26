const express = require('express');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');
const { sendPush } = require('../services/push');
const { sendEmail } = require('../services/email');

const router = express.Router();

router.post('/', authenticate, requireRole('teacher', 'admin'), async (req, res, next) => {
  try {
    const { recipientType, recipientIds, classroomId, subject, body, channels } = req.body;

    if (!body) {
      return res.status(400).json({ error: 'body is required' });
    }

    let resolvedRecipientIds = [];

    if (recipientType === 'individual' && recipientIds && Array.isArray(recipientIds)) {
      resolvedRecipientIds = recipientIds;
    } else if (recipientType === 'class' && classroomId) {
      const parentIds = await knex('children')
        .join('child_parents', 'children.id', 'child_parents.child_id')
        .where('children.classroom_id', classroomId)
        .where('children.active', true)
        .distinct('child_parents.parent_id')
        .pluck('parent_id');
      resolvedRecipientIds = parentIds;
    } else if (recipientType === 'all') {
      const parentIds = await knex('users')
        .where({ role: 'parent' })
        .pluck('id');
      resolvedRecipientIds = parentIds;
    } else {
      return res.status(400).json({ error: 'Invalid recipientType or missing parameters' });
    }

    if (resolvedRecipientIds.length === 0) {
      return res.status(400).json({ error: 'No recipients resolved' });
    }

    const message = await knex.transaction(async (trx) => {
      const [msg] = await trx('messages')
        .insert({
          sender_id: req.user.id,
          classroom_id: classroomId || null,
          subject: subject || null,
          body,
          channels: channels || ['push'],
        })
        .returning('*');

      const recipientRows = resolvedRecipientIds.map((userId) => ({
        message_id: msg.id,
        user_id: userId,
      }));
      await trx('message_recipients').insert(recipientRows);

      return msg;
    });

    const messageChannels = channels || ['push'];
    const io = req.app.get('io');
    for (const userId of resolvedRecipientIds) {
      if (messageChannels.includes('push')) {
        await sendPush(`parent:${userId}`, subject || 'New message', body);
      }
      if (messageChannels.includes('email')) {
        const user = await knex('users').where({ id: userId }).first();
        if (user && user.notif_prefs && user.notif_prefs.email) {
          await sendEmail(user.email, subject || 'New message', body);
        }
      }
      if (io) {
        io.to(`user:${userId}`).emit('new_message', {
          messageId: message.id,
          senderId: req.user.id,
          subject: subject || null,
          body,
          createdAt: message.created_at,
        });
      }
    }

    res.status(201).json(message);
  } catch (err) {
    next(err);
  }
});

router.get('/', authenticate, async (req, res, next) => {
  try {
    const messages = await knex('messages as m')
      .select(
        'm.*',
        knex.raw("json_build_object('id', u.id, 'displayName', u.display_name) as sender"),
        knex.raw(
          `CASE WHEN mr.read_at IS NOT NULL THEN true ELSE false END as is_read`
        )
      )
      .join('users as u', 'm.sender_id', 'u.id')
      .leftJoin('message_recipients as mr', function () {
        this.on('m.id', 'mr.message_id').andOn('mr.user_id', knex.raw('?', [req.user.id]));
      })
      .where(function () {
        this.where('m.sender_id', req.user.id).orWhereIn('m.id', function () {
          this.select('message_id').from('message_recipients').where('user_id', req.user.id);
        });
      })
      .orderBy('m.created_at', 'desc');

    res.json(messages);
  } catch (err) {
    next(err);
  }
});

router.put('/:id/read', authenticate, async (req, res, next) => {
  try {
    const [updated] = await knex('message_recipients')
      .where({ message_id: req.params.id, user_id: req.user.id })
      .update({ read_at: knex.fn.now() })
      .returning('*');

    if (!updated) {
      return res.status(404).json({ error: 'Message not found or not addressed to you' });
    }

    res.json(updated);
  } catch (err) {
    next(err);
  }
});

module.exports = router;
