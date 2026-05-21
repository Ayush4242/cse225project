package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Teacher
import com.example.acadtrack.data.repository.TeacherRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherViewModel(private val repository: TeacherRepository) : ViewModel() {

    private val _teachersState = MutableStateFlow<Resource<List<Teacher>>>(Resource.Loading())
    val teachersState: StateFlow<Resource<List<Teacher>>> = _teachersState.asStateFlow()

    private val _createTeacherState = MutableStateFlow<Resource<Teacher>?>(null)
    val createTeacherState: StateFlow<Resource<Teacher>?> = _createTeacherState.asStateFlow()

    fun fetchTeachers(departmentId: String? = null, page: Int = 1) {
        viewModelScope.launch {
            repository.getTeachers(departmentId, page).collect {
                _teachersState.value = it
            }
        }
    }

    fun createTeacher(
        name: String,
        email: String,
        password: String,
        employeeId: String,
        departmentId: String,
        designation: String,
        phoneNumber: String
    ) {
        viewModelScope.launch {
            repository.createTeacher(name, email, password, employeeId, departmentId, designation, phoneNumber).collect {
                _createTeacherState.value = it
                if (it is Resource.Success) {
                    fetchTeachers(departmentId)
                }
            }
        }
    }

    fun resetCreateState() {
        _createTeacherState.value = null
    }

    fun deleteTeacher(id: String) {
        viewModelScope.launch {
            repository.deleteTeacher(id).collect { state ->
                if (state is Resource.Success) {
                    fetchTeachers()
                }
            }
        }
    }
}
