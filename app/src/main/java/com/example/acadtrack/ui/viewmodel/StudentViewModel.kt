package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Student
import com.example.acadtrack.data.model.StudentResponse
import com.example.acadtrack.data.repository.StudentRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class StudentViewModel(private val repository: StudentRepository) : ViewModel() {

    private val _studentsState = MutableStateFlow<Resource<List<Student>>>(Resource.Loading())
    val studentsState: StateFlow<Resource<List<Student>>> = _studentsState.asStateFlow()

    private val _createStudentState = MutableStateFlow<Resource<Student>?>(null)
    val createStudentState: StateFlow<Resource<Student>?> = _createStudentState.asStateFlow()

    private val _bulkUploadState = MutableStateFlow<Resource<StudentResponse>?>(null)
    val bulkUploadState: StateFlow<Resource<StudentResponse>?> = _bulkUploadState.asStateFlow()

    fun fetchStudents(sectionId: String? = null, page: Int = 1, search: String? = null) {
        viewModelScope.launch {
            repository.getStudents(sectionId, page, search).collect {
                _studentsState.value = it
            }
        }
    }

    fun createStudent(
        name: String,
        email: String,
        password: String,
        rollNumber: String,
        sectionId: String,
        parentContact: String,
        admissionYear: Int
    ) {
        viewModelScope.launch {
            repository.createStudent(name, email, password, rollNumber, sectionId, parentContact, admissionYear).collect {
                _createStudentState.value = it
                if (it is Resource.Success) {
                    fetchStudents(sectionId)
                }
            }
        }
    }

    fun bulkUpload(file: File) {
        viewModelScope.launch {
            repository.bulkUpload(file).collect {
                _bulkUploadState.value = it
                if (it is Resource.Success) {
                    fetchStudents()
                }
            }
        }
    }

    fun resetCreateState() {
        _createStudentState.value = null
    }

    fun resetBulkUploadState() {
        _bulkUploadState.value = null
    }

    fun deleteStudent(id: String) {
        viewModelScope.launch {
            repository.deleteStudent(id).collect { state ->
                if (state is Resource.Success) {
                    fetchStudents()
                }
            }
        }
    }
}
