package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.CreateDepartmentRequest
import com.example.acadtrack.data.model.Department
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class DepartmentRepository(private val apiService: ApiService) {

    fun getDepartments(page: Int, limit: Int): Flow<Resource<List<Department>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getDepartments(page, limit)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()?.departments ?: emptyList()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Unknown error occurred"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "Unexpected error"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    fun createDepartment(name: String, description: String): Flow<Resource<Department>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateDepartmentRequest(name, description)
            val response = apiService.createDepartment(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Data missing in response"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to create department"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteDepartment(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteDepartment(id)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success("Department deleted successfully"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to delete department"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
