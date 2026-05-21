const Enrollment = require('../models/Enrollment');
const ApiError = require('../utils/apiError');

class EnrollmentController {
    // Get all enrollments, optionally filtered by mapping or student
    getEnrollments = async (req, res, next) => {
        try {
            const { mappingId, studentId } = req.query;
            const filter = {};
            if (mappingId) filter.mapping = mappingId;
            if (studentId) filter.student = studentId;

            const enrollments = await Enrollment.find(filter)
                .populate({
                    path: 'student',
                    populate: [
                        { path: 'user', select: 'name email' },
                        { path: 'section', select: 'name' }
                    ]
                })
                .populate({
                    path: 'mapping',
                    populate: [
                        { path: 'subject', select: 'name code' },
                        { path: 'teacher', select: 'user', populate: { path: 'user', select: 'name' } },
                        { path: 'section', select: 'name' }
                    ]
                });

            res.status(200).json({
                success: true,
                data: enrollments
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    // Create a new enrollment
    createEnrollment = async (req, res, next) => {
        try {
            const { studentId, mappingId } = req.body;

            if (!studentId || !mappingId) {
                return next(new ApiError(400, 'Student ID and Mapping ID are required'));
            }

            const existingEnrollment = await Enrollment.findOne({ student: studentId, mapping: mappingId });
            if (existingEnrollment) {
                return next(new ApiError(400, 'Student is already enrolled in this class'));
            }

            const enrollment = await Enrollment.create({
                student: studentId,
                mapping: mappingId
            });

            // Populate newly created enrollment to return full data
            const populatedEnrollment = await Enrollment.findById(enrollment._id)
                .populate({
                    path: 'student',
                    populate: [
                        { path: 'user', select: 'name email' },
                        { path: 'section', select: 'name' }
                    ]
                })
                .populate({
                    path: 'mapping',
                    populate: [
                        { path: 'subject', select: 'name code' },
                        { path: 'teacher', select: 'user', populate: { path: 'user', select: 'name' } },
                        { path: 'section', select: 'name' }
                    ]
                });

            res.status(201).json({
                success: true,
                data: populatedEnrollment
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    // Delete an enrollment
    deleteEnrollment = async (req, res, next) => {
        try {
            const enrollment = await Enrollment.findByIdAndDelete(req.params.id);
            if (!enrollment) {
                return next(new ApiError(404, 'Enrollment not found'));
            }

            res.status(200).json({
                success: true,
                message: 'Enrollment deleted successfully'
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };
}

module.exports = new EnrollmentController();
