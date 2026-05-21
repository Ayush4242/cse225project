package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.CreateSubjectRequest
import com.example.acadtrack.data.model.Subject
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.NetworkUtils
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SubjectRepository(private val apiService: ApiService) {
    fun getSubjects(departmentId: String?, page: Int): Flow<Resource<List<Subject>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getSubjects(departmentId, page)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.subjects ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to fetch subjects"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun createSubject(name: String, code: String, departmentId: String, credits: Int): Flow<Resource<Subject>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateSubjectRequest(name, code, departmentId, credits)
            val response = apiService.createSubject(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to create subject"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteSubject(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteSubject(id)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success("Subject deleted successfully"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to delete subject"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
