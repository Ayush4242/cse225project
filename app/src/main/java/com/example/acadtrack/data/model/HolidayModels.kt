package com.example.acadtrack.data.model

import com.google.gson.annotations.SerializedName

data class Holiday(
    @SerializedName("_id") val id: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val description: String?,
    val type: String,
    val targetRole: String
)

data class HolidayResponse(
    val success: Boolean,
    val data: Holiday?,
    val message: String?
)

data class Notification(
    @SerializedName("_id") val id: String,
    val title: String,
    val message: String,
    val targetRole: String,
    val createdBy: String?,
    val createdAt: String
)

data class NotificationResponse(
    val success: Boolean,
    val data: Notification?,
    val message: String?
)
