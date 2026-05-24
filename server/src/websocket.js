const { Server } = require('socket.io');
const jwt = require('jsonwebtoken');
const pino = require('pino')();

function initSocket(httpServer) {
  const io = new Server(httpServer, {
    cors: {
      origin: '*',
      methods: ['GET', 'POST'],
    },
  });

  io.use((socket, next) => {
    const token = socket.handshake.auth.token;
    if (!token) {
      return next(new Error('Authentication required'));
    }
    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      socket.user = { id: decoded.sub, role: decoded.role };
      next();
    } catch (err) {
      next(new Error('Invalid token'));
    }
  });

  io.on('connection', async (socket) => {
    pino.info({ userId: socket.user.id, role: socket.user.role }, 'Socket connected');

    if (socket.user.role === 'parent') {
      socket.join(`parent:${socket.user.id}`);
    } else if (socket.user.role === 'teacher' || socket.user.role === 'admin') {
      const knex = require('./db/knex');
      try {
        const classrooms = await knex('classroom_teachers')
          .where({ teacher_id: socket.user.id })
          .select('classroom_id');
        classrooms.forEach((c) => socket.join(`classroom:${c.classroom_id}`));
      } catch (err) {
        pino.error({ err }, 'Failed to load classrooms for socket');
      }
    }

    socket.on('disconnect', () => {
      pino.info({ userId: socket.user.id }, 'Socket disconnected');
    });
  });

  return io;
}

module.exports = { initSocket };
