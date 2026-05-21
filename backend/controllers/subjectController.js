const subjectService = require('../services/subjectService');
const ApiError = require('../utils/apiError');

class SubjectController {
    createSubject = async (req, res, next) => {
        try {
            const subject = await subjectService.createSubject(req.body);
            res.status(201).json({
                success: true,
                data: subject
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    getSubjects = async (req, res, next) => {
        try {
            const result = await subjectService.getAllSubjects(req.query);
            res.status(200).json({
                success: true,
                ...result
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    getSubject = async (req, res, next) => {
        try {
            const subject = await subjectService.getSubjectById(req.params.id);
            res.status(200).json({
                success: true,
                data: subject
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };

    updateSubject = async (req, res, next) => {
        try {
            const subject = await subjectService.updateSubject(req.params.id, req.body);
            res.status(200).json({
                success: true,
                data: subject
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    deleteSubject = async (req, res, next) => {
        try {
            await subjectService.deleteSubject(req.params.id);
            res.status(200).json({
                success: true,
                message: 'Subject deleted successfully'
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };
}

module.exports = new SubjectController();
