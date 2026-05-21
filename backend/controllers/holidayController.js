const holidayService = require('../services/holidayService');
const ApiError = require('../utils/apiError');

class HolidayController {
    createHoliday = async (req, res, next) => {
        try {
            const holiday = await holidayService.createHoliday(req.body);
            res.status(201).json({
                success: true,
                data: holiday
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    getHolidays = async (req, res, next) => {
        try {
            const role = req.user?.role || 'admin';
            const holidays = await holidayService.getHolidays(role);
            res.status(200).json({
                success: true,
                data: holidays
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    deleteHoliday = async (req, res, next) => {
        try {
            await holidayService.deleteHoliday(req.params.id);
            res.status(200).json({
                success: true,
                message: 'Holiday deleted successfully'
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };
}

module.exports = new HolidayController();
