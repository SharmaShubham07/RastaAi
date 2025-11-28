package com.example.rastaai.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rastaai.data.local.db.CourseEntity
import com.example.rastaai.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val repo: CourseRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _categoryFilter = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val courses: StateFlow<List<CourseEntity>> = combine(
        _query.debounce(250),
        _categoryFilter
    ) { query, catId ->
        query to catId
    }.flatMapLatest { (query, catId) ->
        when {
            query.isNotBlank() -> repo.searchCourses(query)
            catId != null -> repo.filterCoursesByCategory(catId)
            else -> repo.getAllCourses()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _query.value = q }

    fun setCategoryFilter(catId: Long?) { _categoryFilter.value = catId }

    fun deleteCourse(course: CourseEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repo.deleteCourse(course)
            onComplete()
        }
    }
}
