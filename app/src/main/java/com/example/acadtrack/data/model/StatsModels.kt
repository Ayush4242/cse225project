package com.example.acadtrack.data.model

data class DashboardStats(
    val departments: Int,
    val teachers: Int,
    val students: Int,
    val sections: Int
)

data class StatsResponse(
    val success: Boolean,
    val data: DashboardStats?,
    val message: String?
)
