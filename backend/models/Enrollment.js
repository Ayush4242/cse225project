const mongoose = require('mongoose');

const enrollmentSchema = new mongoose.Schema({
    student: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Student',
        required: [true, 'Student ID is required']
    },
    mapping: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Mapping',
        required: [true, 'Mapping ID is required']
    }
}, {
    timestamps: true
});

// Ensure a student can only be enrolled once in a specific mapping
enrollmentSchema.index({ student: 1, mapping: 1 }, { unique: true });

module.exports = mongoose.model('Enrollment', enrollmentSchema);
