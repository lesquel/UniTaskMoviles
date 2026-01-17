package com.example.unitask.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.unitask.domain.model.Task
import java.time.LocalDateTime

/**
 * Room Entity representing a record in the 'tasks' table.
 *
 * @property subjectId Foreign Key linking to the 'subjects' table.
 *                     If the subject is deleted, the task is also deleted (CASCADE).
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Indexing foreign keys is a best practice for query performance.
    indices = [Index(value = ["subjectId"])]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subjectId: String,
    val dueDateTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val isCompleted: Boolean
)

// Mapping extensions
fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        subjectId = subjectId,
        dueDateTime = dueDateTime,
        createdAt = createdAt,
        isCompleted = isCompleted
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        subjectId = subjectId,
        dueDateTime = dueDateTime,
        createdAt = createdAt,
        isCompleted = isCompleted
    )
}
