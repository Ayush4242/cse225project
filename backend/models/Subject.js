const mongoose = require('mongoose');

const subjectSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, 'Subject name is required'],
        trim: true,
        uppercase: true
    },
    code: {
        type: String,
        required: [true, 'Subject code is required'],
        unique: true,
        trim: true,
        uppercase: true
    },
    department: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Department',
        required: [true, 'Department is required']
    },
    credits: {
        type: Number,
        default: 3
    }
}, {
    timestamps: true
});

// Indexing
subjectSchema.index({ code: 1 });
subjectSchema.index({ department: 1 });

module.exports = mongoose.model('Subject', subjectSchema);
