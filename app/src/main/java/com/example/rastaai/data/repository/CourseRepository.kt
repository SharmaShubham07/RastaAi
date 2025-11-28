package com.example.rastaai.data.repository

import com.example.rastaai.data.local.dao.CategoryDao
import com.example.rastaai.data.local.dao.CourseDao
import com.example.rastaai.data.local.db.CategoryEntity
import com.example.rastaai.data.local.db.CourseEntity
import com.example.rastaai.data.remote.CategoryApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val courseDao: CourseDao,
    private val categoryDao: CategoryDao,
    private val categoryApi: CategoryApi
) {
    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()

    fun searchCourses(query: String): Flow<List<CourseEntity>> = courseDao.search(query)

    fun filterCoursesByCategory(categoryId: Long): Flow<List<CourseEntity>> = courseDao.filterByCategory(categoryId)

    suspend fun getCourse(id: Long): CourseEntity? = courseDao.getCourse(id)

    suspend fun saveCourse(
        id: Long?,
        title: String,
        description: String,
        categoryId: Long?,
        categoryName: String?,
        lessons: Int
    ) {
        val score = title.length * lessons
        val entity = CourseEntity(
            id = id ?: 0,
            title = title.trim(),
            description = description.trim(),
            categoryId = categoryId,
            categoryName = categoryName,
            lessons = lessons,
            score = score
        )
        withContext(Dispatchers.IO) {
            if (id == null || id == 0L) {
                courseDao.insert(entity)
            } else {
                val existing = courseDao.getCourse(id)
                if (existing != null) {
                    val updated = entity.copy(id = id, createdAt = existing.createdAt)
                    courseDao.update(updated)
                } else {
                    courseDao.insert(entity)
                }
            }
        }
    }

    suspend fun deleteCourse(course: CourseEntity) {
        withContext(Dispatchers.IO) {
            courseDao.delete(course)
        }
    }

    suspend fun refreshCategories(): Result<Unit> {
        return try {
            val resp = categoryApi.getCategories()
            if (resp.isSuccessful) {
                val body = resp.body() ?: emptyList()
                val entities = body.map { CategoryEntity(it.id, it.name) }
                categoryDao.clear()
                categoryDao.insertAll(entities)
                Result.success(Unit)
            } else {
                Result.failure(HttpException(resp))
            }
        } catch (e: IOException) {
            Result.failure(e) // network I/O
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCategoriesFlow(): Flow<List<CategoryEntity>> = categoryDao.getCategories()
}
