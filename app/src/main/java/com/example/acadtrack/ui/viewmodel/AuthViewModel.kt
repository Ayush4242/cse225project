package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.AuthResponse
import com.example.acadtrack.data.repository.AuthRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password).collect {
                _loginState.value = it
            }
        }
    }

    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun getUserRole(): String? = repository.getUserRole()

    fun getUserName(): String? = repository.getUserName()

    fun logout() {
        repository.logout()
        _loginState.value = null
    }
}
