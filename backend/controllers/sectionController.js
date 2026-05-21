const sectionService = require('../services/sectionService');
const ApiError = require('../utils/apiError');

class SectionController {
    createSection = async (req, res, next) => {
        try {
            const section = await sectionService.createSection(req.body);
            res.status(201).json({
                success: true,
                data: section
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    getSections = async (req, res, next) => {
        try {
            const result = await sectionService.getAllSections(req.query);
            res.status(200).json({
                success: true,
                ...result
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    getSection = async (req, res, next) => {
        try {
            const section = await sectionService.getSectionById(req.params.id);
            res.status(200).json({
                success: true,
                data: section
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };

    updateSection = async (req, res, next) => {
        try {
            const section = await sectionService.updateSection(req.params.id, req.body);
            res.status(200).json({
                success: true,
                data: section
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    deleteSection = async (req, res, next) => {
        try {
            await sectionService.deleteSection(req.params.id);
            res.status(200).json({
                success: true,
                message: 'Section deleted successfully'
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };
}

module.exports = new SectionController();
