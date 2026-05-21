const mongoose = require('mongoose');

const departmentSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, 'Department name is required'],
        unique: true,
        trim: true,
        uppercase: true
    },
    description: {
        type: String,
        trim: true
    }
}, {
    timestamps: true
});

// Indexing for faster searches
departmentSchema.index({ name: 1 });

module.exports = mongoose.model('Department', departmentSchema);
