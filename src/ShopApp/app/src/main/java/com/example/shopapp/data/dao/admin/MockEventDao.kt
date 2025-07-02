package com.example.shopapp.data.dao.admin

import com.example.shopapp.data.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.google.firebase.Timestamp

class MockEventDao : IEventDao {
    private val events = mutableListOf<Event>()

    init {
        val now = Timestamp.now()

        // Create mock events with different types and statuses
        events.addAll(listOf(
            Event(
                eventId = "event1",
                eventType = "voucher",
                title = "Welcome Discount",
                description = "10% off for new users",
                discountType = "percentage",
                discountValue = 10.0,
                startDate = Timestamp(now.seconds - 86400 * 30, 0),
                endDate = Timestamp(now.seconds + 86400 * 30, 0),
                applicableProducts = null,
                minPurchase = 50.0,
                maxDiscount = 100.0,
                applicableUsers = listOf("new_users"),
                usageLimit = 1,
                status = "active",
                createdAt = Timestamp(now.seconds - 86400 * 60, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 60, 0)
            ),
            Event(
                eventId = "event2",
                eventType = "flash sale",
                title = "Weekend Flash Sale",
                description = "Up to 50% off on selected electronics",
                discountType = "percentage",
                discountValue = 50.0,
                startDate = Timestamp(now.seconds - 86400 * 2, 0),
                endDate = Timestamp(now.seconds + 86400 * 2, 0),
                applicableProducts = listOf("prod1", "prod2", "prod3"),
                minPurchase = 0.0,
                maxDiscount = 500.0,
                applicableUsers = null,
                usageLimit = 0,
                status = "active",
                createdAt = Timestamp(now.seconds - 86400 * 5, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 3, 0)
            ),
            Event(
                eventId = "event3",
                eventType = "voucher",
                title = "Summer Discount",
                description = "$25 off on orders above $100",
                discountType = "fixed amount",
                discountValue = 25.0,
                startDate = Timestamp(now.seconds - 86400 * 45, 0),
                endDate = Timestamp(now.seconds - 86400 * 15, 0),
                applicableProducts = null,
                minPurchase = 100.0,
                maxDiscount = 25.0,
                applicableUsers = null,
                usageLimit = 1000,
                status = "expired",
                createdAt = Timestamp(now.seconds - 86400 * 60, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 45, 0)
            ),
            Event(
                eventId = "event4",
                eventType = "flash sale",
                title = "Black Friday Special",
                description = "Huge discounts on all products",
                discountType = "percentage",
                discountValue = 30.0,
                startDate = Timestamp(now.seconds + 86400 * 30, 0),
                endDate = Timestamp(now.seconds + 86400 * 32, 0),
                applicableProducts = null,
                minPurchase = 0.0,
                maxDiscount = 300.0,
                applicableUsers = null,
                usageLimit = 0,
                status = "upcoming",
                createdAt = Timestamp(now.seconds - 86400 * 10, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 10, 0)
            ),
            Event(
                eventId = "event5",
                eventType = "voucher",
                title = "Loyalty Reward",
                description = "15% off for loyal customers",
                discountType = "percentage",
                discountValue = 15.0,
                startDate = Timestamp(now.seconds - 86400 * 15, 0),
                endDate = Timestamp(now.seconds + 86400 * 15, 0),
                applicableProducts = null,
                minPurchase = 25.0,
                maxDiscount = 150.0,
                applicableUsers = listOf("user1", "user2", "user5"),
                usageLimit = 1,
                status = "active",
                createdAt = Timestamp(now.seconds - 86400 * 20, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 20, 0)
            ),
            Event(
                eventId = "event6",
                eventType = "flash sale",
                title = "Holiday Special",
                description = "Up to 40% off on holiday items",
                discountType = "percentage",
                discountValue = 40.0,
                startDate = Timestamp(now.seconds - 86400 * 3, 0),
                endDate = Timestamp(now.seconds + 86400 * 7, 0),
                applicableProducts = listOf("prod7", "prod8", "prod9", "prod10"),
                minPurchase = 0.0,
                maxDiscount = 200.0,
                applicableUsers = null,
                usageLimit = 0,
                status = "active",
                createdAt = Timestamp(now.seconds - 86400 * 10, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 5, 0)
            ),
            Event(
                eventId = "event7",
                eventType = "voucher",
                title = "First Purchase",
                description = "$10 off on first purchase",
                discountType = "fixed amount",
                discountValue = 10.0,
                startDate = Timestamp(now.seconds - 86400 * 60, 0),
                endDate = Timestamp(now.seconds + 86400 * 30, 0),
                applicableProducts = null,
                minPurchase = 30.0,
                maxDiscount = 10.0,
                applicableUsers = listOf("new_users"),
                usageLimit = 1,
                status = "active",
                createdAt = Timestamp(now.seconds - 86400 * 70, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 70, 0)
            ),
            Event(
                eventId = "event8",
                eventType = "flash sale",
                title = "Tech Tuesday",
                description = "20% off on all tech gadgets",
                discountType = "percentage",
                discountValue = 20.0,
                startDate = Timestamp(now.seconds + 86400 * 7, 0),
                endDate = Timestamp(now.seconds + 86400 * 8, 0),
                applicableProducts = listOf("prod1", "prod2", "prod4", "prod15", "prod16", "prod18"),
                minPurchase = 0.0,
                maxDiscount = 100.0,
                applicableUsers = null,
                usageLimit = 0,
                status = "upcoming",
                createdAt = Timestamp(now.seconds - 86400 * 5, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 5, 0)
            ),
            Event(
                eventId = "event9",
                eventType = "voucher",
                title = "Bulk Purchase",
                description = "25% off on orders above $200",
                discountType = "percentage",
                discountValue = 25.0,
                startDate = Timestamp(now.seconds - 86400 * 20, 0),
                endDate = Timestamp(now.seconds - 86400 * 5, 0),
                applicableProducts = null,
                minPurchase = 200.0,
                maxDiscount = 100.0,
                applicableUsers = null,
                usageLimit = 500,
                status = "expired",
                createdAt = Timestamp(now.seconds - 86400 * 30, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 30, 0)
            ),
            Event(
                eventId = "event10",
                eventType = "voucher",
                title = "Referral Discount",
                description = "20% off for referred friends",
                discountType = "percentage",
                discountValue = 20.0,
                startDate = Timestamp(now.seconds - 86400 * 10, 0),
                endDate = Timestamp(now.seconds + 86400 * 80, 0),
                applicableProducts = null,
                minPurchase = 50.0,
                maxDiscount = 50.0,
                applicableUsers = null,
                usageLimit = 1,
                status = "active",
                createdAt = Timestamp(now.seconds - 86400 * 15, 0),
                updatedAt = Timestamp(now.seconds - 86400 * 15, 0)
            )
        ))
    }

