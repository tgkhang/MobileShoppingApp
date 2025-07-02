package com.example.shopapp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopapp.data.model.OrderFirebase
import com.example.shopapp.data.repository.IRepository
import com.example.shopapp.data.repository.OrderRepositoryFirebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModelFirebase(private val repository: IRepository<OrderFirebase>) : ViewModel() {
    private val _orders = MutableStateFlow<List<OrderFirebase>>(emptyList())
    val orders: StateFlow<List<OrderFirebase>> = _orders

    private val _selectedOrder = mutableStateOf<OrderFirebase?>(null)
    var selectedOrder: OrderFirebase?
        get() = _selectedOrder.value
        set(value) {
            _selectedOrder.value = value
        }

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                repository.fetchAll().collect { orderList ->
                    Log.d("OrderViewModelFirebase", "ViewModel received ${orderList.size} orders")
                    _orders.value = orderList
                }
            } catch (e: Exception) {
                Log.e("OrderViewModelFirebase", "Failed to load orders: ${e.message}", e)
            }
        }
    }

    fun addOrder(order: OrderFirebase, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val success = repository.create(order)
            if (success) {
                Log.d("OrderViewModelFirebase", "Order added successfully: ${order.orderId}")
                onSuccess()
            } else {
                Log.e("OrderViewModelFirebase", "Failed to add order: ${order.orderId}")
                onFailure("Failed to place order. Please try again.")
            }
        }
    }

    fun updateOrder(order: OrderFirebase, onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.modify(order)
            if (success) {
                Log.d("OrderViewModelFirebase", "Order updated successfully: ${order.orderId}")
                _orders.value = _orders.value.map { existingOrder ->
                    if (existingOrder.orderId == order.orderId) order else existingOrder
                }
                onSuccess()
            } else {
                Log.e("OrderViewModelFirebase", "Failed to update order: ${order.orderId}")
                onFailure("Failed to update order. Please try again.")
            }
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.remove(orderId)
        }
    }

    suspend fun getOrderById(orderId: String): OrderFirebase? {
        return try {
            repository.fetchById(orderId)
        } catch (e: Exception) {
            Log.e("OrderViewModelFirebase", "Failed to get order $orderId: ${e.message}", e)
            null
        }
    }

    fun searchOrdersByUserId(userId: String) {
        viewModelScope.launch {
            if (repository is OrderRepositoryFirebase) {
                try {
                    repository.searchOrdersByUserId(userId).collect { results ->
                        _orders.value = results
                    }
                } catch (e: Exception) {
                    Log.e("OrderViewModelFirebase", "Failed to search orders by userId: ${e.message}", e)
                }
            } else {
                Log.d("OrderViewModelFirebase", "Repository does not support search")
            }
        }
    }

    fun searchOrdersByUsername(username: String) {
        viewModelScope.launch {
            if (repository is OrderRepositoryFirebase) {
                try {
                    repository.searchOrdersByUsername(username).collect { results ->
                        _orders.value = results
                    }
                } catch (e: Exception) {
                    Log.e("OrderViewModelFirebase", "Failed to search orders by username: ${e.message}", e)
                }
            } else {
                Log.d("OrderViewModelFirebase", "Repository does not support search")
            }
        }
    }

    fun selectOrder(order: OrderFirebase) {
        selectedOrder = order
    }
}