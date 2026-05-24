const pino = require('pino')();

async function sendEmail(to, subject, body) {
  pino.info({ to, subject }, 'Email would be sent (stub)');
}

module.exports = { sendEmail };
