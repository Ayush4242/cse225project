const express = require('express');
const router = express.Router();
const notificationController = require('../controllers/notificationController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);

router
    .route('/')
    .get(notificationController.getNotifications)
    .post(authorize('admin'), notificationController.createNotification);

router
    .route('/:id')
    .delete(authorize('admin'), notificationController.deleteNotification);

module.exports = router;
