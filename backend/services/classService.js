const Class = require('../models/Class');
const Department = require('../models/Department');
const Section = require('../models/Section');
const Mapping = require('../models/Mapping');
const Student = require('../models/Student');
const Timetable = require('../models/Timetable');
const Session = require('../models/Session');
const User = require('../models/User');

class ClassService {
    async createClass(classData) {
        // Validate department exists
        const dept = await Department.findById(classData.department);
        if (!dept) {
            throw new Error('Department not found');
        }

        const existingClass = await Class.findOne({
            name: classData.name.toUpperCase(),
            department: classData.department,
            batchYear: classData.batchYear
        });

        if (existingClass) {
            throw new Error('Class already exists in this department for this batch');
        }

        const newClass = await Class.create(classData);
        
        // Return populated class so Android can parse it into ClassModel
        return await Class.findById(newClass._id).populate('department', 'name');
    }

    async getAllClasses(query = {}) {
        const { department, page = 1, limit = 50 } = query;
        const filter = {};
        if (department) filter.department = department;

        const skip = (page - 1) * limit;
        const classes = await Class.find(filter)
            .populate('department', 'name')
            .sort({ batchYear: -1, name: 1 })
            .skip(skip)
            .limit(limit);

        const total = await Class.countDocuments(filter);

        return {
            classes,
            totalPages: Math.ceil(total / limit),
            currentPage: page,
            totalClasses: total
        };
    }

    async getClassById(id) {
        const classObj = await Class.findById(id).populate('department', 'name');
        if (!classObj) {
            throw new Error('Class not found');
        }
        return classObj;
    }

    async updateClass(id, updateData) {
        const classObj = await Class.findByIdAndUpdate(id, updateData, {
            new: true,
            runValidators: true
        }).populate('department', 'name');
        
        if (!classObj) {
            throw new Error('Class not found');
        }
        return classObj;
    }

    async deleteClass(id) {
        const classObj = await Class.findById(id);
        if (!classObj) {
            throw new Error('Class not found');
        }

        // Find all sections belonging to this class
        const sections = await Section.find({ class: id });
        const sectionIds = sections.map(s => s._id);

        if (sectionIds.length > 0) {
            // Delete all mappings for these sections
            await Mapping.deleteMany({ section: { $in: sectionIds } });

            // Find all students in these sections to delete their user accounts
            const students = await Student.find({ section: { $in: sectionIds } });
            const studentUserIds = students.map(s => s.user);
            
            // Delete student records
            await Student.deleteMany({ section: { $in: sectionIds } });
            
            // Delete user accounts for students
            await User.deleteMany({ _id: { $in: studentUserIds } });

            // Delete timetables for these sections
            await Timetable.deleteMany({ section: { $in: sectionIds } });

            // Delete sessions for these sections
            await Session.deleteMany({ section: { $in: sectionIds } });

            // Delete all sections
            await Section.deleteMany({ class: id });
        }

        // Finally delete the class
        await Class.findByIdAndDelete(id);
        
        return classObj;
    }
}

module.exports = new ClassService();
