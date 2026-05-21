package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Department
import com.example.acadtrack.data.repository.DepartmentRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DepartmentViewModel(private val repository: DepartmentRepository) : ViewModel() {

    private val _departmentsState = MutableStateFlow<Resource<List<Department>>>(Resource.Loading())
    val departmentsState: StateFlow<Resource<List<Department>>> = _departmentsState.asStateFlow()

    private val _createDepartmentState = MutableStateFlow<Resource<Department>?>(null)
    val createDepartmentState: StateFlow<Resource<Department>?> = _createDepartmentState.asStateFlow()

    init {
        fetchDepartments()
    }

    fun fetchDepartments(page: Int = 1, limit: Int = 50) {
        viewModelScope.launch {
            repository.getDepartments(page, limit).collect {
                _departmentsState.value = it
            }
        }
    }

    fun createDepartment(name: String, description: String) {
        viewModelScope.launch {
            repository.createDepartment(name, description).collect {
                _createDepartmentState.value = it
                if (it is Resource.Success) {
                    fetchDepartments() // Refresh list
                }
            }
        }
    }

    fun deleteDepartment(id: String) {
        viewModelScope.launch {
            repository.deleteDepartment(id).collect {
                if (it is Resource.Success) {
                    fetchDepartments() // Refresh list
                }
            }
        }
    }

    fun resetCreateState() {
        _createDepartmentState.value = null
    }
}
