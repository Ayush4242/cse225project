const Leave = require('../models/Leave');

class LeaveService {
    async createLeave(data) {
        // Check for overlapping leaves for the same user
        const existingLeave = await Leave.findOne({
            user: data.user,
            status: { $ne: 'rejected' }, // Ignore rejected leaves
            startDate: { $lte: new Date(data.endDate) },
            endDate: { $gte: new Date(data.startDate) }
        });

        if (existingLeave) {
            throw new Error('You have already applied for a leave during these dates.');
        }

        const leave = new Leave(data);
        await leave.save();
        return await Leave.findById(leave._id).populate('user', 'name email role');
    }

    async getMyLeaves(userId) {
        return await Leave.find({ user: userId }).sort({ createdAt: -1 }).populate('user', 'name email role');
    }

    async getAllPendingLeaves() {
        return await Leave.find({ status: 'pending' }).sort({ createdAt: 1 }).populate('user', 'name email role');
    }

    async getAllLeaves() {
        return await Leave.find().sort({ createdAt: -1 }).populate('user', 'name email role');
    }

    async updateLeaveStatus(leaveId, status) {
        const leave = await Leave.findById(leaveId);
        if (!leave) {
            throw new Error('Leave request not found');
        }
        leave.status = status;
        await leave.save();
        return await Leave.findById(leaveId).populate('user', 'name email role');
    }
}

module.exports = new LeaveService();
