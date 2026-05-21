package com.example.acadtrack.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Departments : Screen("departments")
    object Classes : Screen("classes")
    object Sections : Screen("sections")
    object Subjects : Screen("subjects")
    object Teachers : Screen("teachers")
    object Students : Screen("students")
    object Mappings : Screen("mappings")
    object Enrollments : Screen("enrollments")
    object Timetable : Screen("timetable")
    object Holidays : Screen("holidays")
    object Leaves : Screen("leaves")
    object Notifications : Screen("notifications")
    
    // Teacher Portal
    object TeacherDashboard : Screen("teacher_dashboard")
    object TeacherSession : Screen("teacher_session/{subjectId}/{sectionId}/{slotId}") {
        fun createRoute(subjectId: String, sectionId: String, slotId: String) = "teacher_session/$subjectId/$sectionId/$slotId"
    }
    object TeacherAnalytics : Screen("teacher_analytics/{sectionId}") {
        fun createRoute(sectionId: String) = "teacher_analytics/$sectionId"
    }
    object TeacherTimetable : Screen("teacher_timetable")

    // Student Portal
    object StudentDashboard : Screen("student_dashboard")
    object StudentScanner : Screen("student_scanner")
    object StudentFaceVerification : Screen("student_face_verification/{sessionId}/{token}") {
        fun createRoute(sessionId: String, token: String) = "student_face_verification/$sessionId/$token"
    }
    object StudentAnalytics : Screen("student_analytics")
}
