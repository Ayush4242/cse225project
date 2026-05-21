const express = require('express');
const router = express.Router();
const attendanceController = require('../controllers/attendanceController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);

// Student route to mark attendance
router.post('/mark', authorize('student'), attendanceController.markAttendance);
router.get('/my-analytics', authorize('student'), attendanceController.getStudentAnalytics);

// Teacher routes for attendance data
router.get('/session/:sessionId', authorize('teacher'), attendanceController.getSessionAttendance);
router.get('/analytics/section/:sectionId', authorize('teacher'), attendanceController.getSectionAnalytics);

module.exports = router;
