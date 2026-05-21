const express = require('express');
const router = express.Router();
const departmentController = require('../controllers/departmentController');
const { protect, authorize } = require('../middleware/authMiddleware');
// Assuming we'll use express-validator or similar for validation
// const { departmentValidator } = require('../validators/departmentValidator');

router.use(protect);
router.use(authorize('admin'));

router
    .route('/')
    .get(departmentController.getDepartments)
    .post(departmentController.createDepartment);

router
    .route('/:id')
    .get(departmentController.getDepartment)
    .put(departmentController.updateDepartment)
    .delete(departmentController.deleteDepartment);

module.exports = router;
