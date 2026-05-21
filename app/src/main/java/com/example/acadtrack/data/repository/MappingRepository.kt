package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.Mapping
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MappingRepository(private val apiService: ApiService) {
    fun getMappings(sectionId: String?, teacherId: String?): Flow<Resource<List<Mapping>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getMappings(sectionId, teacherId)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()?.mappings ?: emptyList()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch mappings"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun createMapping(teacherId: String, subjectId: String, sectionId: String): Flow<Resource<Mapping>> = flow {
        emit(Resource.Loading())
        try {
            val request = com.example.acadtrack.data.model.CreateMappingRequest(teacherId, subjectId, sectionId)
            val response = apiService.createMapping(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { emit(Resource.Success(it)) } ?: emit(Resource.Error("Data missing"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to create mapping"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteMapping(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteMapping(id)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success("Mapping deleted successfully"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to delete mapping"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
