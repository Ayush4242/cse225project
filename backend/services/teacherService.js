const mongoose = require('mongoose');
const User = require('../models/User');
const Teacher = require('../models/Teacher');
const Department = require('../models/Department');
const Mapping = require('../models/Mapping');
const Timetable = require('../models/Timetable');
const ApiError = require('../utils/apiError');

class TeacherService {
    /**
     * Create a teacher: Creates both User and Teacher profile
     */
    async createTeacher(teacherData) {
        const session = await mongoose.startSession();
        session.startTransaction();

        try {
            // 1. Check if department exists
            const dept = await Department.findById(teacherData.department);
            if (!dept) throw new Error('Department not found');

            // 2. Check if user already exists
            const existingUser = await User.findOne({ email: teacherData.email });
            if (existingUser) throw new Error('User with this email already exists');

            // 3. Check if employeeId exists
            const existingTeacher = await Teacher.findOne({ employeeId: teacherData.employeeId.toUpperCase() });
            if (existingTeacher) throw new Error('Teacher with this Employee ID already exists');

            // 4. Create User
            const user = new User({
                name: teacherData.name,
                email: teacherData.email,
                password: teacherData.password, // Will be hashed by pre-save hook
                role: 'teacher'
            });
            await user.save({ session });

            // 5. Create Teacher Profile
            const teacher = new Teacher({
                user: user._id,
                employeeId: teacherData.employeeId.toUpperCase(),
                department: teacherData.department,
                designation: teacherData.designation,
                phoneNumber: teacherData.phoneNumber
            });
            await teacher.save({ session });

            await session.commitTransaction();
            
            // Return populated teacher data
            return await Teacher.findById(teacher._id).populate('user', '-password').populate('department', 'name');
        } catch (error) {
            await session.abortTransaction();
            throw error;
        } finally {
            session.endSession();
        }
    }

    async getAllTeachers(query = {}) {
        const { department, page = 1, limit = 10 } = query;
        const filter = {};
        if (department) filter.department = department;

        const skip = (page - 1) * limit;
        const teachers = await Teacher.find(filter)
            .populate('user', '-password')
            .populate('department', 'name')
            .sort({ createdAt: -1 })
            .skip(skip)
            .limit(limit);

        const total = await Teacher.countDocuments(filter);

        return {
            teachers,
            totalPages: Math.ceil(total / limit),
            currentPage: page,
            totalTeachers: total
        };
    }

    async getTeacherById(id) {
        const teacher = await Teacher.findById(id)
            .populate('user', '-password')
            .populate('department', 'name');
        if (!teacher) throw new Error('Teacher not found');
        return teacher;
    }

    async updateTeacher(id, updateData) {
        const teacher = await Teacher.findById(id);
        if (!teacher) throw new Error('Teacher not found');

        // Update profile fields
        if (updateData.designation) teacher.designation = updateData.designation;
        if (updateData.phoneNumber) teacher.phoneNumber = updateData.phoneNumber;
        if (updateData.department) teacher.department = updateData.department;
        
        await teacher.save();

        // Update user fields if provided
        const userUpdate = {};
        if (updateData.name) userUpdate.name = updateData.name;
        if (updateData.password) {
            // We need to fetch the user to use the save() hook for hashing,
            // or hash it manually if using findByIdAndUpdate
            const user = await User.findById(teacher.user);
            if (user) {
                user.password = updateData.password;
                await user.save();
            }
        } else if (updateData.name) {
            await User.findByIdAndUpdate(teacher.user, { name: updateData.name });
        }

        return await this.getTeacherById(id);
    }

    async deleteTeacher(id) {
        const teacher = await Teacher.findById(id);
        if (!teacher) throw new Error('Teacher not found');

        // Delete all mappings for this teacher
        await Mapping.deleteMany({ teacher: id });

        // Delete all timetable slots for this teacher
        await Timetable.updateMany(
            { 'slots.teacher': id },
            { $pull: { slots: { teacher: id } } }
        );

        // Delete both User and Teacher profile
        await User.findByIdAndDelete(teacher.user);
        await Teacher.findByIdAndDelete(id);

        return { message: 'Teacher and associated user account deleted' };
    }
}

module.exports = new TeacherService();
