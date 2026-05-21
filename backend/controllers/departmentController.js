const departmentService = require('../services/departmentService');
const ApiError = require('../utils/apiError');

/**
 * Controller for Department operations
 */
class DepartmentController {
    /**
     * Create a department
     */
    createDepartment = async (req, res, next) => {
        try {
            const department = await departmentService.createDepartment(req.body);
            res.status(201).json({
                success: true,
                data: department
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    /**
     * Get all departments
     */
    getDepartments = async (req, res, next) => {
        try {
            const page = parseInt(req.query.page) || 1;
            const limit = parseInt(req.query.limit) || 10;
            const result = await departmentService.getAllDepartments(page, limit);
            res.status(200).json({
                success: true,
                ...result
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    /**
     * Get single department
     */
    getDepartment = async (req, res, next) => {
        try {
            const department = await departmentService.getDepartmentById(req.params.id);
            res.status(200).json({
                success: true,
                data: department
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };

    /**
     * Update department
     */
    updateDepartment = async (req, res, next) => {
        try {
            const department = await departmentService.updateDepartment(req.params.id, req.body);
            res.status(200).json({
                success: true,
                data: department
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    /**
     * Delete department
     */
    deleteDepartment = async (req, res, next) => {
        try {
            await departmentService.deleteDepartment(req.params.id);
            res.status(200).json({
                success: true,
                message: 'Department deleted successfully'
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };
}

module.exports = new DepartmentController();
