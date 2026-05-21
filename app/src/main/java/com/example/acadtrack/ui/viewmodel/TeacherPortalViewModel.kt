package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.*
import com.example.acadtrack.data.repository.TeacherRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherPortalViewModel(private val repository: TeacherRepository) : ViewModel() {

    private val _myClassesState = MutableStateFlow<Resource<List<Mapping>>>(Resource.Loading())
    val myClassesState: StateFlow<Resource<List<Mapping>>> = _myClassesState.asStateFlow()

    private val _todayClassesState = MutableStateFlow<Resource<List<TodayClass>>>(Resource.Loading())
    val todayClassesState: StateFlow<Resource<List<TodayClass>>> = _todayClassesState.asStateFlow()

    private val _activeSessionState = MutableStateFlow<Resource<Session?>>(Resource.Loading())
    val activeSessionState: StateFlow<Resource<Session?>> = _activeSessionState.asStateFlow()

    private val _attendanceListState = MutableStateFlow<Resource<List<Attendance>>>(Resource.Loading())
    val attendanceListState: StateFlow<Resource<List<Attendance>>> = _attendanceListState.asStateFlow()

    private val _analyticsState = MutableStateFlow<Resource<List<SectionAnalytics>>>(Resource.Loading())
    val analyticsState: StateFlow<Resource<List<SectionAnalytics>>> = _analyticsState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<Resource<Teacher>?>(null)
    val updateProfileState: StateFlow<Resource<Teacher>?> = _updateProfileState.asStateFlow()

    private var qrRefreshJob: Job? = null

    fun fetchMyClasses() {
        viewModelScope.launch {
            repository.getMyClasses().collect {
                _myClassesState.value = it
            }
        }
    }

    fun fetchTodayClasses() {
        viewModelScope.launch {
            repository.getTodayClasses().collect {
                _todayClassesState.value = it
            }
        }
    }

    fun fetchActiveSession() {
        viewModelScope.launch {
            repository.getActiveSession().collect {
                _activeSessionState.value = it
                if (it is Resource.Success && it.data != null) {
                    startQrAutoRefresh(it.data.id)
                    fetchAttendance(it.data.id)
                }
            }
        }
    }

    fun startSession(subjectId: String, sectionId: String, timetableSlotId: String) {
        viewModelScope.launch {
            repository.startSession(subjectId, sectionId, timetableSlotId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val session = resource.data
                        _activeSessionState.value = Resource.Success(session)
                        if (session != null) {
                            startQrAutoRefresh(session.id)
                            fetchAttendance(session.id)
                        }
                    }
                    is Resource.Error -> {
                        _activeSessionState.value = Resource.Error(resource.message ?: "Error starting session")
                    }
                    is Resource.Loading -> {
                        _activeSessionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun endSession(sessionId: String) {
        viewModelScope.launch {
            repository.endSession(sessionId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        stopPollingAndReset()
                    }
                    is Resource.Error -> {
                        // If session already ended (404) or other error, we still want to stop polling
                        // if the teacher intended to end it.
                        if (resource.message?.contains("not found", ignoreCase = true) == true) {
                            stopPollingAndReset()
                        } else {
                            _activeSessionState.value = Resource.Error(resource.message ?: "Error ending session")
                        }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    private fun stopPollingAndReset() {
        qrRefreshJob?.cancel()
        qrRefreshJob = null
        _activeSessionState.value = Resource.Success(null)
        _attendanceListState.value = Resource.Success(emptyList())
        fetchTodayClasses()
    }

    fun fetchAttendance(sessionId: String) {
        viewModelScope.launch {
            repository.getSessionAttendance(sessionId).collect {
                _attendanceListState.value = it
            }
        }
    }

    fun fetchAnalytics(sectionId: String) {
        viewModelScope.launch {
            // Need to add this to repository
            repository.getSectionAnalytics(sectionId).collect {
                _analyticsState.value = it
            }
        }
    }

    fun updateProfile(name: String?, password: String?) {
        viewModelScope.launch {
            repository.updateProfile(name, password).collect {
                _updateProfileState.value = it
            }
        }
    }

    private fun startQrAutoRefresh(sessionId: String) {
        qrRefreshJob?.cancel()
        qrRefreshJob = viewModelScope.launch {
            while (true) {
                delay(45000) // Refresh every 45 seconds (QR expires in 60s)
                repository.refreshQr(sessionId).collect { resource ->
                    if (resource is Resource.Success && resource.data != null) {
                        val currentSession = (_activeSessionState.value as? Resource.Success)?.data
                        if (currentSession != null) {
                            _activeSessionState.value = Resource.Success(
                                currentSession.copy(
                                    qrCodeToken = resource.data.qrCodeToken,
                                    qrExpiry = resource.data.qrExpiry,
                                )
                            )
                        }
                    }
                }
                // Also refresh attendance list periodically
                fetchAttendance(sessionId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        qrRefreshJob?.cancel()
    }
}
