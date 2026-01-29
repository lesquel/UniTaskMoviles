package com.example.unitask.data.supabase

import com.example.unitask.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Singleton que proporciona acceso al cliente de Supabase.
 * 
 * IMPORTANTE: Antes de usar Supabase, debes:
 * 1. Crear un proyecto en https://supabase.com
 * 2. Obtener tu URL y Anon Key del Dashboard > Settings > API
 * 3. Actualizar los valores en build.gradle.kts:
 *    - buildConfigField("String", "SUPABASE_URL", "\"https://tu-proyecto.supabase.co\"")
 *    - buildConfigField("String", "SUPABASE_ANON_KEY", "\"tu-anon-key\"")
 */
object SupabaseClientProvider {
    
    /**
     * Cliente de Supabase configurado con Auth, Postgrest y Storage.
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Autenticación
            install(Auth) {
                // Configuración opcional de auth
            }
            
            // Base de datos (Postgrest)
            install(Postgrest) {
                // Configuración opcional de postgrest
            }
            
            // Almacenamiento de archivos
            install(Storage) {
                // Configuración opcional de storage
            }
        }
    }
    
    /**
     * Verifica si las credenciales de Supabase están configuradas.
     */
    fun isConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL.isNotEmpty() &&
               BuildConfig.SUPABASE_ANON_KEY.isNotEmpty() &&
               BuildConfig.SUPABASE_URL != "https://your-project.supabase.co"
    }
}
