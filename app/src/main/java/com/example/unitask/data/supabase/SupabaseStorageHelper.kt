package com.example.unitask.data.supabase

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.storage.storage
import java.io.ByteArrayOutputStream

/**
 * Helper para operaciones de Storage en Supabase.
 * Maneja la subida y descarga de archivos, especialmente imágenes de perfil.
 */
class SupabaseStorageHelper(private val context: Context) {
    
    private val client = SupabaseClientProvider.client
    
    companion object {
        const val AVATARS_BUCKET = "avatars"
    }
    
    /**
     * Sube una imagen de perfil al bucket de avatars.
     * 
     * @param userId ID del usuario
     * @param imageUri URI local de la imagen a subir
     * @return URL pública de la imagen subida
     */
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw IllegalArgumentException("Cannot open image URI")
        
        val bytes = inputStream.use { stream ->
            ByteArrayOutputStream().use { output ->
                stream.copyTo(output)
                output.toByteArray()
            }
        }
        
        val path = "profiles/$userId.jpg"
        
        // Subir imagen (upsert para reemplazar si existe)
        client.storage
            .from(AVATARS_BUCKET)
            .upload(path, bytes) {
                upsert = true
            }
        
        // Obtener URL pública
        return client.storage
            .from(AVATARS_BUCKET)
            .publicUrl(path)
    }
    
    /**
     * Obtiene la URL pública de una imagen de perfil.
     * 
     * @param userId ID del usuario
     * @return URL pública o null si no existe
     */
    fun getProfileImageUrl(userId: String): String {
        val path = "profiles/$userId.jpg"
        return client.storage
            .from(AVATARS_BUCKET)
            .publicUrl(path)
    }
    
    /**
     * Elimina la imagen de perfil de un usuario.
     * 
     * @param userId ID del usuario
     */
    suspend fun deleteProfileImage(userId: String) {
        val path = "profiles/$userId.jpg"
        client.storage
            .from(AVATARS_BUCKET)
            .delete(path)
    }
    
    /**
     * Sube cualquier archivo al bucket especificado.
     * 
     * @param bucket Nombre del bucket
     * @param path Ruta dentro del bucket
     * @param data Bytes del archivo
     * @return URL pública del archivo
     */
    suspend fun uploadFile(bucket: String, path: String, data: ByteArray): String {
        client.storage
            .from(bucket)
            .upload(path, data) {
                upsert = true
            }
        
        return client.storage
            .from(bucket)
            .publicUrl(path)
    }
}
