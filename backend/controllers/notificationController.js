const notificationService = require('../services/notificationService');
const ApiError = require('../utils/apiError');

class NotificationController {
    createNotification = async (req, res, next) => {
        try {
            const notification = await notificationService.createNotification(req.body, req.user.id);
            res.status(201).json({
                success: true,
                data: notification
            });
        } catch (error) {
            next(new ApiError(400, error.message));
        }
    };

    getNotifications = async (req, res, next) => {
        try {
            const notifications = await notificationService.getNotifications(req.query.role);
            res.status(200).json({
                success: true,
                data: notifications
            });
        } catch (error) {
            next(new ApiError(500, error.message));
        }
    };

    deleteNotification = async (req, res, next) => {
        try {
            await notificationService.deleteNotification(req.params.id);
            res.status(200).json({
                success: true,
                message: 'Notification deleted successfully'
            });
        } catch (error) {
            next(new ApiError(404, error.message));
        }
    };
}

module.exports = new NotificationController();
