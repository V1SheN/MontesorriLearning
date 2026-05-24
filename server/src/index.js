require('dotenv').config();

const express = require('express');
const http = require('http');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const pino = require('pino')();
const { initSocket } = require('./websocket');
const { initBuckets } = require('./routes/upload');

const authRoutes = require('./routes/auth');
const childrenRoutes = require('./routes/children');
const workEntriesRoutes = require('./routes/workEntries');
const uploadRoutes = require('./routes/upload');
const messagesRoutes = require('./routes/messages');
const classroomsRoutes = require('./routes/classrooms');
const dailySummaryRoutes = require('./routes/dailySummary');

const app = express();

app.use(cors());
app.use(express.json());

const limiter = rateLimit({
  windowMs: 60 * 1000,
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
});
app.use(limiter);

app.use((req, res, next) => {
  pino.info({ method: req.method, url: req.url }, 'Request');
  next();
});

app.use('/api/auth', authRoutes);
app.use('/api/children', childrenRoutes);
app.use('/api/work-entries', workEntriesRoutes);
app.use('/api', uploadRoutes);
app.use('/api/messages', messagesRoutes);
app.use('/api/classrooms', classroomsRoutes);
app.use('/api/daily-summary', dailySummaryRoutes);

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.use((err, req, res, next) => {
  pino.error({ err }, 'Unhandled error');

  if (err.code === '23505') {
    return res.status(409).json({ error: 'Resource already exists' });
  }
  if (err.code === '23503') {
    return res.status(400).json({ error: 'Referenced resource not found' });
  }
  if (err.code === '23514') {
    return res.status(400).json({ error: 'Invalid value for constraint' });
  }
  if (err.code === 'LIMIT_FILE_SIZE') {
    return res.status(413).json({ error: 'File too large' });
  }

  res.status(err.status || 500).json({
    error: process.env.NODE_ENV === 'production' ? 'Internal server error' : err.message,
  });
});

const PORT = process.env.PORT || 3000;
const server = http.createServer(app);

initBuckets()
  .then(() => {
    const io = initSocket(server);
    app.set('io', io);

    server.listen(PORT, () => {
      pino.info({ port: PORT }, 'Server started');
    });
  })
  .catch((err) => {
    pino.error({ err }, 'Failed to initialize MinIO buckets');
  });
