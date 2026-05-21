const mongoose = require('mongoose');

const mappingSchema = new mongoose.Schema({
    teacher: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Teacher',
        required: [true, 'Teacher is required']
    },
    subject: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Subject',
        required: [true, 'Subject is required']
    },
    section: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Section',
        required: [true, 'Section is required']
    }
}, {
    timestamps: true
});

// Ensures that for a specific section, a subject is assigned to only one teacher
mappingSchema.index({ subject: 1, section: 1 }, { unique: true });

module.exports = mongoose.model('Mapping', mappingSchema);
