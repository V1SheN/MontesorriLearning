const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../../.env') });

const conn = { connectionString: process.env.DATABASE_URL };
if (process.env.DB_PASSWORD) conn.password = process.env.DB_PASSWORD;
conn.ssl = process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false;

const config = {
  client: 'pg',
  connection: conn,
  migrations: {
    directory: './src/db/migrations',
    extension: 'js',
  },
};

const knex = require('knex')(config);

module.exports = knex;
