const mappingService = require('../services/mappingService');
const ApiError = require('../utils/apiError');

class MappingController {
    createMapping = async (req, res, next) => {
        try {
            const mapping = await mappingService.createMapping(req.body);
            res.status(201).json({
                success: true,
                data: mapping
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    getMappings = async (req, res, next) => {
        try {
            const result = await mappingService.getMappings(req.query);
            res.status(200).json({
                success: true,
                ...result
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    deleteMapping = async (req, res, next) => {
        try {
            await mappingService.deleteMapping(req.params.id);
            res.status(200).json({
                success: true,
                message: 'Mapping deleted successfully'
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };
}

module.exports = new MappingController();
