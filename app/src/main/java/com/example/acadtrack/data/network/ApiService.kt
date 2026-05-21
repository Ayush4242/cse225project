package com.example.acadtrack.data.network

import com.example.acadtrack.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Auth
    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<AuthResponse>

    // Departments
    @GET("departments")
    suspend fun getDepartments(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<DepartmentResponse>

    @POST("departments")
    suspend fun createDepartment(@Body department: CreateDepartmentRequest): Response<DepartmentResponse>

    @DELETE("departments/{id}")
    suspend fun deleteDepartment(@Path("id") id: String): Response<DepartmentResponse>

    // Classes
    @GET("classes")
    suspend fun getClasses(
        @Query("department") departmentId: String? = null,
        @Query("page") page: Int = 1
    ): Response<ClassResponse>

    @POST("classes")
    suspend fun createClass(@Body classData: CreateClassRequest): Response<ClassResponse>

    @DELETE("classes/{id}")
    suspend fun deleteClass(@Path("id") id: String): Response<ClassResponse>

    // Sections
    @GET("sections")
    suspend fun getSections(
        @Query("classId") classId: String? = null,
        @Query("page") page: Int = 1
    ): Response<SectionResponse>

    @POST("sections")
    suspend fun createSection(@Body sectionData: CreateSectionRequest): Response<SectionResponse>

    @DELETE("sections/{id}")
    suspend fun deleteSection(@Path("id") id: String): Response<SectionResponse>

    // Teachers
    @GET("teachers")
    suspend fun getTeachers(
        @Query("department") departmentId: String? = null,
        @Query("page") page: Int = 1
    ): Response<TeacherResponse>

    @POST("teachers")
    suspend fun createTeacher(@Body teacherData: CreateTeacherRequest): Response<TeacherResponse>

    @DELETE("teachers/{id}")
    suspend fun deleteTeacher(@Path("id") id: String): Response<TeacherResponse>

    // Subjects
    @GET("subjects")
    suspend fun getSubjects(
        @Query("department") departmentId: String? = null,
        @Query("page") page: Int = 1
    ): Response<SubjectResponse>

    @POST("subjects")
    suspend fun createSubject(@Body subjectData: CreateSubjectRequest): Response<SubjectResponse>

    @DELETE("subjects/{id}")
    suspend fun deleteSubject(@Path("id") id: String): Response<SubjectResponse>

    // Students
    @GET("students")
    suspend fun getStudents(
        @Query("section") sectionId: String? = null,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null
    ): Response<StudentResponse>

    @POST("students")
    suspend fun createStudent(@Body studentData: CreateStudentRequest): Response<StudentResponse>

    @DELETE("students/{id}")
    suspend fun deleteStudent(@Path("id") id: String): Response<StudentResponse>

    @Multipart
    @POST("students/bulk-upload")
    suspend fun bulkUploadStudents(@Part file: MultipartBody.Part): Response<StudentResponse>

    // Mappings
    @GET("mappings")
    suspend fun getMappings(
        @Query("section") sectionId: String? = null,
        @Query("teacher") teacherId: String? = null
    ): Response<MappingResponse>

    @POST("mappings")
    suspend fun createMapping(@Body mappingData: CreateMappingRequest): Response<MappingResponse>

    @DELETE("mappings/{id}")
    suspend fun deleteMapping(@Path("id") id: String): Response<MappingResponse>

    // Enrollments
    @GET("enrollments")
    suspend fun getEnrollments(
        @Query("mappingId") mappingId: String? = null,
        @Query("studentId") studentId: String? = null
    ): Response<EnrollmentResponse>

    @POST("enrollments")
    suspend fun createEnrollment(@Body enrollmentData: CreateEnrollmentRequest): Response<SingleEnrollmentResponse>

    @DELETE("enrollments/{id}")
    suspend fun deleteEnrollment(@Path("id") id: String): Response<DeleteEnrollmentResponse>

    // Timetable
    @GET("timetable/{sectionId}")
    suspend fun getTimetable(@Path("sectionId") sectionId: String): Response<TimetableResponse>

    @POST("timetable/{sectionId}/slots")
    suspend fun addTimetableSlot(
        @Path("sectionId") sectionId: String,
        @Body slotData: CreateTimetableSlotRequest
    ): Response<TimetableResponse>

    @DELETE("timetable/{sectionId}/slots/{slotId}")
    suspend fun deleteTimetableSlot(
        @Path("sectionId") sectionId: String,
        @Path("slotId") slotId: String
    ): Response<TimetableResponse>

    // Holidays
    @GET("holidays")
    suspend fun getHolidays(): Response<HolidayListResponse>

    @POST("holidays")
    suspend fun createHoliday(@Body holidayData: CreateHolidayRequest): Response<HolidayResponse>

    @DELETE("holidays/{id}")
    suspend fun deleteHoliday(@Path("id") id: String): Response<HolidayResponse>

    // Notifications
    @GET("notifications")
    suspend fun getNotifications(@Query("role") role: String? = null): Response<NotificationListResponse>

    @POST("notifications")
    suspend fun createNotification(@Body notificationData: CreateNotificationRequest): Response<NotificationResponse>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<NotificationResponse>

    // Teacher Portal
    @GET("teachers/my-classes")
    suspend fun getMyClasses(): Response<MappingListResponse>

    @GET("teachers/today-classes")
    suspend fun getTodayClasses(): Response<TodayClassesResponse>

    @PUT("teachers/update-profile")
    suspend fun updateTeacherProfile(@Body updateData: Map<String, String>): Response<TeacherResponse>

    // Leaves
    @POST("leaves")
    suspend fun createLeave(@Body request: CreateLeaveRequest): Response<SingleLeaveResponse>

    @GET("leaves/my")
    suspend fun getMyLeaves(): Response<LeaveResponse>

    @GET("leaves/pending")
    suspend fun getAllPendingLeaves(): Response<LeaveResponse>

    @GET("leaves/all")
    suspend fun getAllLeaves(): Response<LeaveResponse>

    @PUT("leaves/{id}/status")
    suspend fun updateLeaveStatus(
        @Path("id") id: String,
        @Body request: UpdateLeaveStatusRequest
    ): Response<SingleLeaveResponse>

    // Sessions
    @POST("sessions/start")
    suspend fun startSession(@Body request: StartSessionRequest): Response<SessionResponse>

    @POST("sessions/{sessionId}/refresh")
    suspend fun refreshQr(@Path("sessionId") sessionId: String): Response<QrRefreshResponse>

    @POST("sessions/{sessionId}/end")
    suspend fun endSession(@Path("sessionId") sessionId: String): Response<SessionResponse>

    @GET("sessions/active")
    suspend fun getActiveSession(): Response<SessionResponse>

    @GET("sessions/student/active")
    suspend fun getStudentActiveSessions(): Response<SessionListResponse>

    // Attendance
    @POST("attendance/mark")
    suspend fun markAttendance(@Body request: MarkAttendanceRequest): Response<AttendanceResponse>

    @GET("attendance/my-analytics")
    suspend fun getStudentAnalytics(): Response<StudentAnalyticsResponse>

    @GET("attendance/session/{sessionId}")
    suspend fun getSessionAttendance(@Path("sessionId") sessionId: String): Response<AttendanceListResponse>

    @GET("attendance/analytics/section/{sectionId}")
    suspend fun getSectionAnalytics(@Path("sectionId") sectionId: String): Response<SectionAnalyticsResponse>
}

// Added missing list response models for convenience
data class HolidayListResponse(val success: Boolean, val data: List<Holiday>?, val message: String?)
data class NotificationListResponse(val success: Boolean, val data: List<Notification>?, val message: String?)
