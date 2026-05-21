package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Subject
import com.example.acadtrack.data.repository.SubjectRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectViewModel(private val repository: SubjectRepository) : ViewModel() {

    private val _subjectsState = MutableStateFlow<Resource<List<Subject>>>(Resource.Loading())
    val subjectsState: StateFlow<Resource<List<Subject>>> = _subjectsState.asStateFlow()

    private val _createSubjectState = MutableStateFlow<Resource<Subject>?>(null)
    val createSubjectState: StateFlow<Resource<Subject>?> = _createSubjectState.asStateFlow()

    fun fetchSubjects(departmentId: String? = null, page: Int = 1) {
        viewModelScope.launch {
            repository.getSubjects(departmentId, page).collect {
                _subjectsState.value = it
            }
        }
    }

    fun createSubject(name: String, code: String, departmentId: String, credits: Int) {
        viewModelScope.launch {
            repository.createSubject(name, code, departmentId, credits).collect {
                _createSubjectState.value = it
                if (it is Resource.Success) {
                    fetchSubjects(departmentId)
                }
            }
        }
    }

    fun resetCreateState() {
        _createSubjectState.value = null
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            repository.deleteSubject(id).collect { state ->
                if (state is Resource.Success) {
                    fetchSubjects()
                }
            }
        }
    }
}
