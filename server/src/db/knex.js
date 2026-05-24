const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../../.env') });

const config = {
  client: 'pg',
  connection: {
    connectionString: process.env.DATABASE_URL,
    password: process.env.DB_PASSWORD,
    ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false,
  },
  migrations: {
    directory: './src/db/migrations',
    extension: 'js',
  },
};

const knex = require('knex')(config);

module.exports = knex;
