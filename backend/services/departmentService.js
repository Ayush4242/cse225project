const Department = require('../models/Department');

/**
 * Service for Department related business logic
 */
class DepartmentService {
    /**
     * Create a new department
     * @param {Object} deptData 
     * @returns {Promise<Object>}
     */
    async createDepartment(deptData) {
        const existingDept = await Department.findOne({ name: deptData.name.toUpperCase() });
        if (existingDept) {
            throw new Error('Department with this name already exists');
        }
        return await Department.create(deptData);
    }

    /**
     * Get all departments with pagination
     * @param {number} page 
     * @param {number} limit 
     * @returns {Promise<Object>}
     */
    async getAllDepartments(page = 1, limit = 10) {
        const skip = (page - 1) * limit;
        const departments = await Department.find()
            .sort({ name: 1 })
            .skip(skip)
            .limit(limit);
        
        const total = await Department.countDocuments();
        
        return {
            departments,
            totalPages: Math.ceil(total / limit),
            currentPage: page,
            totalDepartments: total
        };
    }

    /**
     * Get department by ID
     * @param {string} id 
     * @returns {Promise<Object>}
     */
    async getDepartmentById(id) {
        const department = await Department.findById(id);
        if (!department) {
            throw new Error('Department not found');
        }
        return department;
    }

    /**
     * Update department
     * @param {string} id 
     * @param {Object} updateData 
     * @returns {Promise<Object>}
     */
    async updateDepartment(id, updateData) {
        const department = await Department.findByIdAndUpdate(id, updateData, {
            new: true,
            runValidators: true
        });
        if (!department) {
            throw new Error('Department not found');
        }
        return department;
    }

    /**
     * Delete department
     * @param {string} id 
     * @returns {Promise<Object>}
     */
    async deleteDepartment(id) {
        const department = await Department.findByIdAndDelete(id);
        if (!department) {
            throw new Error('Department not found');
        }
        return department;
    }
}

module.exports = new DepartmentService();
