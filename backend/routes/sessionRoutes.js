const express = require('express');
const router = express.Router();
const sessionController = require('../controllers/sessionController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);

// Teacher routes for session management
router.post('/start', authorize('teacher'), sessionController.startSession);
router.post('/:sessionId/refresh', authorize('teacher'), sessionController.refreshQr);
router.post('/:sessionId/end', authorize('teacher'), sessionController.endSession);
router.get('/active', authorize('teacher'), sessionController.getActiveSession);
router.get('/student/active', authorize('student'), sessionController.getActiveSessionsForStudent);

module.exports = router;
