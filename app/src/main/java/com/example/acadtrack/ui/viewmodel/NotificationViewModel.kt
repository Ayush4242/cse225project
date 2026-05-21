package com.example.acadtrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acadtrack.data.model.Notification
import com.example.acadtrack.data.repository.NotificationRepository
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    private val _notificationsState = MutableStateFlow<Resource<List<Notification>>>(Resource.Loading())
    val notificationsState: StateFlow<Resource<List<Notification>>> = _notificationsState.asStateFlow()

    private val _createNotificationState = MutableStateFlow<Resource<Notification>?>(null)
    val createNotificationState: StateFlow<Resource<Notification>?> = _createNotificationState.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications(role: String? = null) {
        viewModelScope.launch {
            repository.getNotifications(role).collect {
                _notificationsState.value = it
            }
        }
    }

    fun createNotification(title: String, message: String, targetRole: String) {
        viewModelScope.launch {
            repository.createNotification(title, message, targetRole).collect {
                _createNotificationState.value = it
                if (it is Resource.Success) {
                    fetchNotifications()
                }
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id).collect {
                if (it is Resource.Success) {
                    fetchNotifications()
                }
            }
        }
    }

    fun resetCreateState() {
        _createNotificationState.value = null
    }
}
