package com.example.shopapp.data.dao

import android.util.Log
import com.example.shopapp.data.model.OrderFirebase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseOrderDao : IOrderDaoFirebase {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    override suspend fun getAll(): Flow<List<OrderFirebase>> = flow {
        Log.d("FirebaseOrderDao", "Fetching all orders from Firestore")
        val snapshot = ordersCollection.get().await()
        val orders = snapshot.toObjects(OrderFirebase::class.java)
        Log.d("FirebaseOrderDao", "Fetched ${orders.size} orders from Firestore")
        emit(orders)
    }

    override suspend fun getById(id: String): OrderFirebase? {
        Log.d("FirebaseOrderDao", "Fetching order with ID: $id")
        val snapshot = ordersCollection.document(id).get().await()
        val order = snapshot.toObject(OrderFirebase::class.java)
        Log.d("FirebaseOrderDao", "Fetched order: $order")
        return order
    }

    override suspend fun add(item: OrderFirebase): Boolean {
        Log.d("FirebaseOrderDao", "Adding order: ${item.orderId}")
        return try {
            ordersCollection.document(item.orderId).set(item).await()
            Log.d("FirebaseOrderDao", "Order added successfully: ${item.orderId}")
            true
        } catch (e: Exception) {
            Log.e("FirebaseOrderDao", "Failed to add order: ${e.message}", e)
            false
        }
    }

    override suspend fun update(item: OrderFirebase): Boolean {
        Log.d("FirebaseOrderDao", "Updating order: ${item.orderId}")
        return try {
            ordersCollection.document(item.orderId).set(item).await()
            Log.d("FirebaseOrderDao", "Order updated successfully: ${item.orderId}")
            true
        } catch (e: Exception) {
            Log.e("FirebaseOrderDao", "Failed to update order: ${e.message}", e)
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        Log.d("FirebaseOrderDao", "Deleting order with ID: $id")
        return try {
            ordersCollection.document(id).delete().await()
            Log.d("FirebaseOrderDao", "Order deleted successfully: $id")
            true
        } catch (e: Exception) {
            Log.e("FirebaseOrderDao", "Failed to delete order: ${e.message}", e)
            false
        }
    }

    override suspend fun searchOrdersByUserId(userId: String): Flow<List<OrderFirebase>> = flow {
        Log.d("FirebaseOrderDao", "Searching orders for userId: $userId")
        val snapshot = ordersCollection.whereEqualTo("userId", userId).get().await()
        val orders = snapshot.toObjects(OrderFirebase::class.java)
        Log.d("FirebaseOrderDao", "Found ${orders.size} orders for userId: $userId")
        emit(orders)
    }

    override suspend fun searchOrdersByUsername(username: String): Flow<List<OrderFirebase>> = flow {
        Log.d("FirebaseOrderDao", "Searching orders for username: $username")
        val snapshot = ordersCollection.whereEqualTo("username", username).get().await()
        val orders = snapshot.toObjects(OrderFirebase::class.java)
        Log.d("FirebaseOrderDao", "Found ${orders.size} orders for username: $username")
        emit(orders)
    }
}