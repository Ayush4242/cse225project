const express = require('express');
const router = express.Router();
const holidayController = require('../controllers/holidayController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);

router
    .route('/')
    .get(holidayController.getHolidays)
    .post(authorize('admin'), holidayController.createHoliday);

router
    .route('/:id')
    .delete(authorize('admin'), holidayController.deleteHoliday);

module.exports = router;
