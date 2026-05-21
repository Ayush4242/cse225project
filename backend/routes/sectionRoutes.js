const express = require('express');
const router = express.Router();
const sectionController = require('../controllers/sectionController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);
router.use(authorize('admin'));

router
    .route('/')
    .get(sectionController.getSections)
    .post(sectionController.createSection);

router
    .route('/:id')
    .get(sectionController.getSection)
    .put(sectionController.updateSection)
    .delete(sectionController.deleteSection);

module.exports = router;
