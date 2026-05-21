package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Timetable
import com.example.acadtrack.data.repository.TimetableRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimetableViewModel(private val repository: TimetableRepository) : ViewModel() {

    private val _timetableState = MutableStateFlow<Resource<Timetable>?>(null)
    val timetableState: StateFlow<Resource<Timetable>?> = _timetableState.asStateFlow()

    fun fetchTimetable(sectionId: String) {
        viewModelScope.launch {
            repository.getTimetable(sectionId).collect {
                _timetableState.value = it
            }
        }
    }

    fun addSlot(sectionId: String, day: String, startTime: String, endTime: String, subjectId: String, teacherId: String) {
        viewModelScope.launch {
            repository.addSlot(sectionId, day, startTime, endTime, subjectId, teacherId).collect {
                _timetableState.value = it
            }
        }
    }

    fun deleteSlot(sectionId: String, slotId: String) {
        viewModelScope.launch {
            repository.deleteSlot(sectionId, slotId).collect {
                _timetableState.value = it
            }
        }
    }
}
