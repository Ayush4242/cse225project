package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.ClassModel
import com.example.acadtrack.data.model.CreateClassRequest
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.NetworkUtils
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ClassRepository(private val apiService: ApiService) {
    fun getClasses(departmentId: String?, page: Int): Flow<Resource<List<ClassModel>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getClasses(departmentId, page)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.classes ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to fetch classes"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun createClass(name: String, departmentId: String, batchYear: Int): Flow<Resource<ClassModel>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateClassRequest(name, departmentId, batchYear)
            val response = apiService.createClass(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to create class"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteClass(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteClass(id)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success("Class deleted successfully"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to delete class"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
