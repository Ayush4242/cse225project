const Subject = require('../models/Subject');
const Department = require('../models/Department');
const Mapping = require('../models/Mapping');
const Timetable = require('../models/Timetable');

class SubjectService {
    async createSubject(subjectData) {
        // Validate department
        const dept = await Department.findById(subjectData.department);
        if (!dept) {
            throw new Error('Department not found');
        }

        const existingSubject = await Subject.findOne({ code: subjectData.code.toUpperCase() });
        if (existingSubject) {
            throw new Error('Subject with this code already exists');
        }

        const subject = await Subject.create(subjectData);
        // Populate department before returning
        return await Subject.findById(subject._id).populate('department', 'name description');
    }

    async getAllSubjects(query = {}) {
        const { department, page = 1, limit = 10 } = query;
        const filter = {};
        if (department) filter.department = department;

        const skip = (page - 1) * limit;
        const subjects = await Subject.find(filter)
            .populate('department', 'name')
            .sort({ name: 1 })
            .skip(skip)
            .limit(limit);

        const total = await Subject.countDocuments(filter);

        return {
            subjects,
            totalPages: Math.ceil(total / limit),
            currentPage: page,
            totalSubjects: total
        };
    }

    async getSubjectById(id) {
        const subject = await Subject.findById(id).populate('department', 'name');
        if (!subject) {
            throw new Error('Subject not found');
        }
        return subject;
    }

    async updateSubject(id, updateData) {
        const subject = await Subject.findByIdAndUpdate(id, updateData, {
            new: true,
            runValidators: true
        });
        if (!subject) {
            throw new Error('Subject not found');
        }
        return subject;
    }

    async deleteSubject(id) {
        const subject = await Subject.findById(id);
        if (!subject) {
            throw new Error('Subject not found');
        }

        // Delete all mappings for this subject
        await Mapping.deleteMany({ subject: id });

        // Delete all timetable slots for this subject
        await Timetable.updateMany(
            { 'slots.subject': id },
            { $pull: { slots: { subject: id } } }
        );

        // Delete the subject
        await Subject.findByIdAndDelete(id);
        
        return subject;
    }
}

module.exports = new SubjectService();
