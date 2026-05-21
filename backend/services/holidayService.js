const Holiday = require('../models/Holiday');

class HolidayService {
    async createHoliday(holidayData) {
        return await Holiday.create(holidayData);
    }

    async getHolidays(role) {
        if (!role || role === 'admin') {
            return await Holiday.find().sort({ startDate: 1 });
        } else {
            return await Holiday.find({ targetRole: { $in: ['all', role] } }).sort({ startDate: 1 });
        }
    }

    async deleteHoliday(id) {
        const holiday = await Holiday.findByIdAndDelete(id);
        if (!holiday) throw new Error('Holiday not found');
        return holiday;
    }
}

module.exports = new HolidayService();
