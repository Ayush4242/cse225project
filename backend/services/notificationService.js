const Notification = require('../models/Notification');

class NotificationService {
    async createNotification(notificationData, adminId) {
        return await Notification.create({
            ...notificationData,
            createdBy: adminId
        });
    }

    async getNotifications(role) {
        const filter = {};
        if (role && role !== 'admin') {
            filter.targetRole = { $in: ['all', role] };
        }
        return await Notification.find(filter)
            .sort({ createdAt: -1 });
    }

    async deleteNotification(id) {
        const notification = await Notification.findByIdAndDelete(id);
        if (!notification) throw new Error('Notification not found');
        return notification;
    }
}

module.exports = new NotificationService();
