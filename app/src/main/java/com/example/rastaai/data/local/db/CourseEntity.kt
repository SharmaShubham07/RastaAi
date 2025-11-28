package com.example.rastaai.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val categoryId: Long?,
    val categoryName: String?,
    val lessons: Int,
    val score: Int,
    val createdAt: Long = System.currentTimeMillis()
)
