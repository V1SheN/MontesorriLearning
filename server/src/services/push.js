const pino = require('pino')();
const http = require('http');
const https = require('https');

async function sendPush(topic, title, message, tags = []) {
  const ntfyUrl = process.env.NTFY_URL || 'http://localhost:80';
  const url = `${ntfyUrl}/${topic}`;

  const body = JSON.stringify({
    topic,
    title,
    message,
    tags,
    priority: 4,
  });

  try {
    const client = url.startsWith('https') ? https : http;
    await new Promise((resolve, reject) => {
      const req = client.request(
        url,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(body),
          },
        },
        (res) => {
          let data = '';
          res.on('data', (chunk) => (data += chunk));
          res.on('end', () => {
            if (res.statusCode >= 200 && res.statusCode < 300) {
              resolve(data);
            } else {
              reject(new Error(`ntfy returned ${res.statusCode}: ${data}`));
            }
          });
        }
      );
      req.on('error', reject);
      req.write(body);
      req.end();
    });
    pino.info({ topic, title }, 'Push notification sent');
  } catch (err) {
    pino.error({ err, topic }, 'Failed to send push notification');
  }
}

module.exports = { sendPush };
