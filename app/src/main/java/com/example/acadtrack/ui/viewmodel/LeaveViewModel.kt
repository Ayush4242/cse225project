package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Leave
import com.example.acadtrack.data.repository.LeaveRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaveViewModel(private val repository: LeaveRepository) : ViewModel() {

    private val _leavesState = MutableStateFlow<Resource<List<Leave>>>(Resource.Loading())
    val leavesState: StateFlow<Resource<List<Leave>>> = _leavesState.asStateFlow()

    fun fetchMyLeaves() {
        viewModelScope.launch {
            repository.getMyLeaves().collect {
                _leavesState.value = it
            }
        }
    }

    fun fetchPendingLeaves() {
        viewModelScope.launch {
            repository.getAllPendingLeaves().collect {
                _leavesState.value = it
            }
        }
    }

    fun createLeave(startDate: String, endDate: String, reason: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.createLeave(startDate, endDate, reason).collect { result ->
                if (result is Resource.Success) {
                    fetchMyLeaves()
                    onResult(true, null)
                } else if (result is Resource.Error) {
                    onResult(false, result.message)
                }
            }
        }
    }

    fun updateLeaveStatus(id: String, status: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.updateLeaveStatus(id, status).collect { result ->
                if (result is Resource.Success) {
                    fetchPendingLeaves() // Refresh the list
                    onResult(true, null)
                } else if (result is Resource.Error) {
                    onResult(false, result.message)
                }
            }
        }
    }
}
