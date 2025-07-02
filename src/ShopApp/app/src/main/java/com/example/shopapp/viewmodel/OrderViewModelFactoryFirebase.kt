package com.example.shopapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shopapp.data.model.OrderFirebase
import com.example.shopapp.data.repository.IRepository

class OrderViewModelFactoryFirebase (private val repository: IRepository<OrderFirebase>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModelFirebase::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderViewModelFirebase(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}