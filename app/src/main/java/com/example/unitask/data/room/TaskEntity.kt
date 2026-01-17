package com.example.unitask.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.unitask.domain.model.Task
import java.time.LocalDateTime

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
