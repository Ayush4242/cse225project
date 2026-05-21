const attendanceService = require('../services/attendanceService');
const ApiError = require('../utils/apiError');

class AttendanceController {
    /**
     * Mark attendance for a student (scanned QR)
     */
    markAttendance = async (req, res, next) => {
        try {
            const studentId = req.user.studentProfileId;
            if (!studentId) {
                return next(new ApiError(403, 'Only students can mark attendance'));
            }

            const { sessionId, qrToken, location, faceVerified, deviceId } = req.body;
            if (!sessionId || !qrToken) {
                return next(new ApiError(400, 'Session ID and QR Token are required'));
            }

            const attendance = await attendanceService.markAttendance(
                studentId,
                { sessionId, qrToken, location, faceVerified, deviceId }
            );

            res.status(201).json({
                success: true,
                message: 'Attendance marked successfully',
                data: attendance
            });
        } catch (error) {
            next(new ApiError(error.statusCode || 400, error.message));
        }
    };

    /**
     * Get list of students present in a session (for teacher)
     */
    getSessionAttendance = async (req, res, next) => {
        try {
            const { sessionId } = req.params;
            const attendanceList = await attendanceService.getSessionAttendance(sessionId);

            res.status(200).json({
                success: true,
                count: attendanceList.length,
                data: attendanceList
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    /**
     * Get analytics for a section (attendance % per student)
     */
    getSectionAnalytics = async (req, res, next) => {
        try {
            const { sectionId } = req.params;
            const stats = await attendanceService.getSectionAnalytics(sectionId);

            res.status(200).json({
                success: true,
                data: stats
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    /**
     * Get analytics for a student (subject-wise attendance)
     */
    getStudentAnalytics = async (req, res, next) => {
        try {
            const studentId = req.user.studentProfileId;
            if (!studentId) {
                return next(new ApiError(403, 'Only students can view their analytics'));
            }

            const stats = await attendanceService.getStudentAnalytics(studentId);

            res.status(200).json({
                success: true,
                data: stats
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };
}

module.exports = new AttendanceController();
