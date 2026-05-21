const classService = require('../services/classService');
const ApiError = require('../utils/apiError');

class ClassController {
    createClass = async (req, res, next) => {
        try {
            const classObj = await classService.createClass(req.body);
            res.status(201).json({
                success: true,
                data: classObj
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    getClasses = async (req, res, next) => {
        try {
            const result = await classService.getAllClasses(req.query);
            res.status(200).json({
                success: true,
                ...result
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    getClass = async (req, res, next) => {
        try {
            const classObj = await classService.getClassById(req.params.id);
            res.status(200).json({
                success: true,
                data: classObj
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };

    updateClass = async (req, res, next) => {
        try {
            const classObj = await classService.updateClass(req.params.id, req.body);
            res.status(200).json({
                success: true,
                data: classObj
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    deleteClass = async (req, res, next) => {
        try {
            await classService.deleteClass(req.params.id);
            res.status(200).json({
                success: true,
                message: 'Class deleted successfully'
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };
}

module.exports = new ClassController();
