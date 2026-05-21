package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.CreateSectionRequest
import com.example.acadtrack.data.model.Section
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.NetworkUtils
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SectionRepository(private val apiService: ApiService) {
    fun getSections(classId: String?, page: Int): Flow<Resource<List<Section>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getSections(classId, page)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.sections ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to fetch sections"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun createSection(name: String, classId: String, roomNumber: String): Flow<Resource<Section>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateSectionRequest(name, classId, roomNumber)
            val response = apiService.createSection(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to create section"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
