package com.example.shopapp.data.dao.admin

import com.example.shopapp.data.model.Order
import com.example.shopapp.data.model.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.google.firebase.Timestamp

class MockOrderDao : IOrderDao {
    private val orders = mutableListOf<Order>()

    init {
        val now = Timestamp.now()

        // Create mock orders
        orders.addAll(listOf(
            Order(
                orderId = "order1",
                userId = "user1",
                username = "john_doe",
                phone = "555-123-4567",
                address = "123 Main St, City",
                orderDetail = listOf(
                    CartItem(productId = "prod1", productTitle = "Smartphone X", productImage = "https://picsum.photos/id/1/500/500", price = 699.99, quantity = 1),
                    CartItem(productId = "prod3", productTitle = "Wireless Earbuds", productImage = "https://picsum.photos/id/3/500/500", price = 129.99, quantity = 1)
                ),
                totalPrice = 829.98,
                status = "delivered",
                createdAt = Timestamp(now.seconds - 86400 * 7, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 86400 * 5, now.nanoseconds)
            ),
            Order(
                orderId = "order2",
                userId = "user2",
                username = "jane_smith",
                phone = "555-987-6543",
                address = "456 Oak Ave, Town",
                orderDetail = listOf(
                    CartItem(productId = "prod2", productTitle = "Laptop Pro", productImage = "https://picsum.photos/id/2/500/500", price = 1299.99, quantity = 1)
                ),
                totalPrice = 1299.99,
                status = "shipping",
                createdAt = Timestamp(now.seconds - 86400 * 3, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 86400 * 2, now.nanoseconds)
            ),
            Order(
                orderId = "order3",
                userId = "user3",
                username = "mike_wilson",
                phone = "555-555-5555",
                address = "789 Pine St, Village",
                orderDetail = listOf(
                    CartItem(productId = "prod4", productTitle = "Smart Watch", productImage = "https://picsum.photos/id/4/500/500", price = 249.99, quantity = 1),
                    CartItem(productId = "prod5", productTitle = "Fitness Tracker", productImage = "https://picsum.photos/id/5/500/500", price = 79.99, quantity = 2)
                ),
                totalPrice = 409.97,
                status = "pending",
                createdAt = Timestamp(now.seconds - 86400 * 1, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 43200, now.nanoseconds)
            ),
            Order(
                orderId = "order4",
                userId = "user1",
                username = "john_doe",
                phone = "555-123-4567",
                address = "123 Main St, City",
                orderDetail = listOf(
                    CartItem(productId = "prod6", productTitle = "Bluetooth Speaker", productImage = "https://picsum.photos/id/6/500/500", price = 89.99, quantity = 1)
                ),
                totalPrice = 89.99,
                status = "pending",
                createdAt = Timestamp(now.seconds - 3600, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 3600, now.nanoseconds)
            ),
            Order(
                orderId = "order5",
                userId = "user5",
                username = "david_brown",
                phone = "555-333-4444",
                address = "202 Elm St, Metropolis",
                orderDetail = listOf(
                    CartItem(productId = "prod7", productTitle = "Gaming Console", productImage = "https://picsum.photos/id/7/500/500", price = 499.99, quantity = 1),
                    CartItem(productId = "prod8", productTitle = "Game Controller", productImage = "https://picsum.photos/id/8/500/500", price = 59.99, quantity = 2),
                    CartItem(productId = "prod9", productTitle = "Video Game", productImage = "https://picsum.photos/id/9/500/500", price = 59.99, quantity = 3)
                ),
                totalPrice = 799.94,
                status = "delivered",
                createdAt = Timestamp(now.seconds - 86400 * 30, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 86400 * 28, now.nanoseconds)
            ),
            Order(
                orderId = "order6",
                userId = "user7",
                username = "robert_garcia",
                phone = "555-666-7777",
                address = "404 Birch Rd, Uptown",
                orderDetail = listOf(
                    CartItem(productId = "prod10", productTitle = "4K Smart TV", productImage = "https://picsum.photos/id/10/500/500", price = 899.99, quantity = 1),
                    CartItem(productId = "prod11", productTitle = "Soundbar", productImage = "https://picsum.photos/id/11/500/500", price = 199.99, quantity = 1)
                ),
                totalPrice = 1099.98,
                status = "cancelled",
                createdAt = Timestamp(now.seconds - 86400 * 10, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 86400 * 9, now.nanoseconds)
            ),
            Order(
                orderId = "order7",
                userId = "user8",
                username = "emma_taylor",
                phone = "555-777-8888",
                address = "505 Walnut Ave, Countryside",
                orderDetail = listOf(
                    CartItem(productId = "prod12", productTitle = "Digital Camera", productImage = "https://picsum.photos/id/12/500/500", price = 449.99, quantity = 1),
                    CartItem(productId = "prod13", productTitle = "Camera Lens", productImage = "https://picsum.photos/id/13/500/500", price = 349.99, quantity = 1),
                    CartItem(productId = "prod14", productTitle = "Camera Bag", productImage = "https://picsum.photos/id/14/500/500", price = 79.99, quantity = 1)
                ),
                totalPrice = 879.97,
                status = "delivered",
                createdAt = Timestamp(now.seconds - 86400 * 60, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 86400 * 58, now.nanoseconds)
            ),
            Order(
                orderId = "order8",
                userId = "user4",
                username = "alice_johnson",
                phone = "555-111-2222",
                address = "101 Maple Dr, Suburb",
                orderDetail = listOf(
                    CartItem(productId = "prod15", productTitle = "Wireless Keyboard", productImage = "https://picsum.photos/id/15/500/500", price = 49.99, quantity = 1),
                    CartItem(productId = "prod16", productTitle = "Wireless Mouse", productImage = "https://picsum.photos/id/16/500/500", price = 29.99, quantity = 1)
                ),
                totalPrice = 79.98,
                status = "shipping",
                createdAt = Timestamp(now.seconds - 86400 * 2, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 86400 * 1, now.nanoseconds)
            ),
            Order(
                orderId = "order9",
                userId = "user10",
                username = "olivia_martinez",
                phone = "555-999-0000",
                address = "707 Oak Circle, Riverside",
                orderDetail = listOf(
                    CartItem(productId = "prod17", productTitle = "Coffee Maker", productImage = "https://picsum.photos/id/17/500/500", price = 129.99, quantity = 1)
                ),
                totalPrice = 129.99,
                status = "pending",
                createdAt = Timestamp(now.seconds - 43200, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 21600, now.nanoseconds)
            ),
            Order(
                orderId = "order10",
                userId = "user1",
                username = "john_doe",
                phone = "555-123-4567",
                address = "123 Main St, City",
                orderDetail = listOf(
                    CartItem(productId = "prod18", productTitle = "Tablet", productImage = "https://picsum.photos/id/18/500/500", price = 349.99, quantity = 1),
                    CartItem(productId = "prod19", productTitle = "Screen Protector", productImage = "https://picsum.photos/id/19/500/500", price = 19.99, quantity = 1),
                    CartItem(productId = "prod20", productTitle = "Tablet Case", productImage = "https://picsum.photos/id/20/500/500", price = 29.99, quantity = 1)
                ),
                totalPrice = 399.97,
                status = "pending",
                createdAt = Timestamp(now.seconds - 1800, now.nanoseconds),
                updatedAt = Timestamp(now.seconds - 1800, now.nanoseconds)
            )
        ))
    }

