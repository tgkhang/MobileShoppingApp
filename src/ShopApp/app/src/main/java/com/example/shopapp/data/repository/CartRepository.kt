package com.example.shopapp.data.repository

import com.example.shopapp.data.dao.CartDao
import com.example.shopapp.data.model.CartItem

import kotlinx.coroutines.flow.Flow


class CartRepository(private val cartDao: CartDao)
{
    val allCartItems: Flow<List<CartItem>> = cartDao.getAll()
    val totalPrice: Flow<Double?> = cartDao.getTotalPrice()
    val itemCount: Flow<Int> = cartDao.getItemCount()

    suspend fun addToCart(item: CartItem) {
        val existingItem = cartDao.getItem(item.productId)
        if (existingItem != null) {
            // Increase quantity if item already in cart
            cartDao.update(existingItem.copy(quantity = existingItem.quantity + item.quantity))
        } else {
            cartDao.insert(item)
        }
    }

    suspend fun updateCartItem(item: CartItem) {
        cartDao.update(item)
    }

    suspend fun removeFromCart(item: CartItem) {
        cartDao.delete(item)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    suspend fun getCartItem(productId: String): CartItem? {
        return cartDao.getItem(productId)
    }
}