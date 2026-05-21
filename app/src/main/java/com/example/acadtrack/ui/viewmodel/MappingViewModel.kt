package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Mapping
import com.example.acadtrack.data.repository.MappingRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MappingViewModel(private val repository: MappingRepository) : ViewModel() {

    private val _mappingsState = MutableStateFlow<Resource<List<Mapping>>>(Resource.Loading())
    val mappingsState: StateFlow<Resource<List<Mapping>>> = _mappingsState.asStateFlow()

    private val _createMappingState = MutableStateFlow<Resource<Mapping>?>(null)
    val createMappingState: StateFlow<Resource<Mapping>?> = _createMappingState.asStateFlow()

    fun fetchMappings(sectionId: String? = null, teacherId: String? = null) {
        viewModelScope.launch {
            repository.getMappings(sectionId, teacherId).collect {
                _mappingsState.value = it
            }
        }
    }

    fun createMapping(teacherId: String, subjectId: String, sectionId: String) {
        viewModelScope.launch {
            repository.createMapping(teacherId, subjectId, sectionId).collect {
                _createMappingState.value = it
                if (it is Resource.Success) {
                    fetchMappings(sectionId)
                }
            }
        }
    }

    fun deleteMapping(id: String, sectionId: String? = null) {
        viewModelScope.launch {
            repository.deleteMapping(id).collect {
                if (it is Resource.Success) {
                    fetchMappings(sectionId)
                }
            }
        }
    }

    fun resetCreateState() {
        _createMappingState.value = null
    }
}
