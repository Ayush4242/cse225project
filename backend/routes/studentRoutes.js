const express = require('express');
const router = express.Router();
const studentController = require('../controllers/studentController');
const { protect, authorize } = require('../middleware/authMiddleware');
const upload = require('../utils/multer');

router.use(protect);
router.use(authorize('admin'));

router
    .route('/')
    .get(studentController.getStudents)
    .post(studentController.createStudent);

router.post('/bulk-upload', upload.single('file'), studentController.bulkUpload);

router
    .route('/:id')
    .get(studentController.getStudent)
    .put(studentController.updateStudent)
    .delete(studentController.deleteStudent);

module.exports = router;
