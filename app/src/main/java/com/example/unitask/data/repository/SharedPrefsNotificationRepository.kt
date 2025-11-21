package com.example.unitask.data.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private const val PREFS_NAME = "unitask_prefs"
private const val KEY_NOTIFICATIONS = "notifications_json"

class SharedPrefsNotificationRepository(private val context: Context, private val alarmScheduler: com.example.unitask.notifications.AlarmScheduler) : NotificationRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(loadAll())

    // Carga la lista serializada de ajustes de notificación desde SharedPreferences.
    private fun loadAll(): List<NotificationSetting> {
        val raw = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                NotificationSetting(
                    id = o.getString("id"),
                    taskId = if (o.isNull("taskId")) null else o.getString("taskId"),
                    enabled = o.getBoolean("enabled"),
                    triggerAtMillis = o.getLong("triggerAtMillis"),
                    repeatIntervalMillis = if (o.isNull("repeatIntervalMillis")) null else o.getLong("repeatIntervalMillis"),
                    useMinutes = o.getBoolean("useMinutes"),
                    exact = o.getBoolean("exact")
                )
            }
        } catch (t: Throwable) { emptyList() }
    }

    // Guarda la lista actualizada como JSON y actualiza el estado interno.
    private fun persist(list: List<NotificationSetting>) {
        val arr = JSONArray()
        list.forEach { s ->
            val o = JSONObject()
            o.put("id", s.id)
            o.put("taskId", s.taskId)
            o.put("enabled", s.enabled)
            o.put("triggerAtMillis", s.triggerAtMillis)
            o.put("repeatIntervalMillis", s.repeatIntervalMillis)
            o.put("useMinutes", s.useMinutes)
            o.put("exact", s.exact)
            arr.put(o)
        }
        prefs.edit().putString(KEY_NOTIFICATIONS, arr.toString()).apply()
        _state.value = list
    }

    // Actualiza o agrega una notificación y la programa si está habilitada.
    override suspend fun save(setting: NotificationSetting) {
        val list = _state.value.toMutableList()
        val idx = list.indexOfFirst { it.id == setting.id }
        if (idx >= 0) list[idx] = setting else list.add(setting)
        persist(list)
        if (setting.enabled) {
            // schedule via alarmScheduler
            val intent = android.content.Intent(context, com.example.unitask.notifications.AlarmReceiver::class.java).apply {
                putExtra("alarm_id", setting.id)
                putExtra("task_id", setting.taskId)
            }
            val pending = android.app.PendingIntent.getBroadcast(context, setting.id.hashCode(), intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            alarmScheduler.scheduleExact(setting.id, setting.triggerAtMillis, setting.repeatIntervalMillis, pending)
        }
    }

    // Elimina un ajuste y cancela su alarma asociada.
    override suspend fun delete(id: String) {
        val list = _state.value.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val removed = list.removeAt(idx)
            persist(list)
            // cancel scheduled alarm
            val intent = android.content.Intent(context, com.example.unitask.notifications.AlarmReceiver::class.java).apply { putExtra("alarm_id", id) }
            val pending = android.app.PendingIntent.getBroadcast(context, id.hashCode(), intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            alarmScheduler.cancel(id, pending)
        }
    }

    // Obtiene un ajuste por ID desde caché en memoria.
    override suspend fun get(id: String): NotificationSetting? = _state.value.firstOrNull { it.id == id }

    // Flujo observador que emite cambios en los ajustes registrados.
    override fun observeAll(): Flow<List<NotificationSetting>> = _state

    // Programa manualmente una alarma para el ajuste indicado.
    override suspend fun schedule(setting: NotificationSetting) {
        val intent = android.content.Intent(context, com.example.unitask.notifications.AlarmReceiver::class.java).apply {
            putExtra("alarm_id", setting.id)
            putExtra("task_id", setting.taskId)
        }
        val pending = android.app.PendingIntent.getBroadcast(context, setting.id.hashCode(), intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        alarmScheduler.scheduleExact(setting.id, setting.triggerAtMillis, setting.repeatIntervalMillis, pending)
    }

    // Cancela una alarma basada en el ID del ajuste.
    override suspend fun cancel(id: String) {
        val intent = android.content.Intent(context, com.example.unitask.notifications.AlarmReceiver::class.java).apply { putExtra("alarm_id", id) }
        val pending = android.app.PendingIntent.getBroadcast(context, id.hashCode(), intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        alarmScheduler.cancel(id, pending)
    }
}
