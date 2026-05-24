const pino = require('pino')();

async function sendWhatsApp(to, message) {
  pino.info({ to, message }, 'WhatsApp message would be sent (stub)');
}

module.exports = { sendWhatsApp };
