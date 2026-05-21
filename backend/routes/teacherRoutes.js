const express = require('express');
const router = express.Router();
const teacherController = require('../controllers/teacherController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);

router.get('/my-classes', authorize('teacher'), teacherController.getMyClasses);
router.get('/today-classes', authorize('teacher'), teacherController.getTodayClasses);
router.put('/update-profile', authorize('teacher'), teacherController.updateProfile);

router.use(authorize('admin'));

router
    .route('/')
    .get(teacherController.getTeachers)
    .post(teacherController.createTeacher);

router
    .route('/:id')
    .get(teacherController.getTeacher)
    .put(teacherController.updateTeacher)
    .delete(teacherController.deleteTeacher);

module.exports = router;
