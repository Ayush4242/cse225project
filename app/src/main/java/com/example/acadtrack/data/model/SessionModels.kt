package com.example.acadtrack.data.model

import com.google.gson.annotations.SerializedName

data class Session(
    @SerializedName("_id") val id: String,
    val teacher: String,
    val subject: Subject,
    val section: Section,
    val startTime: String,
    val endTime: String?,
    val isActive: Boolean,
    val qrCodeToken: String,
    val qrExpiry: String,
    val location: Location?,
    val timetableSlotId: String?
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class StartSessionRequest(
    val subjectId: String,
    val sectionId: String,
    val timetableSlotId: String,
    val location: Location? = null
)

data class SessionResponse(
    val success: Boolean,
    val data: Session?,
    val message: String? = null
)

data class SessionListResponse(
    val success: Boolean,
    val data: List<Session>?,
    val message: String? = null
)

data class QrRefreshResponse(
    val success: Boolean,
    val data: QrData?,
    val message: String?
)

data class QrData(
    val qrCodeToken: String,
    val qrExpiry: String
)

data class MarkAttendanceRequest(
    val sessionId: String,
    val qrToken: String,
    val location: Location? = null,
    val faceVerified: Boolean = false,
    val deviceId: String? = null
)

data class Attendance(
    @SerializedName("_id") val id: String,
    val student: Student?,
    val session: String,
    val timestamp: String,
    val status: String,
    val location: Location?,
    val faceVerified: Boolean
)

data class AttendanceResponse(
    val success: Boolean,
    val data: Attendance?,
    val message: String?
)

data class AttendanceListResponse(
    val success: Boolean,
    val count: Int,
    val data: List<Attendance>?
)

data class SectionAnalytics(
    val studentId: String,
    val name: String,
    val rollNumber: String,
    val attendanceCount: Int,
    val totalSessions: Int,
    val percentage: Double
)

data class SectionAnalyticsResponse(
    val success: Boolean,
    val data: List<SectionAnalytics>?,
    val message: String? = null
)

data class StudentAnalyticsResponse(
    val success: Boolean,
    val data: List<Map<String, Any>>?,
    val message: String? = null
)

data class TodayClass(
    val slotId: String,
    val subject: Subject,
    val section: Section,
    val startTime: String,
    val endTime: String,
    val status: String // upcoming, ongoing, completed
)

data class TodayClassesResponse(
    val success: Boolean,
    val data: List<TodayClass>?,
    val message: String? = null
)
