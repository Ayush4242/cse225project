package com.example.acadtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.acadtrack.data.repository.LeaveRepository
import com.example.acadtrack.ui.navigation.Screen
import com.example.acadtrack.ui.screen.*
import com.example.acadtrack.ui.theme.AcadTrackTheme
import com.example.acadtrack.ui.viewmodel.AuthViewModel
import com.example.acadtrack.ui.viewmodel.TeacherPortalViewModel
import com.example.acadtrack.ui.viewmodel.StudentPortalViewModel
import com.example.acadtrack.ui.viewmodel.LeaveViewModel
import com.example.acadtrack.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcadTrackTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(context))
    val teacherPortalViewModel: TeacherPortalViewModel = viewModel(factory = ViewModelFactory(context))
    val studentPortalViewModel: StudentPortalViewModel = viewModel(factory = ViewModelFactory(context))
    val leaveViewModel: LeaveViewModel = viewModel(factory = ViewModelFactory(context))
    
    val startDestination = if (authViewModel.isLoggedIn()) {
        when (authViewModel.getUserRole()) {
            "teacher" -> Screen.TeacherDashboard.route
            "student" -> Screen.StudentDashboard.route
            "admin" -> Screen.Dashboard.route
            else -> Screen.Login.route
        }
    } else {
        Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = {
                val route = when (authViewModel.getUserRole()) {
                    "teacher" -> Screen.TeacherDashboard.route
                    "student" -> Screen.StudentDashboard.route
                    "admin" -> Screen.Dashboard.route
                    else -> Screen.Dashboard.route
                }
                navController.navigate(route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Departments.route) { DepartmentScreen() }
        composable(Screen.Classes.route) { ClassScreen() }
        composable(Screen.Sections.route) { SectionScreen() }
        composable(Screen.Subjects.route) { SubjectScreen() }
        composable(Screen.Teachers.route) { TeacherScreen() }
        composable(Screen.Students.route) { StudentScreen() }
        composable(Screen.Mappings.route) { MappingScreen() }
        composable(Screen.Enrollments.route) { EnrollmentScreen() }
        composable(Screen.Timetable.route) { TimetableScreen() }
        composable(Screen.Holidays.route) { 
            HolidayScreen(isAdmin = authViewModel.getUserRole() == "admin") 
        }
        composable(Screen.Notifications.route) { 
            NotificationScreen(isAdmin = authViewModel.getUserRole() == "admin") 
        }
        composable(Screen.Leaves.route) {
            LeaveScreen(
                viewModel = leaveViewModel,
                isAdmin = authViewModel.getUserRole() == "admin",
                onBack = { navController.popBackStack() }
            )
        }

        // Teacher Portal
        composable(Screen.TeacherDashboard.route) {
            TeacherDashboardScreen(
                viewModel = teacherPortalViewModel,
                userName = authViewModel.getUserName(),
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.TeacherDashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.TeacherSession.route) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
            val slotId = backStackEntry.arguments?.getString("slotId") ?: ""
            TeacherSessionScreen(
                subjectId = subjectId,
                sectionId = sectionId,
                slotId = slotId,
                viewModel = teacherPortalViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TeacherAnalytics.route) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
            TeacherAnalyticsScreen(
                sectionId = sectionId,
                viewModel = teacherPortalViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TeacherTimetable.route) {
            TeacherTimetableScreen(
                teacherViewModel = teacherPortalViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Student Portal
        composable(Screen.StudentDashboard.route) {
            StudentDashboardScreen(
                viewModel = studentPortalViewModel,
                userName = authViewModel.getUserName(),
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.StudentDashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.StudentScanner.route) {
            QrScannerScreen(
                onQrScanned = { sessionId, token ->
                    navController.navigate(Screen.StudentFaceVerification.createRoute(sessionId, token))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.StudentFaceVerification.route) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""
            FaceVerificationScreen(
                sessionId = sessionId,
                token = token,
                viewModel = studentPortalViewModel,
                onSuccess = {
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.StudentDashboard.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.StudentAnalytics.route) {
            StudentAnalyticsScreen(
                viewModel = studentPortalViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