    override suspend fun getAll(): Flow<List<Event>> {
        return flowOf(events)
    }

    override suspend fun getById(id: String): Event? {
        return events.find { it.eventId == id }
    }

    override suspend fun add(item: Event): Boolean {
        return events.add(item)
    }

    override suspend fun update(item: Event): Boolean {
        val index = events.indexOfFirst { it.eventId == item.eventId }
        if (index != -1) {
            events[index] = item
            return true
        }
        return false
    }

    override suspend fun delete(id: String): Boolean {
        return events.removeIf { it.eventId == id }
    }

    override suspend fun searchEventsByTitle(title: String): Flow<List<Event>> {
        return flowOf(events.filter { it.title.contains(title, ignoreCase = true) })
    }

    override suspend fun searchEventsByDescription(description: String): Flow<List<Event>> {
        return flowOf(events.filter { it.description.contains(description, ignoreCase = true) })
    }

    override suspend fun searchEventsByType(eventType: String): Flow<List<Event>> {
        return flowOf(events.filter { it.eventType == eventType })
    }

    override suspend fun searchEventsByStatus(status: String): Flow<List<Event>> {
        return flowOf(events.filter { it.status == status })
    }

    override suspend fun getEventsPage(limit: Int, offset: Int): Flow<List<Event>> {
        return flowOf(events.sortedByDescending { it.createdAt }.drop(offset).take(limit))
    }

    override suspend fun getEventsPageByType(eventType: String, limit: Int, offset: Int): Flow<List<Event>> {
        return flowOf(events
            .filter { it.eventType == eventType }
            .sortedByDescending { it.createdAt }
            .drop(offset)
            .take(limit))
    }

    override suspend fun getEventsPageByStatus(status: String, limit: Int, offset: Int): Flow<List<Event>> {
        return flowOf(events
            .filter { it.status == status }
            .sortedByDescending { it.createdAt }
            .drop(offset)
            .take(limit))
    }

    override suspend fun getTotalEventsCount(): Int {
        return events.size
    }

    override suspend fun getTotalEventsCountByType(eventType: String): Int {
        return events.count { it.eventType == eventType }
    }

    override suspend fun getTotalEventsCountByStatus(status: String): Int {
        return events.count { it.status == status }
    }
}