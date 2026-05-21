const Section = require('../models/Section');
const Class = require('../models/Class');
const Mapping = require('../models/Mapping');
const Student = require('../models/Student');
const Timetable = require('../models/Timetable');
const Session = require('../models/Session');
const User = require('../models/User');

class SectionService {
    async createSection(sectionData) {
        // Validate class exists
        const classObj = await Class.findById(sectionData.class);
        if (!classObj) {
            throw new Error('Class not found');
        }

        const existingSection = await Section.findOne({
            name: sectionData.name.toUpperCase(),
            class: sectionData.class
        });

        if (existingSection) {
            throw new Error('Section already exists for this class');
        }

        const newSection = await Section.create(sectionData);
        
        // Return populated section for Android model consistency
        return await Section.findById(newSection._id).populate({
            path: 'class',
            populate: { path: 'department', select: 'name' }
        });
    }

    async getAllSections(query = {}) {
        const { classId, page = 1, limit = 50 } = query;
        const filter = {};
        if (classId) filter.class = classId;

        const skip = (page - 1) * limit;
        const sections = await Section.find(filter)
            .populate({
                path: 'class',
                select: 'name department',
                populate: { path: 'department', select: 'name' }
            })
            .sort({ name: 1 })
            .skip(skip)
            .limit(limit);

        const total = await Section.countDocuments(filter);

        return {
            sections,
            totalPages: Math.ceil(total / limit),
            currentPage: page,
            totalSections: total
        };
    }

    async getSectionById(id) {
        const section = await Section.findById(id).populate({
            path: 'class',
            populate: { path: 'department', select: 'name' }
        });
        if (!section) {
            throw new Error('Section not found');
        }
        return section;
    }

    async updateSection(id, updateData) {
        const section = await Section.findByIdAndUpdate(id, updateData, {
            new: true,
            runValidators: true
        }).populate({
            path: 'class',
            populate: { path: 'department', select: 'name' }
        });
        
        if (!section) {
            throw new Error('Section not found');
        }
        return section;
    }

    async deleteSection(id) {
        const section = await Section.findById(id);
        if (!section) {
            throw new Error('Section not found');
        }

        // Delete all mappings for this section
        await Mapping.deleteMany({ section: id });

        // Find all students in this section to delete their user accounts
        const students = await Student.find({ section: id });
        const studentUserIds = students.map(s => s.user);
        
        // Delete student records
        await Student.deleteMany({ section: id });
        
        // Delete user accounts for students
        await User.deleteMany({ _id: { $in: studentUserIds } });

        // Delete timetable for this section
        await Timetable.deleteMany({ section: id });

        // Delete sessions for this section
        await Session.deleteMany({ section: id });

        // Delete the section
        await Section.findByIdAndDelete(id);
        
        return section;
    }
}

module.exports = new SectionService();
