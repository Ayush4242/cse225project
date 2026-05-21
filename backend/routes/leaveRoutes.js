const express = require('express');
const router = express.Router();
const leaveController = require('../controllers/leaveController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.post('/', protect, authorize('student', 'teacher'), leaveController.createLeave);
router.get('/my', protect, authorize('student', 'teacher'), leaveController.getMyLeaves);

// Admin routes
router.get('/pending', protect, authorize('admin'), leaveController.getAllPendingLeaves);
router.get('/all', protect, authorize('admin'), leaveController.getAllLeaves);
router.put('/:id/status', protect, authorize('admin'), leaveController.updateLeaveStatus);

module.exports = router;
