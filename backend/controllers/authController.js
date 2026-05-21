const User = require('../models/User');
const ApiError = require('../utils/apiError');
const jwt = require('jsonwebtoken');

const signToken = (id, role) => {
    return jwt.sign({ id, role }, process.env.JWT_SECRET || 'fallback_secret', {
        expiresIn: process.env.JWT_EXPIRES_IN || '30d'
    });
};

class AuthController {
    login = async (req, res, next) => {
        try {
            const { email, password } = req.body;

            console.log('Login attempt:', { email, passwordProvided: !!password });

            if (!email || !password) {
                return next(new ApiError(400, 'Please provide email and password'));
            }

            const user = await User.findOne({ email }).select('+password');
            console.log('User found:', !!user);

            if (!user) {
                return next(new ApiError(401, 'Incorrect email or password'));
            }

            const isPasswordCorrect = await user.comparePassword(password);
            console.log('Password match:', isPasswordCorrect);

            if (!isPasswordCorrect) {
                return next(new ApiError(401, 'Incorrect email or password'));
            }

            const token = signToken(user._id, user.role);

            res.status(200).json({
                success: true,
                token,
                data: {
                    user: {
                        id: user._id,
                        name: user.name,
                        email: user.email,
                        role: user.role
                    }
                }
            });
        } catch (error) {
            console.error('Login error:', error);
            next(new ApiError(500, error.message));
        }
    };

    // For initial setup or internal use to create the first admin
    createAdmin = async (req, res, next) => {
        try {
            const { name, email, password } = req.body;
            
            console.log('Creating admin:', { name, email });
            
            // Check if user already exists
            const existingUser = await User.findOne({ email });
            if (existingUser) {
                console.log('User already exists');
                return next(new ApiError(400, 'User with this email already exists'));
            }
            
            const user = await User.create({
                name,
                email,
                password,
                role: 'admin'
            });

            console.log('Admin created successfully:', user._id);

            const token = signToken(user._id, user.role);

            res.status(201).json({
                success: true,
                token,
                data: {
                    user: {
                        id: user._id,
                        name: user.name,
                        email: user.email,
                        role: user.role
                    }
                }
            });
        } catch (error) {
            console.error('Create admin error:', error.message);
            next(new ApiError(400, error.message));
        }
    };
}

module.exports = new AuthController();
