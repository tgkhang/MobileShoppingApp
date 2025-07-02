package com.example.shopapp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopapp.data.model.Order
import com.example.shopapp.data.repository.IRepository
import com.example.shopapp.data.repository.OrderRepository
import com.example.shopapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import com.example.shopapp.services.NotificationService

class OrderViewModel (
    private val repository: IRepository<Order>,
    private val notificationService: NotificationService
) : ViewModel() {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _selectedOrder = mutableStateOf<Order?>(null)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    private val _pageSize = MutableStateFlow(8) // Default page size
    val pageSize: StateFlow<Int> = _pageSize

    private val _deliveredOrders = MutableStateFlow<List<Order>>(emptyList())
    val deliveredOrders: StateFlow<List<Order>> = _deliveredOrders

    private val _pendingOrders = MutableStateFlow<List<Order>>(emptyList())
    val pendingOrders: StateFlow<List<Order>> = _pendingOrders

    var selectedOrder: Order?
        get() = _selectedOrder.value
        set(value) {
            _selectedOrder.value = value
        }

    private val _currentStatusFilter = MutableStateFlow<String?>(null)
    val currentStatusFilter: StateFlow<String?> = _currentStatusFilter

    init {
        loadInitialOrders()
    }

    fun loadInitialOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _currentStatusFilter.value = null

            if (repository is OrderRepository) {
                val orderDao = (repository as? OrderRepository)?.getOrderDao()
                _totalCount.value = orderDao?.getTotalOrdersCount() ?: 0

                orderDao?.getOrdersPage(_pageSize.value, 0)?.collect { orderList ->
                    _orders.value = orderList
                    _currentPage.value = 0
                    _hasMoreData.value = orderList.size >= _pageSize.value
                    _isLoading.value = false
                }
            } else {
                repository.fetchAll().collect { orderList ->
                    _orders.value = orderList
                    _isLoading.value = false
                    _hasMoreData.value = false
                }
            }
        }
    }

    fun loadNextPage() {
        if (_isLoading.value || !_hasMoreData.value) return

        viewModelScope.launch {
            _isLoading.value = true
            val nextPage = _currentPage.value + 1
            val offset = nextPage * _pageSize.value

            val orderDao = (repository as? OrderRepository)?.getOrderDao()
            if (orderDao != null) {
                if (_currentStatusFilter.value != null) {
                    // Use status-filtered pagination
                    orderDao.getOrdersPageByStatus(_currentStatusFilter.value!!, _pageSize.value, offset)
                        .collect { results ->
                            _orders.value = results
                            _currentPage.value = nextPage
                            _hasMoreData.value = results.size >= _pageSize.value
                            _isLoading.value = false

                            Log.d("OrderViewModel", "Loaded status-filtered page $nextPage with ${results.size} orders")
                        }
                } else {
                    // Use regular pagination
                    orderDao.getOrdersPage(_pageSize.value, offset).collect { results ->
                        _orders.value = results
                        _currentPage.value = nextPage
                        _hasMoreData.value = results.size >= _pageSize.value
                        _isLoading.value = false

                        Log.d("OrderViewModel", "Loaded page $nextPage with ${results.size} orders")
                    }
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun previousPage() {
        if (_currentPage.value <= 0) return

        viewModelScope.launch {
            _isLoading.value = true
            val prevPage = _currentPage.value - 1
            val offset = prevPage * _pageSize.value

            val orderDao = (repository as? OrderRepository)?.getOrderDao()
            if (orderDao != null) {
                if (_currentStatusFilter.value != null) {
                    // Use status-filtered pagination
                    orderDao.getOrdersPageByStatus(_currentStatusFilter.value!!, _pageSize.value, offset)
                        .collect { results ->
                            _orders.value = results
                            _currentPage.value = prevPage
                            _hasMoreData.value = true  // If we went back, there is definitely more data ahead
                            _isLoading.value = false
                        }
                } else {
                    // Use regular pagination
                    orderDao.getOrdersPage(_pageSize.value, offset).collect { results ->
                        _orders.value = results
                        _currentPage.value = prevPage
                        _hasMoreData.value = true
                        _isLoading.value = false
                    }
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun goToPage(page: Int) {
        if (page == _currentPage.value || page < 0) return

        viewModelScope.launch {
            _isLoading.value = true
            val offset = page * _pageSize.value

            val orderDao = (repository as? OrderRepository)?.getOrderDao()
            if (orderDao != null) {
                if (_currentStatusFilter.value != null) {
                    // Use status-filtered pagination
                    orderDao.getOrdersPageByStatus(_currentStatusFilter.value!!, _pageSize.value, offset)
                        .collect { results ->
                            _orders.value = results
                            _currentPage.value = page
                            _hasMoreData.value = results.size >= _pageSize.value
                            _isLoading.value = false
                        }
                } else {
                    // Use regular pagination
                    orderDao.getOrdersPage(_pageSize.value, offset).collect { results ->
                        _orders.value = results
                        _currentPage.value = page
                        _hasMoreData.value = results.size >= _pageSize.value
                        _isLoading.value = false
                    }
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            repository.fetchAll().collect { orderList ->
                Log.d("OrderViewModel", "ViewModel received ${orderList.size} orders")
                _orders.value = orderList
            }
        }
    }

    fun addOrder(order: Order) {
        viewModelScope.launch {
            repository.create(order)
        }
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch {
            if(repository.modify(order)){
                loadInitialOrders()
            }
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.remove(orderId)
        }
    }

    fun getOrderById(orderId: String): Order? {
        var order: Order? = null
        viewModelScope.launch {
            order = repository.fetchById(orderId)
        }
        return order
    }

    fun searchOrdersByUserId(userId: String) {
        viewModelScope.launch {
            if (repository is OrderRepository) {
                repository.searchOrdersByUserId(userId).collect { results ->
                    _orders.value = results
                }
            } else {
                Log.d("OrderViewModel", "Repository does not support search")
            }
        }
    }

    fun searchOrdersByUsername(username: String) {
        viewModelScope.launch {
            if (repository is OrderRepository) {
                repository.searchOrdersByUsername(username).collect { results ->
                    _orders.value = results
                }
            } else {
                Log.d("OrderViewModel", "Repository does not support search")
            }
        }
    }

    fun selectOrder(order: Order) {
        selectedOrder = order
    }

    fun updateOrderStatus(order: Order, newStatus: String, productRepository: ProductRepository? = null) {
        viewModelScope.launch {
            try {
                val previousStatus = order.status
                val updatedOrder = order.copy(status = newStatus)
                repository.modify(updatedOrder)

                // Update product stock and sales when status changes from pending to shipping
                if (previousStatus == "pending" && newStatus == "shipping" && productRepository != null) {
                    for (cartItem in order.orderDetail) {
                        val product = productRepository.fetchById(cartItem.productId)
                        product?.let {
                            // Decrease stock and increase sales
                            val updatedProduct = it.copy(
                                stock = it.stock - cartItem.quantity,
                                sales = it.sales + cartItem.quantity
                            )
                            productRepository.modify(updatedProduct)
                            Log.d("OrderViewModel", "Updated product ${it.title}: stock=${updatedProduct.stock}, sales=${updatedProduct.sales}")
                        }
                    }
                }

                // Send notification when status changes from "pending" to "shipping"
                if (previousStatus == "pending" && newStatus == "shipping") {
                    notificationService.sendOrderStatusNotification(updatedOrder, order.userId)
                }

                // Refresh orders
                loadInitialOrders()
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error updating order status", e)
            }
        }
    }

    fun filterOrdersByStatus(status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentStatusFilter.value = status
            _orders.value = emptyList()
            _currentPage.value = -1
            _hasMoreData.value = true

            if (repository is OrderRepository) {
                val orderDao = repository.getOrderDao()
                _totalCount.value = orderDao.getTotalOrdersCountByStatus(status)

                orderDao.getOrdersPageByStatus(status, _pageSize.value, 0).collect { results ->
                    _orders.value = results
                    _currentPage.value = 0
                    _hasMoreData.value = results.size >= _pageSize.value
                    _isLoading.value = false

                    Log.d("OrderViewModel", "Filtered ${results.size} orders with status '$status'")
                }
            } else {
                _isLoading.value = false
                Log.d("OrderViewModel", "Repository does not support filtering")
            }
        }
    }

    fun resetFiltersAndSearch() {
        _currentStatusFilter.value = null
        _currentPage.value = 0
        _hasMoreData.value = true

        loadInitialOrders()

        Log.d("OrderViewModel", "Order filters reset")
    }

    fun loadAllDeliveredOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            if (repository is OrderRepository) {
                repository.getAllOrderByStatus("delivered").collect { results ->
                    _deliveredOrders.value = results
                    _isLoading.value = false
                    Log.d("OrderViewModel", "Loaded ${results.size} delivered orders")
                }
            } else {
                _isLoading.value = false
                Log.d("OrderViewModel", "Repository does not support filtering")
            }
        }
    }

    fun loadAllPendingOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            if (repository is OrderRepository) {
                repository.getAllOrderByStatus("pending").collect { results ->
                    _pendingOrders.value = results
                    _isLoading.value = false
                    Log.d("OrderViewModel", "Loaded ${results.size} pending orders")
                }
            } else {
                _isLoading.value = false
                Log.d("OrderViewModel", "Repository does not support filtering")
            }
        }
    }
}