const sessionService = require('../services/sessionService');
const ApiError = require('../utils/apiError');

class SessionController {
    /**
     * Start a new class session
     */
    startSession = async (req, res, next) => {
        try {
            // teacherId comes from the protect middleware (req.user.profileId or similar)
            // Assuming the JWT payload or user fetch attached the teacher profile ID to req.teacherId
            const teacherId = req.user.teacherProfileId; 
            
            if (!teacherId) {
                return next(new ApiError(403, 'Only teachers can start sessions'));
            }

            const session = await sessionService.startSession(teacherId, req.body);
            res.status(201).json({
                success: true,
                data: session
            });
        } catch (error) {
            next(new ApiError(error.statusCode || 400, error.message));
        }
    };

    /**
     * Refresh the QR token for the current session
     */
    refreshQr = async (req, res, next) => {
        try {
            const teacherId = req.user.teacherProfileId;
            const { sessionId } = req.params;

            const qrData = await sessionService.refreshQrToken(teacherId, sessionId);
            res.status(200).json({
                success: true,
                data: qrData
            });
        } catch (error) {
            next(new ApiError(error.statusCode || 400, error.message));
        }
    };

    /**
     * End the active session
     */
    endSession = async (req, res, next) => {
        try {
            const teacherId = req.user.teacherProfileId;
            const { sessionId } = req.params;

            const session = await sessionService.endSession(teacherId, sessionId);
            res.status(200).json({
                success: true,
                message: 'Session ended successfully',
                data: session
            });
        } catch (error) {
            next(new ApiError(error.statusCode || 400, error.message));
        }
    };

    /**
     * Get the current active session for the logged-in teacher
     */
    getActiveSession = async (req, res, next) => {
        try {
            const teacherId = req.user.teacherProfileId;
            const session = await sessionService.getActiveSession(teacherId);

            res.status(200).json({
                success: true,
                data: session || null
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };
    /**
     * Get active sessions for the logged-in student based on enrollments
     */
    getActiveSessionsForStudent = async (req, res, next) => {
        try {
            const studentId = req.user.studentProfileId;
            const sessions = await sessionService.getActiveSessionsForStudent(studentId);

            res.status(200).json({
                success: true,
                data: sessions
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };
}

module.exports = new SessionController();
