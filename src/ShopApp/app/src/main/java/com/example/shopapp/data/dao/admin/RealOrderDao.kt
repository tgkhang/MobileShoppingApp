package com.example.shopapp.data.dao.admin

import android.util.Log
import com.example.shopapp.data.model.CartItem
import com.example.shopapp.data.model.Order
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class RealOrderDao : IOrderDao {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")
    private val TAG = "RealOrderDao"

    // Helper function to convert Firestore document to Order
    private fun documentToOrder(doc: com.google.firebase.firestore.DocumentSnapshot): Order? {
        try {
            val order = doc.toObject(Order::class.java) ?: return null

            val itemList = doc.get("orderDetail") as? List<Map<String, Any>> ?: emptyList()
            val parsedItemList = itemList.map { item ->
                CartItem(
                    productId = item["productId"] as? String ?: "",
                    productTitle = item["productTitle"] as? String ?: "",
                    productImage = item["productImage"] as? String ?: "",
                    price = (item["price"] as? Number)?.toDouble() ?: 0.0,
                    quantity = (item["quantity"] as? Number)?.toInt() ?: 0,
                    timestamp = (item["timestamp"] as? Timestamp)?.seconds?.toLong() ?: System.currentTimeMillis()
                )
            }
            return order.copy(orderDetail = parsedItemList)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing order document: ${e.message}")
            return null
        }
    }

    override suspend fun getAll(): Flow<List<Order>> = flow {
        try {
            val snapshot = ordersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val orders = snapshot.documents.mapNotNull { documentToOrder(it) }
            Log.d(TAG, "Total orders fetched: ${orders.size}")
            emit(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getById(id: String): Order? {
        return try {
            val doc = ordersCollection.document(id).get().await()
            documentToOrder(doc)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching order by ID: ${e.message}")
            null
        }
    }

    override suspend fun add(item: Order): Boolean {
        return try {
            ordersCollection.document(item.orderId).set(item).await()
            Log.d(TAG, "Order added successfully: ${item.orderId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding order", e)
            false
        }
    }

    override suspend fun update(item: Order): Boolean {
        return try {
            ordersCollection.document(item.orderId).set(item).await()
            Log.d(TAG, "Order updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order", e)
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            ordersCollection.document(id).delete().await()
            Log.d(TAG, "Order deleted successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting order", e)
            false
        }
    }

    override suspend fun searchOrdersByUserId(userId: String): Flow<List<Order>> = flow {
        try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            val orders = snapshot.documents.mapNotNull { documentToOrder(it) }
            Log.d(TAG, "Found ${orders.size} orders for user ID: $userId")
            emit(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching orders by user ID: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun searchOrdersByUsername(username: String): Flow<List<Order>> = flow {
        try {
            val lowercaseUsername = username.lowercase()
            val matchingOrders = mutableListOf<Order>()
            val orderIds = mutableSetOf<String>()

            if (lowercaseUsername.isNotEmpty()) {
                val firstChar = lowercaseUsername.first().toString()
                val firstCharCapitalized = firstChar.uppercase()

                // Search with lowercase first char
                val lowerResults = ordersCollection
                    .whereGreaterThanOrEqualTo("username", firstChar)
                    .whereLessThanOrEqualTo("username", firstChar + "\uf8ff")
                    .get().await()

                // Search with uppercase first char
                val upperResults = ordersCollection
                    .whereGreaterThanOrEqualTo("username", firstCharCapitalized)
                    .whereLessThanOrEqualTo("username", firstCharCapitalized + "\uf8ff")
                    .get().await()

                // Process results and filter client-side
                for (snapshot in listOf(lowerResults, upperResults)) {
                    snapshot.documents.forEach { doc ->
                        if (!orderIds.contains(doc.id)) {
                            documentToOrder(doc)?.let { order ->
                                if (order.username.lowercase().contains(lowercaseUsername)) {
                                    orderIds.add(doc.id)
                                    matchingOrders.add(order)
                                }
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "Search results for username '$username': ${matchingOrders.size}")
            emit(matchingOrders)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching orders by username: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getOrdersPage(limit: Int, offset: Int): Flow<List<Order>> = flow {
        try {
            val query = if (offset == 0) {
                ordersCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = ordersCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    ordersCollection
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<Order>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val orders = snapshot.documents.mapNotNull { documentToOrder(it) }
            Log.d(TAG, "Fetched page with limit=$limit, offset=$offset: ${orders.size} orders")
            emit(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders page: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getOrdersPageByUserId(userId: String, limit: Int, offset: Int): Flow<List<Order>> = flow {
        try {
            val baseQuery = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            val query = if (offset == 0) {
                baseQuery.limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = baseQuery
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    baseQuery
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<Order>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val orders = snapshot.documents.mapNotNull { documentToOrder(it) }
            Log.d(TAG, "Fetched user=$userId page with limit=$limit, offset=$offset: ${orders.size} orders")
            emit(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders page by user ID: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getOrdersPageByStatus(status: String, limit: Int, offset: Int): Flow<List<Order>> = flow {
        try {
            val baseQuery = ordersCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            val query = if (offset == 0) {
                baseQuery.limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = baseQuery
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    baseQuery
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<Order>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val orders = snapshot.documents.mapNotNull { documentToOrder(it) }
            Log.d(TAG, "Fetched status=$status page with limit=$limit, offset=$offset: ${orders.size} orders")
            emit(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders page by status: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getTotalOrdersCount(): Int {
        return try {
            val snapshot = ordersCollection.get().await()
            val count = snapshot.size()
            Log.d(TAG, "Total orders count: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total orders count: ${e.message}")
            0
        }
    }

    override suspend fun getTotalOrdersCountByUserId(userId: String): Int {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val count = snapshot.size()
            Log.d(TAG, "Total orders count for user $userId: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user orders count: ${e.message}")
            0
        }
    }

    override suspend fun getTotalOrdersCountByStatus(status: String): Int {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("status", status)
                .get()
                .await()
            val count = snapshot.size()
            Log.d(TAG, "Total orders count for status '$status': $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting status orders count: ${e.message}")
            0
        }
    }

    override suspend fun getAllOrdersByStatus(status: String): Flow<List<Order>> = flow {
        try {
            val snapshot = ordersCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            val orders = snapshot.documents.mapNotNull { documentToOrder(it) }
            Log.d(TAG, "Fetched all orders with status '$status': ${orders.size} orders")
            emit(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all orders by status: ${e.message}")
            emit(emptyList())
        }
    }
}