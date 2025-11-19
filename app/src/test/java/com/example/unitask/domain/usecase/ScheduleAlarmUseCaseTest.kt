package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleAlarmUseCaseTest {
    private class FakeNotificationRepository : NotificationRepository {
        var savedSetting: NotificationSetting? = null
        var scheduledSetting: NotificationSetting? = null

        override suspend fun save(setting: NotificationSetting) {
            savedSetting = setting
        }

        override suspend fun delete(id: String) {}

        override suspend fun get(id: String): NotificationSetting? = null

        override fun observeAll(): Flow<List<NotificationSetting>> = flowOf(emptyList())

        override suspend fun schedule(setting: NotificationSetting) {
            scheduledSetting = setting
        }

        override suspend fun cancel(id: String) {}
    }

    @Test
    fun `saves and schedules notification`() = runBlocking {
        val repo = FakeNotificationRepository()
        val useCase = ScheduleAlarmUseCase(repo)
        val setting = NotificationSetting(
            id = "test",
            taskId = null,
            enabled = true,
            triggerAtMillis = System.currentTimeMillis() + 60000L,
            repeatIntervalMillis = 60000L,
            useMinutes = true,
            exact = true
        )

        val result = useCase(setting)
        assertTrue(result.isSuccess)
        assertNotNull(repo.savedSetting)
        assertNotNull(repo.scheduledSetting)
        assertEquals(setting.id, repo.savedSetting?.id)
        assertEquals(setting.id, repo.scheduledSetting?.id)
    }
}
