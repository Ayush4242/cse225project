package com.example.acadtrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.CreateEnrollmentRequest
import com.example.acadtrack.data.model.Enrollment
import com.example.acadtrack.data.network.RetrofitClient
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EnrollmentViewModel(context: Context) : ViewModel() {
    private val api = RetrofitClient.getApiService(context)

    private val _enrollmentsState = MutableStateFlow<Resource<List<Enrollment>>>(Resource.Loading())
    val enrollmentsState: StateFlow<Resource<List<Enrollment>>> = _enrollmentsState

    private val _createEnrollmentState = MutableStateFlow<Resource<Enrollment>?>(null)
    val createEnrollmentState: StateFlow<Resource<Enrollment>?> = _createEnrollmentState

    fun fetchEnrollments() {
        viewModelScope.launch {
            _enrollmentsState.value = Resource.Loading()
            try {
                val response = api.getEnrollments()
                if (response.isSuccessful && response.body() != null) {
                    _enrollmentsState.value = Resource.Success(response.body()!!.data)
                } else {
                    _enrollmentsState.value = Resource.Error(response.message() ?: "Failed to fetch enrollments")
                }
            } catch (e: Exception) {
                _enrollmentsState.value = Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun createEnrollment(studentId: String, mappingId: String) {
        viewModelScope.launch {
            _createEnrollmentState.value = Resource.Loading()
            try {
                val request = CreateEnrollmentRequest(studentId, mappingId)
                val response = api.createEnrollment(request)
                if (response.isSuccessful && response.body() != null) {
                    _createEnrollmentState.value = Resource.Success(response.body()!!.data)
                    fetchEnrollments() // Refresh list
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val json = org.json.JSONObject(errorBody ?: "")
                        json.getString("message")
                    } catch (e: Exception) {
                        "Failed to create enrollment"
                    }
                    _createEnrollmentState.value = Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                _createEnrollmentState.value = Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun deleteEnrollment(id: String) {
        viewModelScope.launch {
            try {
                val response = api.deleteEnrollment(id)
                if (response.isSuccessful) {
                    fetchEnrollments() // Refresh list
                }
            } catch (e: Exception) {
                // handle error silently or log
            }
        }
    }

    fun resetCreateState() {
        _createEnrollmentState.value = null
    }
}
