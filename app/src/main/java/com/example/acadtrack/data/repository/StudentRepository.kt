package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.*
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.NetworkUtils
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class StudentRepository(private val apiService: ApiService) {
    fun getStudents(sectionId: String?, page: Int, search: String?): Flow<Resource<List<Student>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getStudents(sectionId, page, search)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.students ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to fetch students"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun createStudent(
        name: String,
        email: String,
        password: String,
        rollNumber: String,
        sectionId: String,
        parentContact: String,
        admissionYear: Int
    ): Flow<Resource<Student>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateStudentRequest(
                name = name,
                email = email,
                password = password,
                rollNumber = rollNumber,
                section = sectionId,
                parentContact = parentContact,
                admissionYear = admissionYear
            )
            val response = apiService.createStudent(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to create student"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun bulkUpload(file: File): Flow<Resource<StudentResponse>> = flow {
        emit(Resource.Loading())
        try {
            val requestFile = file.asRequestBody("text/csv".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = apiService.bulkUploadStudents(body)
            if (response.isSuccessful) {
                val bodyData = response.body()
                if (bodyData?.success == true) {
                    emit(Resource.Success(bodyData))
                } else {
                    emit(Resource.Error(bodyData?.message ?: "Bulk upload failed"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteStudent(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteStudent(id)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success("Student deleted successfully"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to delete student"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun markAttendance(request: MarkAttendanceRequest): Flow<Resource<Attendance>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.markAttendance(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to mark attendance"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getStudentAnalytics(): Flow<Resource<List<Map<String, Any>>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getStudentAnalytics()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.data ?: emptyList()))
                } else {
                    emit(Resource.Error("Failed to fetch analytics"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getActiveSessions(): Flow<Resource<List<Session>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getStudentActiveSessions()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.data ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to fetch active sessions"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
