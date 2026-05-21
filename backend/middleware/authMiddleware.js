const jwt = require('jsonwebtoken');
const User = require('../models/User');
const Teacher = require('../models/Teacher');
const Student = require('../models/Student');
const ApiError = require('../utils/apiError');

/**
 * Middleware to protect routes - ensures user is logged in
 */
const protect = async (req, res, next) => {
    let token;

    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
        token = req.headers.authorization.split(' ')[1];
    }

    if (!token) {
        return next(new ApiError(401, 'Not authorized to access this route'));
    }

    try {
        // Verify token
        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'fallback_secret');
        
        // Fetch user from DB
        const user = await User.findById(decoded.id);
        if (!user) {
            return next(new ApiError(401, 'The user belonging to this token no longer exists'));
        }

        // Attach user to request
        req.user = user;

        // If user is teacher, attach teacher profile ID
        if (user.role === 'teacher') {
            const teacher = await Teacher.findOne({ user: user._id });
            if (teacher) req.user.teacherProfileId = teacher._id;
        }

        // If user is student, attach student profile ID
        if (user.role === 'student') {
            const student = await Student.findOne({ user: user._id });
            if (student) req.user.studentProfileId = student._id;
        }

        next();
    } catch (err) {
        return next(new ApiError(401, 'Not authorized to access this route'));
    }
};

/**
 * Middleware to authorize specific roles
 */
const authorize = (...roles) => {
    return (req, res, next) => {
        if (!roles.includes(req.user.role)) {
            return next(new ApiError(403, `User role ${req.user.role} is not authorized to access this route`));
        }
        next();
    };
};

module.exports = { protect, authorize };
