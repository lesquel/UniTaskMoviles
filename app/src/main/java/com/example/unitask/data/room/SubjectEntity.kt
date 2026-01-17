package com.example.unitask.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.unitask.domain.model.Subject

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val colorHex: String,
    val teacher: String?
)

fun SubjectEntity.toDomain(): Subject {
    return Subject(
        id = id,
        name = name,
        colorHex = colorHex,
        teacher = teacher
    )
}

fun Subject.toEntity(): SubjectEntity {
    return SubjectEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        teacher = teacher
    )
}
