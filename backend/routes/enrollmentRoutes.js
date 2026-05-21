const express = require('express');
const router = express.Router();
const enrollmentController = require('../controllers/enrollmentController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);

router
    .route('/')
    .get(authorize('admin', 'teacher'), enrollmentController.getEnrollments)
    .post(authorize('admin'), enrollmentController.createEnrollment);

router
    .route('/:id')
    .delete(authorize('admin'), enrollmentController.deleteEnrollment);

module.exports = router;
