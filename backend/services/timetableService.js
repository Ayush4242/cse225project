const Timetable = require('../models/Timetable');
const Section = require('../models/Section');
const Mapping = require('../models/Mapping');

class TimetableService {
    /**
     * Helper to convert "HH:mm" to minutes from start of day for comparison
     */
    _timeToMinutes(timeStr) {
        const [hours, minutes] = timeStr.split(':').map(Number);
        return hours * 60 + minutes;
    }

    /**
     * Check if two time slots overlap
     */
    _isOverlapping(slot1, slot2) {
        if (slot1.day !== slot2.day) return false;
        
        const start1 = this._timeToMinutes(slot1.startTime);
        const end1 = this._timeToMinutes(slot1.endTime);
        const start2 = this._timeToMinutes(slot2.startTime);
        const end2 = this._timeToMinutes(slot2.endTime);

        return start1 < end2 && start2 < end1;
    }

    /**
     * Add or Update a slot in a section's timetable
     */
    async addOrUpdateSlot(sectionId, slotData) {
        // 1. Validate section exists
        const section = await Section.findById(sectionId);
        if (!section) throw new Error('Section not found');

        // 2. Validate Mapping (Teacher must be assigned to Subject for this Section)
        const mapping = await Mapping.findOne({
            section: sectionId,
            subject: slotData.subject,
            teacher: slotData.teacher
        });
        if (!mapping) {
            throw new Error('Teacher is not assigned to this subject in this section');
        }

        // 3. Get or Create Timetable for section
        let timetable = await Timetable.findOne({ section: sectionId });
        if (!timetable) {
            timetable = new Timetable({ section: sectionId, slots: [] });
        }

        // 4. Check for overlap within the section's own timetable
        const sectionOverlap = timetable.slots.some(slot => this._isOverlapping(slot, slotData));
        if (sectionOverlap) {
            throw new Error('This time slot overlaps with another subject in this section');
        }

        // 5. Check for Teacher availability across ALL sections
        const teacherBusy = await Timetable.findOne({
            'slots': {
                $elemMatch: {
                    day: slotData.day,
                    teacher: slotData.teacher
                    // Complex overlap logic in MongoDB is hard, we'll fetch and check in JS
                }
            }
        });

        if (teacherBusy) {
            // Find specific overlaps for the teacher
            const teacherSchedules = await Timetable.find({ 'slots.teacher': slotData.teacher });
            for (const t of teacherSchedules) {
                const overlap = t.slots.find(slot => 
                    slot.teacher.toString() === slotData.teacher.toString() && 
                    this._isOverlapping(slot, slotData)
                );
                if (overlap) {
                    throw new Error(`Teacher is already busy in another section during this time`);
                }
            }
        }

        timetable.slots.push(slotData);
        await timetable.save();
        
        // Return populated timetable
        return await Timetable.findById(timetable._id)
            .populate({
                path: 'slots.subject',
                select: 'name code credits'
            })
            .populate({
                path: 'slots.teacher',
                populate: [
                    { path: 'user', select: 'name email' },
                    { path: 'department', select: 'name' }
                ]
            });
    }

    async getTodayClasses(teacherId) {
        const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        const today = days[new Date().getDay()];

        // Find all timetables that have slots for this teacher on this day
        const timetables = await Timetable.find({
            'slots': {
                $elemMatch: {
                    day: today,
                    teacher: teacherId
                }
            }
        })
        .populate('section', 'name')
        .populate({
            path: 'section',
            populate: { path: 'class', select: 'name' }
        })
        .populate('slots.subject', 'name code');

        const todayClasses = [];
        const now = new Date();
        const currentTimeInMinutes = now.getHours() * 60 + now.getMinutes();

        timetables.forEach(timetable => {
            timetable.slots.forEach(slot => {
                if (slot.day === today && slot.teacher.toString() === teacherId.toString()) {
                    const startTimeMins = this._timeToMinutes(slot.startTime);
                    const endTimeMins = this._timeToMinutes(slot.endTime);

                    let status = 'upcoming';
                    if (currentTimeInMinutes >= startTimeMins && currentTimeInMinutes <= endTimeMins) {
                        status = 'ongoing';
                    } else if (currentTimeInMinutes > endTimeMins) {
                        status = 'completed';
                    }

                    todayClasses.push({
                        slotId: slot._id,
                        subject: slot.subject,
                        section: timetable.section,
                        startTime: slot.startTime,
                        endTime: slot.endTime,
                        status: status
                    });
                }
            });
        });

        // Sort by start time
        return todayClasses.sort((a, b) => this._timeToMinutes(a.startTime) - this._timeToMinutes(b.startTime));
    }

    async getTimetableBySection(sectionId) {
        const timetable = await Timetable.findOne({ section: sectionId })
            .populate({
                path: 'slots.subject',
                select: 'name code credits'
            })
            .populate({
                path: 'slots.teacher',
                populate: [
                    { path: 'user', select: 'name email' },
                    { path: 'department', select: 'name' }
                ]
            });
        
        if (!timetable) return { section: sectionId, slots: [] };
        return timetable;
    }

    async removeSlot(sectionId, slotId) {
        const timetable = await Timetable.findOne({ section: sectionId });
        if (!timetable) throw new Error('Timetable not found');

        timetable.slots = timetable.slots.filter(slot => slot._id.toString() !== slotId);
        await timetable.save();
        
        // Return populated timetable
        return await Timetable.findById(timetable._id)
            .populate({
                path: 'slots.subject',
                select: 'name code credits'
            })
            .populate({
                path: 'slots.teacher',
                populate: [
                    { path: 'user', select: 'name email' },
                    { path: 'department', select: 'name' }
                ]
            });
    }
}

module.exports = new TimetableService();
