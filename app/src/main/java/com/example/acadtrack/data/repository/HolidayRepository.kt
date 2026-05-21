package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.CreateHolidayRequest
import com.example.acadtrack.data.model.Holiday
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HolidayRepository(private val apiService: ApiService) {
    fun getHolidays(): Flow<Resource<List<Holiday>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHolidays()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()?.data ?: emptyList()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch holidays"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun createHoliday(title: String, startDate: String, endDate: String, description: String, type: String, targetRole: String): Flow<Resource<Holiday>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateHolidayRequest(title, startDate, endDate, description, type, targetRole)
            val response = apiService.createHoliday(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Data missing"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to create holiday"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteHoliday(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteHoliday(id)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success("Holiday deleted successfully"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to delete holiday"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
