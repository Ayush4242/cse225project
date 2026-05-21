package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Holiday
import com.example.acadtrack.data.repository.HolidayRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HolidayViewModel(private val repository: HolidayRepository) : ViewModel() {

    private val _holidaysState = MutableStateFlow<Resource<List<Holiday>>>(Resource.Loading())
    val holidaysState: StateFlow<Resource<List<Holiday>>> = _holidaysState.asStateFlow()

    private val _createHolidayState = MutableStateFlow<Resource<Holiday>?>(null)
    val createHolidayState: StateFlow<Resource<Holiday>?> = _createHolidayState.asStateFlow()

    init {
        fetchHolidays()
    }

    fun fetchHolidays() {
        viewModelScope.launch {
            repository.getHolidays().collect {
                _holidaysState.value = it
            }
        }
    }

    fun createHoliday(title: String, startDate: String, endDate: String, description: String, type: String, targetRole: String) {
        viewModelScope.launch {
            repository.createHoliday(title, startDate, endDate, description, type, targetRole).collect {
                _createHolidayState.value = it
                if (it is Resource.Success) {
                    fetchHolidays()
                }
            }
        }
    }

    fun deleteHoliday(id: String) {
        viewModelScope.launch {
            repository.deleteHoliday(id).collect {
                if (it is Resource.Success) {
                    fetchHolidays()
                }
            }
        }
    }

    fun resetCreateState() {
        _createHolidayState.value = null
    }
}
