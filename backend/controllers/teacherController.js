const teacherService = require('../services/teacherService');
const mappingService = require('../services/mappingService');
const timetableService = require('../services/timetableService');
const ApiError = require('../utils/apiError');

class TeacherController {
    getMyClasses = async (req, res, next) => {
        try {
            const teacherId = req.user.teacherProfileId;
            if (!teacherId) {
                return next(new ApiError(403, 'Teacher profile not found'));
            }

            const mappings = await mappingService.getMappings({ teacher: teacherId });
            res.status(200).json({
                success: true,
                data: mappings.mappings
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    getTodayClasses = async (req, res, next) => {
        try {
            const teacherId = req.user.teacherProfileId;
            if (!teacherId) {
                return next(new ApiError(403, 'Teacher profile not found'));
            }

            const classes = await timetableService.getTodayClasses(teacherId);
            res.status(200).json({
                success: true,
                data: classes
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    updateProfile = async (req, res, next) => {
        try {
            const teacherId = req.user.teacherProfileId;
            if (!teacherId) {
                return next(new ApiError(403, 'Teacher profile not found'));
            }

            const teacher = await teacherService.updateTeacher(teacherId, req.body);
            res.status(200).json({
                success: true,
                message: 'Profile updated successfully',
                data: teacher
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    createTeacher = async (req, res, next) => {
        try {
            const teacher = await teacherService.createTeacher(req.body);
            res.status(201).json({
                success: true,
                data: teacher
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    getTeachers = async (req, res, next) => {
        try {
            const result = await teacherService.getAllTeachers(req.query);
            res.status(200).json({
                success: true,
                ...result
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    getTeacher = async (req, res, next) => {
        try {
            const teacher = await teacherService.getTeacherById(req.params.id);
            res.status(200).json({
                success: true,
                data: teacher
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };

    updateTeacher = async (req, res, next) => {
        try {
            const teacher = await teacherService.updateTeacher(req.params.id, req.body);
            res.status(200).json({
                success: true,
                data: teacher
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    deleteTeacher = async (req, res, next) => {
        try {
            await teacherService.deleteTeacher(req.params.id);
            res.status(200).json({
                success: true,
                message: 'Teacher deleted successfully'
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };
}

module.exports = new TeacherController();