    override suspend fun getAll(): Flow<List<Order>> {
        return flowOf(orders)
    }

    override suspend fun getById(id: String): Order? {
        return orders.find { it.orderId == id }
    }

    override suspend fun add(item: Order): Boolean {
        return orders.add(item)
    }

    override suspend fun update(item: Order): Boolean {
        val index = orders.indexOfFirst { it.orderId == item.orderId }
        if (index != -1) {
            orders[index] = item
            return true
        }
        return false
    }

    override suspend fun delete(id: String): Boolean {
        return orders.removeIf { it.orderId == id }
    }

    override suspend fun searchOrdersByUserId(userId: String): Flow<List<Order>> {
        return flowOf(orders.filter { it.userId == userId }.sortedByDescending { it.createdAt })
    }

    override suspend fun searchOrdersByUsername(username: String): Flow<List<Order>> {
        return flowOf(orders.filter { it.username.contains(username, ignoreCase = true) })
    }

    override suspend fun getOrdersPage(limit: Int, offset: Int): Flow<List<Order>> {
        return flowOf(orders.sortedByDescending { it.createdAt }.drop(offset).take(limit))
    }
    override suspend fun getOrdersPageByUserId(userId: String, limit: Int, offset: Int): Flow<List<Order>> {
        return flowOf(orders.filter { it.userId == userId }.sortedByDescending { it.createdAt }.drop(offset).take(limit))
    }
    override suspend fun getOrdersPageByStatus(status: String, limit: Int, offset: Int): Flow<List<Order>> {
        return flowOf(orders.filter { it.status == status }.sortedByDescending { it.createdAt }.drop(offset).take(limit))
    }
    override suspend fun getTotalOrdersCount(): Int {
        return orders.size
    }
    override suspend fun getTotalOrdersCountByUserId(userId: String): Int {
        return orders.count { it.userId == userId }
    }
    override suspend fun getTotalOrdersCountByStatus(status: String): Int {
        return orders.count { it.status == status }
    }
    override suspend fun getAllOrdersByStatus(status: String): Flow<List<Order>> {
        return flowOf(orders.filter { it.status == status })
    }
}