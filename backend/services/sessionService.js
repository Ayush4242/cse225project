const Session = require('../models/Session');
const crypto = require('crypto');
const ApiError = require('../utils/apiError');

class SessionService {
    /**
     * Generate a new secure random token for the QR code
     * @returns {string}
     */
    _generateToken() {
        return crypto.randomBytes(32).toString('hex');
    }

    /**
     * Start a new class session
     */
    async startSession(teacherId, sessionData) {
        const { subjectId, sectionId, timetableSlotId, location, radius } = sessionData;

        // 1. Check if teacher already has an active session
        const activeSession = await Session.findOne({ teacher: teacherId, isActive: true });
        if (activeSession) {
            throw new ApiError(400, 'You already have an active session. Please end it before starting a new one.');
        }

        // 2. Validate Timetable Slot
        const Timetable = require('../models/Timetable');
        const timetable = await Timetable.findOne({
            'slots._id': timetableSlotId,
            'slots.teacher': teacherId
        });

        if (!timetable) {
            throw new ApiError(404, 'Scheduled class not found');
        }

        const slot = timetable.slots.id(timetableSlotId);

        // 3. Time Validation (with 5 min grace period)
        const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        const today = days[new Date().getDay()];

        if (slot.day !== today) {
            throw new ApiError(400, `This class is scheduled for ${slot.day}, not today`);
        }

        const now = new Date();
        const currentTimeInMinutes = now.getHours() * 60 + now.getMinutes();
        const [startH, startM] = slot.startTime.split(':').map(Number);
        const [endH, endM] = slot.endTime.split(':').map(Number);

        const startTimeInMinutes = startH * 60 + startM;
        const endTimeInMinutes = endH * 60 + endM;

        const GRACE_PERIOD = 15; // minutes

        if (currentTimeInMinutes < (startTimeInMinutes - GRACE_PERIOD)) {
            throw new ApiError(400, `You can only start the session ${GRACE_PERIOD} minutes before the scheduled time`);
        }

        if (currentTimeInMinutes > endTimeInMinutes) {
            throw new ApiError(400, 'This class schedule has already ended');
        }

        // 4. Prevent duplicate session for the same slot today
        const startOfDay = new Date();
        startOfDay.setHours(0, 0, 0, 0);

        const existingSession = await Session.findOne({
            timetableSlotId: timetableSlotId,
            startTime: { $gte: startOfDay }
        });

        if (existingSession) {
            throw new ApiError(400, 'A session has already been conducted for this schedule today');
        }

        // 5. Create session
        const token = this._generateToken();
        const expiry = new Date(Date.now() + 60 * 1000); // 1 min QR expiry

        const session = await Session.create({
            teacher: teacherId,
            subject: subjectId,
            section: sectionId,
            timetableSlotId: timetableSlotId,
            qrCodeToken: token,
            qrExpiry: expiry,
            location: location,
            radius: radius || 50
        });

        return await Session.findById(session._id)
            .populate('subject', 'name code')
            .populate({
                path: 'section',
                populate: { path: 'class', select: 'name' }
            });
    }

    /**
     * Refresh the QR token for an active session
     */
    async refreshQrToken(teacherId, sessionId) {
        const session = await Session.findOne({ _id: sessionId, teacher: teacherId, isActive: true });
        if (!session) {
            throw new ApiError(404, 'Active session not found');
        }

        session.qrCodeToken = this._generateToken();
        session.qrExpiry = new Date(Date.now() + 60 * 1000); // 60 seconds expiry
        await session.save();

        return {
            qrCodeToken: session.qrCodeToken,
            qrExpiry: session.qrExpiry
        };
    }

    /**
     * End a class session
     */
    async endSession(teacherId, sessionId) {
        const session = await Session.findOne({ _id: sessionId, teacher: teacherId, isActive: true });
        if (!session) {
            throw new ApiError(404, 'Active session not found');
        }

        session.isActive = false;
        session.endTime = Date.now();
        await session.save();

        return await Session.findById(session._id)
            .populate('subject', 'name code')
            .populate({
                path: 'section',
                populate: { path: 'class', select: 'name' }
            });
    }

    /**
     * Get currently active session for a teacher
     */
    async getActiveSession(teacherId) {
        const session = await Session.findOne({ teacher: teacherId, isActive: true })
            .populate('subject', 'name code')
            .populate({
                path: 'section',
                populate: { path: 'class', select: 'name' }
            });

        if (session && session.timetableSlotId) {
            const Timetable = require('../models/Timetable');
            const timetable = await Timetable.findOne({ 'slots._id': session.timetableSlotId });
            if (timetable) {
                const slot = timetable.slots.id(session.timetableSlotId);
                const [endH, endM] = slot.endTime.split(':').map(Number);
                const endTimeInMinutes = endH * 60 + endM;
                const now = new Date();
                const currentTimeInMinutes = now.getHours() * 60 + now.getMinutes();

                if (currentTimeInMinutes > endTimeInMinutes) {
                    session.isActive = false;
                    session.endTime = Date.now();
                    await session.save();
                    return null;
                }
            }
        }

        return session;
    }

    /**
     * Validate a QR token scanned by a student
     */
    async validateSessionToken(sessionId, token) {
        const session = await Session.findOne({ _id: sessionId, isActive: true });
        
        if (!session) {
            throw new Error('Session is no longer active');
        }

        // Auto-close check if slot end time passed
        const Timetable = require('../models/Timetable');
        const timetable = await Timetable.findOne({ 'slots._id': session.timetableSlotId });
        if (timetable) {
            const slot = timetable.slots.id(session.timetableSlotId);
            const [endH, endM] = slot.endTime.split(':').map(Number);
            const endTimeInMinutes = endH * 60 + endM;
            const now = new Date();
            const currentTimeInMinutes = now.getHours() * 60 + now.getMinutes();

            if (currentTimeInMinutes > endTimeInMinutes) {
                // Auto-end the session
                session.isActive = false;
                session.endTime = Date.now();
                await session.save();
                throw new Error('This class session has officially ended');
            }
        }

        if (session.qrCodeToken !== token) {
            throw new Error('Invalid or expired QR code');
        }

        if (new Date() > session.qrExpiry) {
            throw new Error('QR code has expired. Please scan the refreshed code.');
        }

        return session;
    }
    /**
     * Get all active sessions for a specific student based on their enrollments
     */
    async getActiveSessionsForStudent(studentId) {
        // Find student's enrollments
        const Enrollment = require('../models/Enrollment');
        const enrollments = await Enrollment.find({ student: studentId });
        const enrolledMappingIds = enrollments.map(e => e.mapping.toString());

        if (enrolledMappingIds.length === 0) {
            return [];
        }

        // Find all globally active sessions
        const activeSessions = await Session.find({ isActive: true })
            .populate('subject', 'name code')
            .populate({
                path: 'section',
                populate: { path: 'class', select: 'name' }
            });

        const Mapping = require('../models/Mapping');
        
        const matchingSessions = [];
        for (const session of activeSessions) {
            // Find the mapping associated with this session
            const mapping = await Mapping.findOne({
                teacher: session.teacher._id || session.teacher,
                subject: session.subject._id || session.subject,
                section: session.section._id || session.section
            });
            
            if (mapping && enrolledMappingIds.includes(mapping._id.toString())) {
                matchingSessions.push(session);
            }
        }
        
        return matchingSessions;
    }
}

module.exports = new SessionService();
