const express = require('express');
const router = express.Router();
const timetableController = require('../controllers/timetableController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);

// Admin adds slots, others can view
router.post('/:sectionId/slots', authorize('admin'), timetableController.addSlot);
router.get('/:sectionId', timetableController.getTimetable);
router.delete('/:sectionId/slots/:slotId', authorize('admin'), timetableController.removeSlot);

module.exports = router;
