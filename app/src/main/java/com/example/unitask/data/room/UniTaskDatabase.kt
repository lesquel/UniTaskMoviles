package com.example.unitask.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The main database holder for the application.
 * Manages the connection to the SQLite database and provides DAOs.
 *
 * @see TaskDao
 * @see SubjectDao
 * @see UserDao
 */
@Database(
    entities = [TaskEntity::class, SubjectEntity::class, UserEntity::class, UserStatsEntity::class, AlarmTemplateEntity::class],
    version = 4,
    exportSchema = false
)
// Registers custom type converters (e.g. for LocalDateTime <-> String).
@TypeConverters(Converters::class)
abstract class UniTaskDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
    abstract val subjectDao: SubjectDao
    abstract val userDao: UserDao
    abstract val alarmTemplateDao: AlarmTemplateDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crear tabla de usuarios
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id TEXT PRIMARY KEY NOT NULL,
                        username TEXT NOT NULL,
                        email TEXT NOT NULL,
                        passwordHash TEXT NOT NULL,
                        profileImagePath TEXT,
                        totalXp INTEGER NOT NULL DEFAULT 0,
                        createdAt TEXT NOT NULL,
                        isLoggedIn INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // Crear tabla de estadísticas de usuario
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_stats (
                        userId TEXT PRIMARY KEY NOT NULL,
                        totalTasksCompleted INTEGER NOT NULL DEFAULT 0,
                        currentStreak INTEGER NOT NULL DEFAULT 0,
                        longestStreak INTEGER NOT NULL DEFAULT 0,
                        lastCompletedDate TEXT
                    )
                """)
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crear tabla de plantillas de alarma
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS alarm_templates (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        minutesBefore INTEGER NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Agregar columna userId a la tabla tasks
                // Primero creamos una tabla temporal con la nueva estructura
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tasks_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL DEFAULT '',
                        title TEXT NOT NULL,
                        subjectId TEXT NOT NULL,
                        dueDateTime TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // Copiar datos existentes (con userId vacío por defecto)
                db.execSQL("""
                    INSERT INTO tasks_new (id, userId, title, subjectId, dueDateTime, createdAt, isCompleted)
                    SELECT id, '', title, subjectId, dueDateTime, createdAt, isCompleted FROM tasks
                """)
                
                // Eliminar la tabla antigua
                db.execSQL("DROP TABLE tasks")
                
                // Renombrar la nueva tabla
                db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
                
                // Crear índices
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_subjectId ON tasks(subjectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_userId ON tasks(userId)")
            }
        }
    }
}
