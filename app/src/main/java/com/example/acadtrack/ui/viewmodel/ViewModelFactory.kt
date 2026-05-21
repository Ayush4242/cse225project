package com.example.acadtrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acadtrack.data.network.RetrofitClient
import com.example.acadtrack.data.repository.*

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val apiService = RetrofitClient.getApiService(context)
        
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(AuthRepository(apiService, context)) as T
            }
            modelClass.isAssignableFrom(DepartmentViewModel::class.java) -> {
                DepartmentViewModel(DepartmentRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(ClassViewModel::class.java) -> {
                ClassViewModel(ClassRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(SectionViewModel::class.java) -> {
                SectionViewModel(SectionRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(TeacherViewModel::class.java) -> {
                TeacherViewModel(TeacherRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(SubjectViewModel::class.java) -> {
                SubjectViewModel(SubjectRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(StudentViewModel::class.java) -> {
                StudentViewModel(StudentRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(MappingViewModel::class.java) -> {
                MappingViewModel(MappingRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(TimetableViewModel::class.java) -> {
                TimetableViewModel(TimetableRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(HolidayViewModel::class.java) -> {
                HolidayViewModel(HolidayRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(NotificationRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(TeacherPortalViewModel::class.java) -> {
                TeacherPortalViewModel(TeacherRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(StudentPortalViewModel::class.java) -> {
                StudentPortalViewModel(StudentRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(EnrollmentViewModel::class.java) -> {
                EnrollmentViewModel(context) as T
            }
            modelClass.isAssignableFrom(LeaveViewModel::class.java) -> {
                LeaveViewModel(LeaveRepository(apiService)) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
