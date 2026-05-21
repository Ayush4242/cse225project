const mongoose = require('mongoose');

const sessionSchema = new mongoose.Schema({
    teacher: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Teacher',
        required: [true, 'Teacher ID is required']
    },
    subject: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Subject',
        required: [true, 'Subject ID is required']
    },
    section: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Section',
        required: [true, 'Section ID is required']
    },
    timetableSlotId: {
        type: mongoose.Schema.Types.ObjectId,
        required: [true, 'Timetable Slot ID is required']
    },
    startTime: {
        type: Date,
        default: Date.now
    },
    endTime: {
        type: Date
    },
    isActive: {
        type: Boolean,
        default: true
    },
    qrCodeToken: {
        type: String,
        required: true
    },
    qrExpiry: {
        type: Date,
        required: true
    },
    location: {
        latitude: Number,
        longitude: Number
    },
    radius: {
        type: Number,
        default: 50 // meters
    }
}, {
    timestamps: true
});

// Indexing for faster lookups of active sessions
sessionSchema.index({ teacher: 1, isActive: 1 });
sessionSchema.index({ qrCodeToken: 1 });

module.exports = mongoose.model('Session', sessionSchema);
