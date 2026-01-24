package com.example.unitask.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.unitask.domain.model.AlarmTemplate

@Entity(tableName = "alarm_templates")
data class AlarmTemplateEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val minutesBefore: Int,
    val isDefault: Boolean
) {
    fun toDomain(): AlarmTemplate = AlarmTemplate(
        id = id,
        name = name,
        minutesBefore = minutesBefore,
        isDefault = isDefault
    )

    companion object {
        fun fromDomain(template: AlarmTemplate): AlarmTemplateEntity = AlarmTemplateEntity(
            id = template.id,
            name = template.name,
            minutesBefore = template.minutesBefore,
            isDefault = template.isDefault
        )
    }
}
