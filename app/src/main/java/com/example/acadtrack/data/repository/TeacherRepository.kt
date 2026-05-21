package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.*
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.NetworkUtils
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TeacherRepository(private val apiService: ApiService) {

    // Admin Operations
    fun getTeachers(departmentId: String?, page: Int): Flow<Resource<List<Teacher>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTeachers(departmentId, page)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.teachers ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to fetch teachers"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun createTeacher(
        name: String,
        email: String,
        password: String,
        employeeId: String,
        departmentId: String,
        designation: String,
        phoneNumber: String
    ): Flow<Resource<Teacher>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateTeacherRequest(name, email, password, employeeId, departmentId, designation, phoneNumber)
            val response = apiService.createTeacher(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to create teacher"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteTeacher(id: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteTeacher(id)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success("Teacher deleted successfully"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to delete teacher"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    // Teacher Portal Operations
    fun getMyClasses(): Flow<Resource<List<Mapping>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getMyClasses()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.data ?: emptyList()))
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

    fun getTodayClasses(): Flow<Resource<List<TodayClass>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTodayClasses()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.data ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to fetch today's classes"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun startSession(
        subjectId: String,
        sectionId: String,
        timetableSlotId: String,
        location: Location? = null
    ): Flow<Resource<Session>> = flow {
        emit(Resource.Loading())
        try {
            val request = StartSessionRequest(subjectId, sectionId, timetableSlotId, location)
            val response = apiService.startSession(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to start session"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun refreshQr(sessionId: String): Flow<Resource<QrData>> = flow {
        try {
            val response = apiService.refreshQr(sessionId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to refresh QR"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun endSession(sessionId: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.endSession(sessionId)
            if (response.isSuccessful) {
                emit(Resource.Success("Session ended successfully"))
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getActiveSession(): Flow<Resource<Session?>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getActiveSession()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()?.data))
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getSessionAttendance(sessionId: String): Flow<Resource<List<Attendance>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getSessionAttendance(sessionId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    emit(Resource.Success(body.data ?: emptyList()))
                } else {
                    emit(Resource.Error("Failed to fetch attendance"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getSectionAnalytics(sectionId: String): Flow<Resource<List<SectionAnalytics>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getSectionAnalytics(sectionId)
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

    fun updateProfile(name: String?, password: String?): Flow<Resource<Teacher>> = flow {
        emit(Resource.Loading())
        try {
            val updateData = mutableMapOf<String, String>()
            if (name != null) updateData["name"] = name
            if (password != null) updateData["password"] = password
            
            val response = apiService.updateTeacherProfile(updateData)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to update profile"))
                }
            } else {
                emit(Resource.Error(NetworkUtils.getErrorMessage(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
