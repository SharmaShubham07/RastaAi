package com.example.rastaai.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rastaai.data.local.db.CategoryEntity
import com.example.rastaai.data.local.db.CourseEntity
import com.example.rastaai.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val repo: CourseRepository
) : ViewModel() {

    // --------------------------------------------------------
    // STATE FLOWS
    // --------------------------------------------------------

    private val _query = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _isLoading = MutableStateFlow(true)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --------------------------------------------------------
    // CATEGORIES FLOW (offline-first DB cache)
    // --------------------------------------------------------
    val categories: StateFlow<List<CategoryEntity>> =
        repo.getCategoriesFlow()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // --------------------------------------------------------
    // COURSES LIST (Search + Category filter)
    // --------------------------------------------------------
    val courses: StateFlow<List<CourseEntity>> =
        combine(
            _query.debounce(200),
            _selectedCategoryId,
            repo.getAllCourses()
        ) { query, catId, allCourses ->

            var filtered = allCourses

            // Apply search
            if (query.isNotBlank()) {
                val q = query.trim().lowercase()
                filtered = filtered.filter {
                    it.title.lowercase().contains(q) ||
                            it.description.lowercase().contains(q)
                }
            }

            // Apply category filter
            if (catId != null) {
                filtered = filtered.filter { it.categoryId == catId }
            }

            filtered
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // --------------------------------------------------------
    // INIT: Refresh categories from API (offline safe)
    // --------------------------------------------------------
    init {
        viewModelScope.launch {
            _isLoading.value = true
            repo.refreshCategories()  // network → DB
            _isLoading.value = false
        }
    }

    // --------------------------------------------------------
    // USER ACTIONS
    // --------------------------------------------------------

    fun setQuery(query: String) {
        _query.value = query
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    suspend fun getCourse(id: Long): CourseEntity? {
        return repo.getCourse(id)
    }

    fun deleteCourse(course: CourseEntity, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                repo.deleteCourse(course)
                onComplete(Result.success(Unit))
            } catch (e: Exception) {
                onComplete(Result.failure(e))
            }
        }
    }

    fun refreshCategories(onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repo.refreshCategories()
            onComplete(result)
        }
    }
}