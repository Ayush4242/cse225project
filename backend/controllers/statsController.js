const Department = require('../models/Department');
const Teacher = require('../models/Teacher');
const Student = require('../models/Student');
const Section = require('../models/Section');
const ApiError = require('../utils/apiError');

class StatsController {
    getDashboardStats = async (req, res, next) => {
        try {
            const [deptCount, teacherCount, studentCount, sectionCount] = await Promise.all([
                Department.countDocuments(),
                Teacher.countDocuments(),
                Student.countDocuments(),
                Section.countDocuments()
            ]);

            res.status(200).json({
                success: true,
                data: {
                    departments: deptCount,
                    teachers: teacherCount,
                    students: studentCount,
                    sections: sectionCount
                }
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };
}

module.exports = new StatsController();
