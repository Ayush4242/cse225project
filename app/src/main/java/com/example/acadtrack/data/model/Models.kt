package com.example.acadtrack.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val id: String,
    val name: String,
    val email: String,
    val role: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String?,
    val data: AuthData?,
    val message: String?
)

data class AuthData(
    val user: User
)

// Request Models
data class LoginRequest(
    val email: String,
    val password: String
)

data class CreateDepartmentRequest(
    val name: String,
    val description: String?
)

data class CreateClassRequest(
    val name: String,
    val department: String,
    val batchYear: Int
)

data class CreateSectionRequest(
    val name: String,
    @SerializedName("class") val classId: String,
    val roomNumber: String?
)

data class CreateTeacherRequest(
    val name: String,
    val email: String,
    val password: String,
    val employeeId: String,
    val department: String,
    val designation: String?,
    val phoneNumber: String?
)

data class CreateSubjectRequest(
    val name: String,
    val code: String,
    val department: String,
    val credits: Int
)

data class CreateStudentRequest(
    val name: String,
    val email: String,
    val password: String,
    val rollNumber: String,
    val section: String,
    val parentContact: String?,
    val admissionYear: Int
)

data class CreateMappingRequest(
    val teacher: String,
    val subject: String,
    val section: String
)

data class CreateHolidayRequest(
    val title: String,
    val startDate: String,
    val endDate: String,
    val description: String?,
    val type: String,
    val targetRole: String
)

data class CreateNotificationRequest(
    val title: String,
    val message: String,
    val targetRole: String
)

data class CreateTimetableSlotRequest(
    val day: String,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val teacher: String
)

// Response Models
data class Department(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String?
)

data class DepartmentResponse(
    val success: Boolean,
    val departments: List<Department>?,
    val data: Department?,
    val message: String?
)

data class ClassModel(
    @SerializedName("_id") val id: String,
    val name: String,
    val department: Department?,
    val batchYear: Int
)

data class ClassResponse(
    val success: Boolean,
    val classes: List<ClassModel>?,
    val data: ClassModel?,
    val message: String?
)

data class Section(
    @SerializedName("_id") val id: String? = null,
    val name: String? = null,
    @SerializedName("class") val classObj: ClassModel? = null,
    val roomNumber: String? = null
)

data class SectionResponse(
    val success: Boolean,
    val sections: List<Section>?,
    val data: Section?,
    val message: String?
)

data class Teacher(
    @SerializedName("_id") val id: String,
    val user: User?,
    val employeeId: String,
    val department: Department?,
    val designation: String?,
    val phoneNumber: String?
)

data class TeacherResponse(
    val success: Boolean,
    val teachers: List<Teacher>?,
    val data: Teacher?,
    val message: String?
)

data class Subject(
    @SerializedName("_id") val id: String,
    val name: String,
    val code: String,
    val department: Department? = null,  // Can be null when just created
    val credits: Int
)

data class SubjectResponse(
    val success: Boolean,
    val subjects: List<Subject>?,
    val data: Subject?,
    val message: String?
)

data class Student(
    @SerializedName("_id") val id: String,
    val user: User?,
    val rollNumber: String,
    val section: Any?,
    val parentContact: String?,
    val admissionYear: Int
)

data class StudentResponse(
    val success: Boolean,
    val students: List<Student>?,
    val data: Student?,
    val message: String?,
    val successCount: Int? = null,
    val errors: List<Map<String, String>>? = null
)

data class Mapping(
    @SerializedName("_id") val id: String,
    val teacher: Teacher?,
    val subject: Subject?,
    val section: Section?
)

data class MappingResponse(
    val success: Boolean,
    val mappings: List<Mapping>?,
    val data: Mapping?,
    val message: String?
)

data class MappingListResponse(
    val success: Boolean,
    val data: List<Mapping>?,
    val message: String?
)

data class TimeSlot(
    @SerializedName("_id") val id: String?,
    val day: String,
    val startTime: String,
    val endTime: String,
    val subject: Subject?,
    val teacher: Teacher?
)

data class Timetable(
    @SerializedName("_id") val id: String,
    val section: String,
    val slots: List<TimeSlot>
)

data class TimetableResponse(
    val success: Boolean,
    val data: Timetable?,
    val message: String?
)

