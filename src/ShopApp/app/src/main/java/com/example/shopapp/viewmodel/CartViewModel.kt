package com.example.shopapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shopapp.data.model.CartItem
import com.example.shopapp.data.repository.CartRepository
import com.example.shopapp.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    val cartItems = repository.allCartItems
    val cartTotal = repository.totalPrice
    val itemCount = repository.itemCount

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val cartItem = CartItem(
                productId = product.productId,
                productTitle = product.title,
                productImage = product.image,
                price = product.price.toDouble(),
                quantity = quantity
            )
            repository.addToCart(cartItem)
        }
    }

    fun updateCartItem(item: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(item)
        } else {
            viewModelScope.launch {
                repository.updateCartItem(item.copy(quantity = newQuantity))
            }
        }
    }

    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            repository.removeFromCart(item)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }
}

class CartViewModelFactory(private val repository: CartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}