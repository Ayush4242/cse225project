package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.CreateLeaveRequest
import com.example.acadtrack.data.model.Leave
import com.example.acadtrack.data.model.UpdateLeaveStatusRequest
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LeaveRepository(private val apiService: ApiService) {

    fun createLeave(startDate: String, endDate: String, reason: String): Flow<Resource<Leave>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createLeave(CreateLeaveRequest(startDate, endDate, reason))
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to create leave"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getMyLeaves(): Flow<Resource<List<Leave>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getMyLeaves()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()!!.data ?: emptyList()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch your leaves"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getAllPendingLeaves(): Flow<Resource<List<Leave>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getAllPendingLeaves()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()!!.data ?: emptyList()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch pending leaves"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun updateLeaveStatus(id: String, status: String): Flow<Resource<Leave>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateLeaveStatus(id, UpdateLeaveStatusRequest(status))
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to update leave status"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
