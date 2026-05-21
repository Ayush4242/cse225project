const express = require('express');
const router = express.Router();
const mappingController = require('../controllers/mappingController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.use(protect);
router.use(authorize('admin'));

router
    .route('/')
    .get(mappingController.getMappings)
    .post(mappingController.createMapping);

router
    .route('/:id')
    .delete(mappingController.deleteMapping);

module.exports = router;
