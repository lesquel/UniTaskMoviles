package com.example.unitask.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The main database holder for the application.
 * Manages the connection to the SQLite database and provides DAOs.
 *
 * @see TaskDao
 * @see SubjectDao
 */
@Database(
    entities = [TaskEntity::class, SubjectEntity::class],
    version = 1,
    exportSchema = false
)
// Registers custom type converters (e.g. for LocalDateTime <-> String).
@TypeConverters(Converters::class)
abstract class UniTaskDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
    abstract val subjectDao: SubjectDao
}
