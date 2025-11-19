package com.example.unitask.data.repository

import android.content.Context
import android.app.AlarmManager
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.notifications.AlarmScheduler
import com.example.unitask.notifications.RealAlarmManagerWrapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
class SharedPrefsNotificationRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun saveAndGetObservation() = runBlocking {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val wrapper = RealAlarmManagerWrapper(alarmManager)
        val alarmScheduler = AlarmScheduler(wrapper)
        val repo = SharedPrefsNotificationRepository(context, alarmScheduler)
        // Clear
        repo.observeAll().first().forEach { repo.delete(it.id) }

        val setting = NotificationSetting(id = "t1", taskId = null, enabled = true, triggerAtMillis = System.currentTimeMillis() + 60000L, repeatIntervalMillis = 60000L, useMinutes = true, exact = false)
        repo.save(setting)

        val all = repo.observeAll().first()
        assertTrue(all.any { it.id == "t1" })

        val got = repo.get("t1")
        assertNotNull(got)
        assertEquals("t1", got?.id)
        repo.delete("t1")
        val after = repo.observeAll().first()
        assertFalse(after.any { it.id == "t1" })
    }
}
