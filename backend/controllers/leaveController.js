const leaveService = require('../services/leaveService');

exports.createLeave = async (req, res) => {
    try {
        const data = {
            ...req.body,
            user: req.user.id,
            role: req.user.role // Automatically assigned based on logged-in user
        };
        const leave = await leaveService.createLeave(data);
        res.status(201).json({ success: true, data: leave });
    } catch (error) {
        res.status(400).json({ success: false, message: error.message });
    }
};

exports.getMyLeaves = async (req, res) => {
    try {
        const leaves = await leaveService.getMyLeaves(req.user.id);
        res.status(200).json({ success: true, data: leaves });
    } catch (error) {
        res.status(400).json({ success: false, message: error.message });
    }
};

exports.getAllPendingLeaves = async (req, res) => {
    try {
        const leaves = await leaveService.getAllPendingLeaves();
        res.status(200).json({ success: true, data: leaves });
    } catch (error) {
        res.status(400).json({ success: false, message: error.message });
    }
};

exports.getAllLeaves = async (req, res) => {
    try {
        // If the user isn't admin, they shouldn't be calling this, but middleware handles authorization
        const leaves = await leaveService.getAllLeaves();
        res.status(200).json({ success: true, data: leaves });
    } catch (error) {
        res.status(400).json({ success: false, message: error.message });
    }
};

exports.updateLeaveStatus = async (req, res) => {
    try {
        const { status } = req.body;
        if (!['approved', 'rejected'].includes(status)) {
            return res.status(400).json({ success: false, message: 'Invalid status' });
        }
        const leave = await leaveService.updateLeaveStatus(req.params.id, status);
        res.status(200).json({ success: true, data: leave });
    } catch (error) {
        res.status(400).json({ success: false, message: error.message });
    }
};
