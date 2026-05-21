const mongoose = require('mongoose');

const holidaySchema = new mongoose.Schema({
    title: {
        type: String,
        required: [true, 'Holiday title is required'],
        trim: true
    },
    startDate: {
        type: Date,
        required: [true, 'Start date is required']
    },
    endDate: {
        type: Date,
        required: [true, 'End date is required']
    },
    description: {
        type: String,
        trim: true
    },
    type: {
        type: String,
        enum: ['National', 'State', 'Academic', 'Other'],
        default: 'Other'
    },
    targetRole: {
        type: String,
        enum: ['all', 'teacher', 'student'],
        default: 'all'
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('Holiday', holidaySchema);
