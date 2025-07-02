package com.example.shopapp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopapp.data.model.Event
import com.example.shopapp.data.repository.EventRepository
import com.example.shopapp.data.repository.IRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel(private val repository: IRepository<Event>) : ViewModel() {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _selectedEvent = mutableStateOf<Event?>(null)
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

    var selectedEvent: Event?
        get() = _selectedEvent.value
        set(value) {
            _selectedEvent.value = value
        }

    init {
        loadInitialEvents()
    }

    fun loadInitialEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            if (repository is EventRepository) {
                val eventDao = (repository as? EventRepository)?.getEventDao()
                _totalCount.value = eventDao?.getTotalEventsCount() ?: 0

                eventDao?.getEventsPage(_pageSize.value, 0)?.collect { eventList ->
                    _events.value = eventList
                    _currentPage.value = 0
                    _hasMoreData.value = eventList.size >= _pageSize.value
                    _isLoading.value = false
                }
            } else {
                repository.fetchAll().collect { eventList ->
                    _events.value = eventList
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

            val eventDao = (repository as? EventRepository)?.getEventDao()
            eventDao?.getEventsPage(_pageSize.value, offset)?.collect { results ->
                if (results.isNotEmpty()) {
                    _events.value = results
                    _currentPage.value = nextPage
                    _hasMoreData.value = results.size >= _pageSize.value
                } else {
                    _hasMoreData.value = false
                }
                _isLoading.value = false
            }
        }
    }

    fun previousPage() {
        if (currentPage.value > 0) {
            viewModelScope.launch {
                _isLoading.value = true
                val prevPage = _currentPage.value - 1
                val offset = prevPage * _pageSize.value

                val eventDao = (repository as? EventRepository)?.getEventDao()
                eventDao?.getEventsPage(_pageSize.value, offset)?.collect { results ->
                    _events.value = results
                    _currentPage.value = prevPage
                    _hasMoreData.value = true
                    _isLoading.value = false
                }
            }
        }
    }

    fun goToPage(page: Int) {
        if (page != _currentPage.value) {
            viewModelScope.launch {
                _isLoading.value = true
                val offset = page * _pageSize.value

                val eventDao = (repository as? EventRepository)?.getEventDao()
                eventDao?.getEventsPage(_pageSize.value, offset)?.collect { results ->
                    _events.value = results
                    _currentPage.value = page
                    _hasMoreData.value = results.size >= _pageSize.value
                    _isLoading.value = false
                }
            }
        }
    }

    fun filterByEventType(eventType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            if (repository is EventRepository) {
                val eventDao = (repository as? EventRepository)?.getEventDao()
                _totalCount.value = eventDao?.getTotalEventsCountByType(eventType) ?: 0

                eventDao?.getEventsPageByType(eventType, _pageSize.value, 0)?.collect { results ->
                    _events.value = results
                    _currentPage.value = 0
                    _hasMoreData.value = results.size >= _pageSize.value
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun filterByStatus(status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            if (repository is EventRepository) {
                val eventDao = (repository as? EventRepository)?.getEventDao()
                _totalCount.value = eventDao?.getTotalEventsCountByStatus(status) ?: 0

                eventDao?.getEventsPageByStatus(status, _pageSize.value, 0)?.collect { results ->
                    _events.value = results
                    _currentPage.value = 0
                    _hasMoreData.value = results.size >= _pageSize.value
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun filterByTypeAndStatus(eventType: String, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            if (repository is EventRepository) {
                // First get the events by type
                repository.searchEventsByType(eventType).collect { typeResults ->
                    // Then filter by status
                    val filteredResults = typeResults.filter { it.status == status }
                    _events.value = filteredResults
                    _currentPage.value = 0
                    _hasMoreData.value = filteredResults.size >= _pageSize.value
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun searchEventsByTitle(title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            if (repository is EventRepository) {
                repository.searchEventsByTitle(title).collect { results ->
                    _events.value = results
                    _isLoading.value = false
                }
            } else {
                Log.d("EventViewModel", "Repository does not support search")
                _isLoading.value = false
            }
        }
    }

    fun searchEventsByDescription(description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            if (repository is EventRepository) {
                repository.searchEventsByDescription(description).collect { results ->
                    _events.value = results
                    _isLoading.value = false
                }
            } else {
                Log.d("EventViewModel", "Repository does not support search")
                _isLoading.value = false
            }
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            if (repository.create(event)) {
                loadInitialEvents()
            }
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            if (repository.modify(event)) {
                loadInitialEvents()
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            if (repository.remove(eventId)) {
                loadInitialEvents()
            }
        }
    }

    fun getEventById(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val event = repository.fetchById(eventId)
            selectedEvent = event
            _isLoading.value = false
        }
    }

    fun selectEvent(event: Event) {
        selectedEvent = event
    }

    fun clearSelection() {
        selectedEvent = null
    }

    fun resetFiltersAndSearch() {
        _currentPage.value = 0
        _hasMoreData.value = true

        loadInitialEvents()

        Log.d("EventViewModel", "Event filters reset")
    }
}