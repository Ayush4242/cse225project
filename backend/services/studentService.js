const mongoose = require('mongoose');
const User = require('../models/User');
const Student = require('../models/Student');
const Section = require('../models/Section');
const csv = require('csv-parser');
const fs = require('fs');

class StudentService {
    /**
     * Create a single student
     */
    async createStudent(studentData) {
        const session = await mongoose.startSession();
        session.startTransaction();

        try {
            // 1. Validate Section
            const section = await Section.findById(studentData.section);
            if (!section) throw new Error('Section not found');

            // 2. Check if user already exists
            const existingUser = await User.findOne({ email: studentData.email });
            if (existingUser) throw new Error(`User with email ${studentData.email} already exists`);

            // 3. Check if roll number exists
            const existingStudent = await Student.findOne({ rollNumber: studentData.rollNumber.toUpperCase() });
            if (existingStudent) throw new Error(`Student with Roll Number ${studentData.rollNumber} already exists`);

            // 4. Create User
            const user = new User({
                name: studentData.name,
                email: studentData.email,
                password: studentData.password || 'Student@123', // Default password
                role: 'student'
            });
            await user.save({ session });

            // 5. Create Student Profile
            const student = new Student({
                user: user._id,
                rollNumber: studentData.rollNumber.toUpperCase(),
                section: studentData.section,
                parentContact: studentData.parentContact,
                admissionYear: studentData.admissionYear || new Date().getFullYear()
            });
            await student.save({ session });

            await session.commitTransaction();
            return await Student.findById(student._id)
                .populate('user', '-password')
                .populate({
                    path: 'section',
                    populate: {
                        path: 'class',
                        populate: { path: 'department' }
                    }
                });
        } catch (error) {
            await session.abortTransaction();
            throw error;
        } finally {
            session.endSession();
        }
    }

    /**
     * Bulk Upload Students from CSV
     * Expected CSV Columns: name, email, rollNumber, sectionId, parentContact, admissionYear
     */
    async bulkUploadStudents(filePath) {
        const results = [];
        const errors = [];
        let successCount = 0;

        return new Promise((resolve, reject) => {
            fs.createReadStream(filePath)
                .pipe(csv())
                .on('data', (data) => results.push(data))
                .on('end', async () => {
                    for (const row of results) {
                        try {
                            await this.createStudent({
                                name: row.name,
                                email: row.email,
                                rollNumber: row.rollNumber,
                                section: row.sectionId,
                                parentContact: row.parentContact,
                                admissionYear: row.admissionYear
                            });
                            successCount++;
                        } catch (err) {
                            errors.push({ row: row.rollNumber || row.email, error: err.message });
                        }
                    }
                    // Clean up file after processing
                    fs.unlinkSync(filePath);
                    resolve({ successCount, errors });
                })
                .on('error', (err) => reject(err));
        });
    }

    async getAllStudents(query = {}) {
        const { section, page = 1, limit = 10, search } = query;
        const filter = {};
        if (section) filter.section = section;
        
        // Search by roll number or via user name (requires lookup/aggregation or separate search)
        if (search) {
            filter.rollNumber = { $regex: search, $options: 'i' };
        }

        const skip = (page - 1) * limit;
        const students = await Student.find(filter)
            .populate('user', '-password')
            .populate({
                path: 'section',
                populate: { path: 'class', populate: { path: 'department' } }
            })
            .sort({ rollNumber: 1 })
            .skip(skip)
            .limit(limit);

        const total = await Student.countDocuments(filter);

        return {
            students,
            totalPages: Math.ceil(total / limit),
            currentPage: page,
            totalStudents: total
        };
    }

    async getStudentById(id) {
        const student = await Student.findById(id)
            .populate('user', '-password')
            .populate('section');
        if (!student) throw new Error('Student not found');
        return student;
    }

    async updateStudent(id, updateData) {
        const student = await Student.findById(id);
        if (!student) throw new Error('Student not found');

        if (updateData.section) student.section = updateData.section;
        if (updateData.parentContact) student.parentContact = updateData.parentContact;
        
        await student.save();

        if (updateData.name) {
            await User.findByIdAndUpdate(student.user, { name: updateData.name });
        }

        return await this.getStudentById(id);
    }

    async deleteStudent(id) {
        const student = await Student.findById(id);
        if (!student) throw new Error('Student not found');

        await User.findByIdAndDelete(student.user);
        await Student.findByIdAndDelete(id);

        return { message: 'Student and associated account deleted' };
    }
}

module.exports = new StudentService();
