const Mapping = require('../models/Mapping');
const Teacher = require('../models/Teacher');
const Subject = require('../models/Subject');
const Section = require('../models/Section');

class MappingService {
    async createMapping(mappingData) {
        // Validate Teacher, Subject, Section exist
        const [teacher, subject, section] = await Promise.all([
            Teacher.findById(mappingData.teacher),
            Subject.findById(mappingData.subject),
            Section.findById(mappingData.section)
        ]);

        if (!teacher) throw new Error('Teacher not found');
        if (!subject) throw new Error('Subject not found');
        if (!section) throw new Error('Section not found');

        // Check for existing mapping
        const existingMapping = await Mapping.findOne({
            subject: mappingData.subject,
            section: mappingData.section
        });

        if (existingMapping) {
            throw new Error('This subject is already assigned to a teacher in this section');
        }

        const newMapping = await Mapping.create(mappingData);
        
        // Return populated mapping
        return await Mapping.findById(newMapping._id)
            .populate({
                path: 'teacher',
                populate: [
                    { path: 'user', select: 'name email' },
                    { path: 'department', select: 'name' }
                ]
            })
            .populate('subject', 'name code credits')
            .populate({
                path: 'section',
                populate: {
                    path: 'class',
                    select: 'name batchYear',
                    populate: { path: 'department', select: 'name' }
                }
            });
    }

    async getMappings(query = {}) {
        const { section, teacher, page = 1, limit = 20 } = query;
        const filter = {};
        if (section) filter.section = section;
        if (teacher) filter.teacher = teacher;

        const skip = (page - 1) * limit;
        const mappings = await Mapping.find(filter)
            .populate({
                path: 'teacher',
                populate: [
                    { path: 'user', select: 'name email' },
                    { path: 'department', select: 'name' }
                ]
            })
            .populate('subject', 'name code credits')
            .populate({
                path: 'section',
                populate: {
                    path: 'class',
                    select: 'name batchYear',
                    populate: { path: 'department', select: 'name' }
                }
            })
            .skip(skip)
            .limit(limit);

        const total = await Mapping.countDocuments(filter);

        return {
            mappings,
            totalPages: Math.ceil(total / limit),
            currentPage: page,
            totalMappings: total
        };
    }

    async deleteMapping(id) {
        const mapping = await Mapping.findByIdAndDelete(id);
        if (!mapping) throw new Error('Mapping not found');
        return mapping;
    }
}

module.exports = new MappingService();
