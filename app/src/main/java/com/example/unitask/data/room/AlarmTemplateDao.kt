package com.example.unitask.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmTemplateDao {
    
    @Query("SELECT * FROM alarm_templates ORDER BY minutesBefore ASC")
    fun getAllTemplates(): Flow<List<AlarmTemplateEntity>>
    
    @Query("SELECT * FROM alarm_templates ORDER BY minutesBefore ASC")
    suspend fun getAllTemplatesOnce(): List<AlarmTemplateEntity>
    
    @Query("SELECT * FROM alarm_templates WHERE id = :id")
    suspend fun getTemplateById(id: String): AlarmTemplateEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: AlarmTemplateEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<AlarmTemplateEntity>)
    
    @Delete
    suspend fun deleteTemplate(template: AlarmTemplateEntity)
    
    @Query("DELETE FROM alarm_templates WHERE isDefault = 0 AND id = :id")
    suspend fun deleteCustomTemplate(id: String)
    
    @Query("SELECT COUNT(*) FROM alarm_templates")
    suspend fun getCount(): Int
}
