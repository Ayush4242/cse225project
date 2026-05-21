const timetableService = require('../services/timetableService');
const ApiError = require('../utils/apiError');

class TimetableController {
    addSlot = async (req, res, next) => {
        try {
            const { sectionId } = req.params;
            const timetable = await timetableService.addOrUpdateSlot(sectionId, req.body);
            res.status(200).json({
                success: true,
                data: timetable
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    getTimetable = async (req, res, next) => {
        try {
            const { sectionId } = req.params;
            const timetable = await timetableService.getTimetableBySection(sectionId);
            res.status(200).json({
                success: true,
                data: timetable
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };

    removeSlot = async (req, res, next) => {
        try {
            const { sectionId, slotId } = req.params;
            const timetable = await timetableService.removeSlot(sectionId, slotId);
            res.status(200).json({
                success: true,
                data: timetable
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };
}

module.exports = new TimetableController();
