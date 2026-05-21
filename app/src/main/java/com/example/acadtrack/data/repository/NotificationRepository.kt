package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.CreateNotificationRequest
import com.example.acadtrack.data.model.Notification
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NotificationRepository(private val apiService: ApiService) {
    fun getNotifications(role: String?): Flow<Resource<List<Notification>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getNotifications(role)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()?.data ?: emptyList()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch notifications"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun createNotification(title: String, message: String, targetRole: String): Flow<Resource<Notification>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateNotificationRequest(title, message, targetRole)
            val response = apiService.createNotification(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Data missing"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to create notification"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteNotification(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteNotification(id)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success("Notification deleted successfully"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to delete notification"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
