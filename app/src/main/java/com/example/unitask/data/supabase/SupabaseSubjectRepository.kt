package com.example.unitask.data.supabase

import android.util.Log
import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.repository.SubjectRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Implementación de SubjectRepository usando Supabase Postgrest.
 */
class SupabaseSubjectRepository(
    private val getCurrentUserId: suspend () -> String?
) : SubjectRepository {
    
    private val client = SupabaseClientProvider.client
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Cache local de materias
    private val _subjects = MutableStateFlow<List<SubjectDto>>(emptyList())
    
    // Estado de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: Flow<Boolean> = _isLoading
    
    companion object {
        private const val TAG = "SupabaseSubjectRepo"
        private const val TABLE_NAME = "subjects"
    }
    
    init {
        // Cargar materias iniciales
        scope.launch {
            refreshSubjects()
        }
    }
    
    @Serializable
    data class SubjectDto(
        val id: String,
        @SerialName("user_id")
        val userId: String? = null,
        val name: String,
        @SerialName("color_hex")
        val colorHex: String,
        val teacher: String? = null,
        @SerialName("is_shared")
        val isShared: Boolean = false,
        @SerialName("created_at")
        val createdAt: String? = null
    )
    
    override fun getSubjectsFlow(): Flow<List<Subject>> {
        return _subjects.map { dtos -> dtos.map { it.toDomain() } }
    }
    
    override suspend fun addSubject(subject: Subject) {
        try {
            val userId = getCurrentUserId() ?: throw IllegalStateException("No user logged in")
            
            Log.d(TAG, "Adding subject: ${subject.name}")
            
            val dto = SubjectDto(
                id = subject.id,
                userId = userId,
                name = subject.name,
                colorHex = subject.colorHex,
                teacher = subject.teacher,
                isShared = false
            )
            
            client.postgrest
                .from(TABLE_NAME)
                .insert(dto)
            
            Log.d(TAG, "Subject added successfully")
            refreshSubjects()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding subject", e)
            throw e
        }
    }
    
    override suspend fun editSubject(subject: Subject) {
        try {
            Log.d(TAG, "Editing subject: ${subject.id}")
            
            client.postgrest
                .from(TABLE_NAME)
                .update({
                    set("name", subject.name)
                    set("color_hex", subject.colorHex)
                    set("teacher", subject.teacher)
                }) {
                    filter {
                        eq("id", subject.id)
                    }
                }
            
            Log.d(TAG, "Subject updated successfully")
            refreshSubjects()
        } catch (e: Exception) {
            Log.e(TAG, "Error editing subject", e)
            throw e
        }
    }
    
    override suspend fun deleteSubject(subjectId: String) {
        try {
            Log.d(TAG, "Deleting subject: $subjectId")
            
            // Primero eliminar tareas asociadas
            client.postgrest
                .from("tasks")
                .delete {
                    filter {
                        eq("subject_id", subjectId)
                    }
                }
            
            // Luego eliminar la materia
            client.postgrest
                .from(TABLE_NAME)
                .delete {
                    filter {
                        eq("id", subjectId)
                    }
                }
            
            Log.d(TAG, "Subject deleted successfully")
            refreshSubjects()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subject", e)
            throw e
        }
    }
    
    /**
     * Refresca todas las materias desde Supabase.
     */
    suspend fun refreshSubjects() {
        try {
            _isLoading.value = true
            Log.d(TAG, "Refreshing subjects...")
            
            val result = client.postgrest
                .from(TABLE_NAME)
                .select()
                .decodeList<SubjectDto>()
            
            _subjects.value = result
            Log.d(TAG, "Loaded ${result.size} subjects")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing subjects", e)
        } finally {
            _isLoading.value = false
        }
    }
    
    // Extensiones para conversión
    private fun SubjectDto.toDomain(): Subject {
        return Subject(
            id = id,
            name = name,
            colorHex = colorHex,
            teacher = teacher
        )
    }
}
