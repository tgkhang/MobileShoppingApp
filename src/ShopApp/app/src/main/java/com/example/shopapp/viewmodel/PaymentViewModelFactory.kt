package com.example.shopapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shopapp.data.repository.PaymentRepository

class PaymentViewModelFactory(
    private val application: Application,
    private val paymentRepository: PaymentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(application, paymentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}