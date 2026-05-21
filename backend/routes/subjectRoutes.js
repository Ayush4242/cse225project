const express = require('express');
const router = express.Router();
const subjectController = require('../controllers/subjectController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);
router.use(authorize('admin'));

router
    .route('/')
    .get(subjectController.getSubjects)
    .post(subjectController.createSubject);

router
    .route('/:id')
    .get(subjectController.getSubject)
    .put(subjectController.updateSubject)
    .delete(subjectController.deleteSubject);

module.exports = router;
