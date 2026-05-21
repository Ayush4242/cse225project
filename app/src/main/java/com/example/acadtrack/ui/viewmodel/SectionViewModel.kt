package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Section
import com.example.acadtrack.data.repository.SectionRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SectionViewModel(private val repository: SectionRepository) : ViewModel() {

    private val _sectionsState = MutableStateFlow<Resource<List<Section>>>(Resource.Loading())
    val sectionsState: StateFlow<Resource<List<Section>>> = _sectionsState.asStateFlow()

    private val _createSectionState = MutableStateFlow<Resource<Section>?>(null)
    val createSectionState: StateFlow<Resource<Section>?> = _createSectionState.asStateFlow()

    private var currentClassId: String? = null

    fun fetchSections(classId: String? = currentClassId, page: Int = 1) {
        currentClassId = classId
        viewModelScope.launch {
            repository.getSections(classId, page).collect {
                _sectionsState.value = it
            }
        }
    }

    fun createSection(name: String, classId: String, roomNumber: String) {
        viewModelScope.launch {
            repository.createSection(name, classId, roomNumber).collect { state ->
                _createSectionState.value = state
                if (state is Resource.Success) {
                    fetchSections(currentClassId)
                }
            }
        }
    }

    fun resetCreateState() {
        _createSectionState.value = null
    }
}
