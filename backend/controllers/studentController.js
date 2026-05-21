const studentService = require('../services/studentService');
const ApiError = require('../utils/apiError');

class StudentController {
    createStudent = async (req, res, next) => {
        try {
            const student = await studentService.createStudent(req.body);
            res.status(201).json({
                success: true,
                data: student
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    /**
     * Handle bulk CSV upload
     */
    bulkUpload = async (req, res, next) => {
        try {
            if (!req.file) {
                return next(new ApiError(400, 'Please upload a CSV file'));
            }

            const result = await studentService.bulkUploadStudents(req.file.path);
            res.status(200).json({
                success: true,
                ...result
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    getStudents = async (req, res, next) => {
        try {
            const result = await studentService.getAllStudents(req.query);
            res.status(200).json({
                success: true,
                ...result
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    getStudent = async (req, res, next) => {
        try {
            const student = await studentService.getStudentById(req.params.id);
            res.status(200).json({
                success: true,
                data: student
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };

    updateStudent = async (req, res, next) => {
        try {
            const student = await studentService.updateStudent(req.params.id, req.body);
            res.status(200).json({
                success: true,
                data: student
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    deleteStudent = async (req, res, next) => {
        try {
            await studentService.deleteStudent(req.params.id);
            res.status(200).json({
                success: true,
                message: 'Student deleted successfully'
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };
}

module.exports = new StudentController();
