package com.example.shopapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shopapp.data.model.Order
import com.example.shopapp.data.repository.IRepository
import com.example.shopapp.services.NotificationService

class OrderViewModelFactory(
    private val repository: IRepository<Order>,
    private val notificationService: NotificationService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(repository, notificationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}