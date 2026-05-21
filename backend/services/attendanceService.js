const Attendance = require('../models/Attendance');
const sessionService = require('./sessionService');
const Student = require('../models/Student');
const ApiError = require('../utils/apiError');
const { calculateDistance } = require('../utils/geoUtils');

class AttendanceService {
    /**
     * Mark attendance for a student with QR, Location and Face validation
     */
    async markAttendance(studentId, attendanceData) {
        const { sessionId, qrToken, location, faceVerified, deviceId } = attendanceData;

        // 1. Validate the session token and expiry
        const session = await sessionService.validateSessionToken(sessionId, qrToken);

        // 2. Validate student is enrolled in the specific class mapping
        const student = await Student.findById(studentId);
        if (!student) {
            throw new ApiError(404, 'Student profile not found');
        }

        const Mapping = require('../models/Mapping');
        const Enrollment = require('../models/Enrollment');

        const mapping = await Mapping.findOne({
            teacher: session.teacher,
            subject: session.subject,
            section: session.section
        });

        if (!mapping) {
            throw new ApiError(404, 'Class mapping not found for this session');
        }

        const isEnrolled = await Enrollment.findOne({ student: studentId, mapping: mapping._id });
        if (!isEnrolled) {
            throw new ApiError(403, 'You are not enrolled in this class');
        }

        // 3. Check if attendance already marked
        const existingAttendance = await Attendance.findOne({ student: studentId, session: sessionId });
        if (existingAttendance) {
            throw new ApiError(400, 'Attendance already marked for this session');
        }

        // 4. Face Recognition Check
        if (!faceVerified) {
            throw new ApiError(400, 'Face verification failed or not performed');
        }

        // 5. Geo-fencing Validation (Haversine)
        if (session.location && location) {
            const distance = calculateDistance(
                session.location.latitude,
                session.location.longitude,
                location.latitude,
                location.longitude
            );

            // Allow up to session.radius or default 50m
            const allowedRadius = session.radius || 50;
            if (distance > allowedRadius) {
                throw new ApiError(403, `Geo-fencing failed. You are ${Math.round(distance)}m away. Allowed radius is ${allowedRadius}m.`);
            }
        } else if (session.location) {
            // Location is required if session has location set
            throw new ApiError(400, 'Location data is required for this session');
        }

        // 6. Calculate Status (Late detection)
        let status = 'present';
        const lateThresholdMinutes = 15; // Example: 15 mins late allowed
        const sessionStartTime = new Date(session.startTime);
        const currentTime = new Date();
        const diffMinutes = (currentTime - sessionStartTime) / (1000 * 60);

        if (diffMinutes > lateThresholdMinutes) {
            status = 'late';
        }

        // 7. Create attendance record
        const attendance = await Attendance.create({
            student: studentId,
            session: sessionId,
            location: location,
            faceVerified: true,
            deviceId: deviceId,
            status: status
        });

        return attendance;
    }

    /**
     * Get attendance list for a specific session
     */
    async getSessionAttendance(sessionId) {
        return await Attendance.find({ session: sessionId })
            .populate({
                path: 'student',
                populate: { path: 'user', select: 'name email' }
            })
            .sort({ timestamp: -1 });
    }

    /**
     * Get student attendance stats for a section
     */
    async getSectionAnalytics(sectionId) {
        const Session = require('../models/Session');
        const mongoose = require('mongoose');
        const totalSessions = await Session.countDocuments({ section: sectionId });

        if (totalSessions === 0) return [];

        const stats = await Attendance.aggregate([
            {
                $lookup: {
                    from: 'sessions',
                    localField: 'session',
                    foreignField: '_id',
                    as: 'sessionData'
                }
            },
            { $unwind: '$sessionData' },
            { $match: { 'sessionData.section': new mongoose.Types.ObjectId(sectionId) } },
            {
                $group: {
                    _id: '$student',
                    attendanceCount: { $sum: 1 }
                }
            },
            {
                $lookup: {
                    from: 'students',
                    localField: '_id',
                    foreignField: '_id',
                    as: 'studentInfo'
                }
            },
            { $unwind: '$studentInfo' },
            {
                $lookup: {
                    from: 'users',
                    localField: 'studentInfo.user',
                    foreignField: '_id',
                    as: 'userInfo'
                }
            },
            { $unwind: '$userInfo' },
            {
                $project: {
                    studentId: '$_id',
                    name: '$userInfo.name',
                    rollNumber: '$studentInfo.rollNumber',
                    attendanceCount: 1,
                    totalSessions: { $literal: totalSessions },
                    percentage: {
                        $multiply: [
                            { $divide: ['$attendanceCount', totalSessions] },
                            100
                        ]
                    }
                }
            },
            { $sort: { rollNumber: 1 } }
        ]);

        return stats;
    }
    /**
     * Get analytics for a student (subject-wise attendance)
     */
    async getStudentAnalytics(studentId) {
        const Session = require('../models/Session');
        const mongoose = require('mongoose');

        // 1. Get student to find their section
        const student = await Student.findById(studentId);
        if (!student) throw new ApiError(404, 'Student not found');

        // 2. Aggregate sessions per subject for this section
        const subjectStats = await Session.aggregate([
            { $match: { section: student.section } },
            {
                $group: {
                    _id: '$subject',
                    totalSessions: { $sum: 1 }
                }
            },
            {
                $lookup: {
                    from: 'subjects',
                    localField: '_id',
                    foreignField: '_id',
                    as: 'subjectInfo'
                }
            },
            { $unwind: '$subjectInfo' },
            {
                $lookup: {
                    from: 'attendances',
                    let: { subId: '$_id', stdId: new mongoose.Types.ObjectId(studentId) },
                    pipeline: [
                        {
                            $lookup: {
                                from: 'sessions',
                                localField: 'session',
                                foreignField: '_id',
                                as: 'sessionInfo'
                            }
                        },
                        { $unwind: '$sessionInfo' },
                        {
                            $match: {
                                $expr: {
                                    $and: [
                                        { $eq: ['$student', '$$stdId'] },
                                        { $eq: ['$sessionInfo.subject', '$$subId'] }
                                    ]
                                }
                            }
                        }
                    ],
                    as: 'attendanceInfo'
                }
            },
            {
                $project: {
                    subjectId: '$_id',
                    subjectName: '$subjectInfo.name',
                    totalSessions: 1,
                    attendanceCount: { $size: '$attendanceInfo' },
                    percentage: {
                        $cond: [
                            { $eq: ['$totalSessions', 0] },
                            0,
                            { $multiply: [{ $divide: [{ $size: '$attendanceInfo' }, '$totalSessions'] }, 100] }
                        ]
                    }
                }
            }
        ]);

        return subjectStats;
    }
}

module.exports = new AttendanceService();
