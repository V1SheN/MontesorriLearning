const express = require('express');
const { authenticate, requireRole } = require('../middleware/auth');

const router = express.Router();

router.get('/analytics', authenticate, requireRole('admin'), (req, res) => {
  res.status(501).json({ message: 'Not implemented', status: 'stub' });
});

module.exports = router;
