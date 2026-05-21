package com.example.acadtrack.utils

import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Response

object NetworkUtils {
    fun <T> getErrorMessage(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val gson = Gson()
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: "An unknown error occurred"
        } catch (e: Exception) {
            "An error occurred: ${response.code()}"
        }
    }
}

data class ErrorResponse(
    val success: Boolean,
    val message: String?
)
