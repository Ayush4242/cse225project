package com.example.acadtrack.data.repository

import com.example.acadtrack.data.model.Timetable
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TimetableRepository(private val apiService: ApiService) {
    fun getTimetable(sectionId: String): Flow<Resource<Timetable>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTimetable(sectionId)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()?.data!!))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch timetable"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun addSlot(sectionId: String, day: String, startTime: String, endTime: String, subjectId: String, teacherId: String): Flow<Resource<Timetable>> = flow {
        emit(Resource.Loading())
        try {
            val request = com.example.acadtrack.data.model.CreateTimetableSlotRequest(
                day = day,
                startTime = startTime,
                endTime = endTime,
                subject = subjectId,
                teacher = teacherId
            )
            val response = apiService.addTimetableSlot(sectionId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()?.data!!))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to add slot"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteSlot(sectionId: String, slotId: String): Flow<Resource<Timetable>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteTimetableSlot(sectionId, slotId)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()?.data!!))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to delete slot"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
