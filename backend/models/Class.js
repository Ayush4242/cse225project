const mongoose = require('mongoose');

const classSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, 'Class name is required'],
        trim: true,
        uppercase: true
    },
    department: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Department',
        required: [true, 'Department ID is required']
    },
    batchYear: {
        type: Number,
        required: [true, 'Batch year is required']
    }
}, {
    timestamps: true
});

// Compound index to ensure class name is unique within a department for a specific batch
classSchema.index({ name: 1, department: 1, batchYear: 1 }, { unique: true });

module.exports = mongoose.model('Class', classSchema);
