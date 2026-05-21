const mongoose = require('mongoose');

const sectionSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, 'Section name is required'],
        trim: true,
        uppercase: true
    },
    class: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Class',
        required: [true, 'Class ID is required']
    },
    roomNumber: {
        type: String,
        trim: true
    }
}, {
    timestamps: true
});

// Compound index to ensure section name is unique within a class
sectionSchema.index({ name: 1, class: 1 }, { unique: true });

module.exports = mongoose.model('Section', sectionSchema);
