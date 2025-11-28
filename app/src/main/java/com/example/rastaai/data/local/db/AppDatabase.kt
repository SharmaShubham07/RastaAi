package com.example.rastaai.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CourseEntity::class, CategoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): com.example.rastaai.data.local.dao.CourseDao
    abstract fun categoryDao(): com.example.rastaai.data.local.dao.CategoryDao
}
