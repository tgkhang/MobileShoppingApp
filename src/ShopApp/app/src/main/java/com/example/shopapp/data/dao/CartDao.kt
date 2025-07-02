package com.example.shopapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.shopapp.data.model.CartItem

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao{
    @Query("SELECT * FROM cart_items ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getItem(productId: String): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItem)

    @Update
    suspend fun update(item: CartItem)

    @Delete
    suspend fun delete(item: CartItem)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT SUM(price * quantity) FROM cart_items")
    fun getTotalPrice(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM cart_items")
    fun getItemCount(): Flow<Int>
}