const express = require('express');
const multer = require('multer');
const Minio = require('minio');
const { v4: uuidv4 } = require('uuid');
const pino = require('pino')();
const knex = require('../db/knex');
const { authenticate, requireRole } = require('../middleware/auth');
const { compressImage, generateThumbnail } = require('../services/image');

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: 20 * 1024 * 1024 } });

const minioClient = new Minio.Client({
  endPoint: (process.env.MINIO_ENDPOINT || 'localhost:9000').split(':')[0],
  port: parseInt((process.env.MINIO_ENDPOINT || 'localhost:9000').split(':')[1] || '9000', 10),
  useSSL: false,
  accessKey: process.env.MINIO_ACCESS_KEY || 'minioadmin',
  secretKey: process.env.MINIO_SECRET_KEY || 'minio_dev_password',
});

async function initBuckets() {
  const buckets = ['photos', 'thumbnails', 'avatars'];
  for (const bucket of buckets) {
    const exists = await minioClient.bucketExists(bucket);
    if (!exists) {
      await minioClient.makeBucket(bucket);
      pino.info({ bucket }, 'Created MinIO bucket');
    }
  }
}

router.post('/upload', authenticate, requireRole('teacher', 'admin'), upload.single('file'), async (req, res, next) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'File is required' });
    }

    const { childId, isCover, caption, sortOrder } = req.body;

    if (!childId) {
      return res.status(400).json({ error: 'childId is required' });
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

    const today = new Date().toISOString().split('T')[0];
    const [countResult] = await knex('media as m')
      .join('work_entries as e', 'm.entry_id', 'e.id')
      .where('e.child_id', childId)
      .whereRaw('m.created_at::date = ?', [today])
      .count();
    const count = parseInt(countResult.count, 10);
    const max = 50;

    if (count >= max && !req.headers['x-override-limit']) {
      return res.status(429).json({ status: 'limit_reached', count, max });
    }

    const { buffer: compressed, width, height } = await compressImage(req.file.buffer);
    const thumbnailBuffer = await generateThumbnail(req.file.buffer);

    const ext = 'jpg';
    const fileUuid = uuidv4();
    const datePath = today.replace(/-/g, '');
    const storageKey = `${datePath}/${childId}/${fileUuid}.${ext}`;
    const thumbnailKey = `${datePath}/${childId}/${fileUuid}_thumb.${ext}`;

    await Promise.all([
      minioClient.putObject('photos', storageKey, compressed, compressed.length, {
        'Content-Type': 'image/jpeg',
      }),
      minioClient.putObject('thumbnails', thumbnailKey, thumbnailBuffer, thumbnailBuffer.length, {
        'Content-Type': 'image/jpeg',
      }),
    ]);

    res.set('X-Daily-Count', String(count + 1));
    res.set('X-Daily-Max', String(max));
    res.json({ storageKey, thumbnailKey, width, height, fileSize: compressed.length });
  } catch (err) {
    next(err);
  }
});

router.get('/daily-count/:childId', authenticate, async (req, res, next) => {
  try {
    const { childId } = req.params;
    const today = new Date().toISOString().split('T')[0];

    const child = await knex('children').where({ id: childId }).first();
    if (!child) {
      return res.status(404).json({ error: 'Child not found' });
    }

    const [countResult] = await knex('media as m')
      .join('work_entries as e', 'm.entry_id', 'e.id')
      .where('e.child_id', childId)
      .whereRaw('m.created_at::date = ?', [today])
      .count();

    const count = parseInt(countResult.count, 10);

    res.json({ childId, date: today, count, max: 50 });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
module.exports.initBuckets = initBuckets;
