const mongoose = require('mongoose');

const attendanceSchema = new mongoose.Schema({
    student: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Student',
        required: [true, 'Student ID is required']
    },
    session: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Session',
        required: [true, 'Session ID is required']
    },
    timestamp: {
        type: Date,
        default: Date.now
    },
    status: {
        type: String,
        enum: ['present', 'late'],
        default: 'present'
    },
    location: {
        latitude: Number,
        longitude: Number
    },
    faceVerified: {
        type: Boolean,
        default: false
    },
    deviceId: {
        type: String
    }
}, {
    timestamps: true
});

// Compound index to prevent duplicate attendance for the same student in the same session
attendanceSchema.index({ student: 1, session: 1 }, { unique: true });

module.exports = mongoose.model('Attendance', attendanceSchema);
