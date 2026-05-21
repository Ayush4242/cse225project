package com.example.acadtrack.data.model

import com.google.gson.annotations.SerializedName

data class Leave(
    @SerializedName("_id") val id: String,
    val user: User?,
    val role: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String,
    val createdAt: String?
)

data class LeaveResponse(
    val success: Boolean,
    val data: List<Leave>?,
    val message: String? = null
)

data class SingleLeaveResponse(
    val success: Boolean,
    val data: Leave?,
    val message: String? = null
)

data class CreateLeaveRequest(
    val startDate: String,
    val endDate: String,
    val reason: String
)

data class UpdateLeaveStatusRequest(
    val status: String
)
