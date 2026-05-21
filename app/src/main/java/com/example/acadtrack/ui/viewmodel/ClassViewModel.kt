package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.ClassModel
import com.example.acadtrack.data.repository.ClassRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClassViewModel(private val repository: ClassRepository) : ViewModel() {

    private val _classesState = MutableStateFlow<Resource<List<ClassModel>>>(Resource.Loading())
    val classesState: StateFlow<Resource<List<ClassModel>>> = _classesState.asStateFlow()

    private val _createClassState = MutableStateFlow<Resource<ClassModel>?>(null)
    val createClassState: StateFlow<Resource<ClassModel>?> = _createClassState.asStateFlow()

    private var currentDepartmentId: String? = null

    fun fetchClasses(departmentId: String? = currentDepartmentId, page: Int = 1) {
        currentDepartmentId = departmentId
        viewModelScope.launch {
            repository.getClasses(departmentId, page).collect {
                _classesState.value = it
            }
        }
    }

    fun createClass(name: String, departmentId: String, batchYear: Int) {
        viewModelScope.launch {
            repository.createClass(name, departmentId, batchYear).collect { state ->
                _createClassState.value = state
                if (state is Resource.Success) {
                    // Refresh the list with whatever filter was already active (or none)
                    fetchClasses(currentDepartmentId)
                }
            }
        }
    }

    fun resetCreateState() {
        _createClassState.value = null
    }

    fun deleteClass(id: String) {
        viewModelScope.launch {
            repository.deleteClass(id).collect { state ->
                if (state is Resource.Success) {
                    fetchClasses()
                }
            }
        }
    }
}
