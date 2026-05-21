package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.*
import com.example.acadtrack.data.repository.StudentRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentPortalViewModel(private val repository: StudentRepository) : ViewModel() {

    private val _attendanceState = MutableStateFlow<Resource<Attendance>?>(null)
    val attendanceState: StateFlow<Resource<Attendance>?> = _attendanceState.asStateFlow()

    private val _analyticsState = MutableStateFlow<Resource<List<Map<String, Any>>>>(Resource.Loading())
    val analyticsState: StateFlow<Resource<List<Map<String, Any>>>> = _analyticsState.asStateFlow()

    fun markAttendance(
        sessionId: String,
        qrToken: String,
        latitude: Double?,
        longitude: Double?,
        faceVerified: Boolean,
        deviceId: String?
    ) {
        viewModelScope.launch {
            val location = if (latitude != null && longitude != null) {
                Location(latitude, longitude)
            } else null

            val request = MarkAttendanceRequest(
                sessionId = sessionId,
                qrToken = qrToken,
                location = location,
                faceVerified = faceVerified,
                deviceId = deviceId
            )

            repository.markAttendance(request).collect {
                _attendanceState.value = it
            }
        }
    }

    fun fetchAnalytics() {
        viewModelScope.launch {
            repository.getStudentAnalytics().collect {
                _analyticsState.value = it
            }
        }
    }

    private val _activeSessionsState = MutableStateFlow<Resource<List<Session>>>(Resource.Loading())
    val activeSessionsState: StateFlow<Resource<List<Session>>> = _activeSessionsState.asStateFlow()

    fun fetchActiveSessions() {
        viewModelScope.launch {
            repository.getActiveSessions().collect {
                _activeSessionsState.value = it
            }
        }
    }

    fun resetAttendanceState() {
        _attendanceState.value = null
    }
}
