package com.example.acadtrack.data.repository

import android.content.Context
import com.example.acadtrack.data.model.AuthResponse
import com.example.acadtrack.data.model.LoginRequest
import com.example.acadtrack.data.network.ApiService
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(private val apiService: ApiService, private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("acadtrack_prefs", Context.MODE_PRIVATE)

    fun login(email: String, password: String): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()!!
                // Save token and role
                authResponse.token?.let {
                    sharedPreferences.edit().putString("jwt_token", it).apply()
                }
                authResponse.data?.user?.role?.let {
                    sharedPreferences.edit().putString("user_role", it).apply()
                }
                authResponse.data?.user?.name?.let {
                    sharedPreferences.edit().putString("user_name", it).apply()
                }
                emit(Resource.Success(authResponse))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun logout() {
        sharedPreferences.edit().remove("jwt_token").remove("user_role").remove("user_name").apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getString("jwt_token", null) != null
    }

    fun getUserRole(): String? {
        return sharedPreferences.getString("user_role", null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("user_name", null)
    }
}
